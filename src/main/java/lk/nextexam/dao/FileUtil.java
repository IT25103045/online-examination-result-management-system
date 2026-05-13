package lk.nextexam.dao;

import jakarta.servlet.ServletContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Professional file utility for NextExamLK.
 *
 * Responsibilities:
 * - Store data in a stable external folder.
 * - Auto-create missing files.
 * - Auto-copy seed data from WEB-INF/data.
 * - Read/write using UTF-8.
 * - Protect writes using synchronized methods.
 * - Create backups before destructive operations.
 * - Write files atomically using a temporary file.
 * - Sanitize pipe-separated text-file data.
 * - Provide HTML escaping helper for JSP output.
 *
 * NOTE:
 * This is still a text-file persistence layer.
 * It is suitable for coursework/demo/prototype level.
 * For real production, migrate this layer to a database.
 */
public final class FileUtil {

    private static final String DATA_FOLDER_NAME = "NextExamLKData";
    private static final String DEFAULT_DATA_FOLDER = "/WEB-INF/data/";
    private static final String BACKUP_FOLDER_NAME = "backups";

    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private FileUtil() {
        // Utility class: prevent object creation
    }

    /**
     * Returns the base external data directory.
     *
     * Example on Windows:
     * C:/Users/YOUR_USERNAME/NextExamLKData/
     */
    public static File getDataDirectory() {
        String userHome = System.getProperty("user.home");

        if (userHome == null || userHome.trim().isEmpty()) {
            userHome = ".";
        }

        return new File(userHome, DATA_FOLDER_NAME);
    }

    /**
     * Returns the full external file path for a data file.
     */
    public static String getDataFilePath(ServletContext context, String fileName) {
        return getDataFile(context, fileName).getAbsolutePath();
    }

    /**
     * Returns the full external File object for a data file.
     */
    public static File getDataFile(ServletContext context, String fileName) {
        String safeFileName = validateFileName(fileName);
        return new File(getDataDirectory(), safeFileName);
    }

    /**
     * Ensures that the data directory and file exist.
     * If the file is missing or empty, seed data is copied from WEB-INF/data if available.
     */
    public static synchronized void ensureFileExists(ServletContext context, String fileName) throws IOException {
        File file = getDataFile(context, fileName);
        File parentDirectory = file.getParentFile();

        if (parentDirectory != null && !parentDirectory.exists()) {
            boolean created = parentDirectory.mkdirs();

            if (!created && !parentDirectory.exists()) {
                throw new IOException("Unable to create data directory: " + parentDirectory.getAbsolutePath());
            }
        }

        boolean newlyCreated = false;

        if (!file.exists()) {
            boolean created = file.createNewFile();

            if (!created && !file.exists()) {
                throw new IOException("Unable to create data file: " + file.getAbsolutePath());
            }

            newlyCreated = true;
        }

        if (newlyCreated || file.length() == 0) {
            copyDefaultDataIfAvailable(context, fileName, file);
        }
    }

    /**
     * Reads all non-empty lines from a data file.
     */
    public static List<String> readLines(ServletContext context, String fileName) {
        try {
            ensureFileExists(context, fileName);

            File file = getDataFile(context, fileName);
            List<String> lines = new ArrayList<>();

            try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String cleanedLine = line.trim();

                    if (!cleanedLine.isEmpty()) {
                        lines.add(cleanedLine);
                    }
                }
            }

            return lines;

        } catch (IOException | IllegalArgumentException e) {
            logError("Unable to read file: " + fileName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Writes all lines to a file using an atomic write strategy.
     * A backup is created before replacing an existing non-empty file.
     */
    public static synchronized boolean writeLines(ServletContext context, String fileName, List<String> lines) {
        try {
            ensureFileExists(context, fileName);

            File file = getDataFile(context, fileName);
            createBackupIfNeeded(file);

            List<String> safeLines = lines == null ? Collections.emptyList() : lines;
            atomicWrite(file.toPath(), safeLines);

            return true;

        } catch (IOException | IllegalArgumentException e) {
            logError("Unable to write file: " + fileName, e);
            return false;
        }
    }

    /**
     * Appends a single record line to the file.
     * This method is synchronized to reduce concurrent write conflicts.
     */
    public static synchronized boolean appendLine(ServletContext context, String fileName, String line) {
        String safeLine = normalizeRecordLine(line);

        if (safeLine.isEmpty()) {
            return false;
        }

        try {
            ensureFileExists(context, fileName);

            File file = getDataFile(context, fileName);

            try (BufferedWriter writer = Files.newBufferedWriter(
                    file.toPath(),
                    StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND)) {

                writer.write(safeLine);
                writer.newLine();
            }

            return true;

        } catch (IOException | IllegalArgumentException e) {
            logError("Unable to append line to file: " + fileName, e);
            return false;
        }
    }

    /**
     * Deletes a record by ID.
     * The first field before | is treated as the ID.
     */
    public static synchronized boolean deleteLineById(ServletContext context, String fileName, String recordId) {
        String safeRecordId = clean(recordId);

        if (safeRecordId.isEmpty()) {
            return false;
        }

        List<String> lines = readLines(context, fileName);
        List<String> updatedLines = new ArrayList<>();

        boolean deleted = false;

        for (String line : lines) {
            String currentId = getRecordId(line);

            if (currentId.equalsIgnoreCase(safeRecordId)) {
                deleted = true;
            } else {
                updatedLines.add(line);
            }
        }

        return deleted && writeLines(context, fileName, updatedLines);
    }

    /**
     * Updates a record by ID.
     * The first field before | is treated as the ID.
     */
    public static synchronized boolean updateLineById(ServletContext context,
                                                      String fileName,
                                                      String recordId,
                                                      String newLine) {
        String safeRecordId = clean(recordId);
        String safeNewLine = normalizeRecordLine(newLine);

        if (safeRecordId.isEmpty() || safeNewLine.isEmpty()) {
            return false;
        }

        List<String> lines = readLines(context, fileName);
        List<String> updatedLines = new ArrayList<>();

        boolean updated = false;

        for (String line : lines) {
            String currentId = getRecordId(line);

            if (currentId.equalsIgnoreCase(safeRecordId)) {
                updatedLines.add(safeNewLine);
                updated = true;
            } else {
                updatedLines.add(line);
            }
        }

        return updated && writeLines(context, fileName, updatedLines);
    }

    /**
     * Checks if a record ID already exists.
     */
    public static boolean existsById(ServletContext context, String fileName, String recordId) {
        String safeRecordId = clean(recordId);

        if (safeRecordId.isEmpty()) {
            return false;
        }

        List<String> lines = readLines(context, fileName);

        for (String line : lines) {
            String currentId = getRecordId(line);

            if (currentId.equalsIgnoreCase(safeRecordId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Counts records in a file.
     */
    public static int countLines(ServletContext context, String fileName) {
        return readLines(context, fileName).size();
    }

    /**
     * Generates a simple unique ID using a prefix and timestamp.
     *
     * Example:
     * generateId("SUB") -> SUB20260512123045123
     */
    public static String generateId(String prefix) {
        String cleanPrefix = clean(prefix).toUpperCase();

        if (cleanPrefix.isEmpty()) {
            cleanPrefix = "ID";
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        return cleanPrefix + timestamp;
    }

    /**
     * Splits a pipe-separated record safely.
     */
    public static String[] splitRecord(String line) {
        if (line == null) {
            return new String[0];
        }

        return line.split("\\|", -1);
    }

    /**
     * Gets the first field of a record as the record ID.
     */
    public static String getRecordId(String line) {
        String[] data = splitRecord(line);

        if (data.length == 0) {
            return "";
        }

        return clean(data[0]);
    }

    /**
     * Sanitizes a single field before saving to pipe-separated files.
     */
    public static String clean(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace("|", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replaceAll("\\s{2,}", " ");
    }

    /**
     * Sanitizes a whole pipe-separated record line.
     * Keeps pipe separators but cleans each field.
     */
    public static String normalizeRecordLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return "";
        }

        String[] fields = line.split("\\|", -1);
        List<String> cleanedFields = new ArrayList<>();

        for (String field : fields) {
            cleanedFields.add(clean(field));
        }

        return String.join("|", cleanedFields).trim();
    }

    /**
     * Escapes output before printing user/file data inside JSP.
     *
     * Usage:
     * <%= FileUtil.escapeHtml(student.getName()) %>
     */
    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Short alias for JSP readability.
     *
     * Usage:
     * <%= FileUtil.h(student.getName()) %>
     */
    public static String h(String value) {
        return escapeHtml(value);
    }

    /**
     * Checks whether a value is blank.
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Checks whether a value is not blank.
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * Copies default seed data from WEB-INF/data/fileName into the external file.
     */
    private static void copyDefaultDataIfAvailable(ServletContext context, String fileName, File destinationFile) {
        if (context == null || fileName == null || destinationFile == null) {
            return;
        }

        String safeFileName = validateFileName(fileName);
        String defaultResourcePath = DEFAULT_DATA_FOLDER + safeFileName;

        try (InputStream inputStream = context.getResourceAsStream(defaultResourcePath)) {
            if (inputStream == null) {
                return;
            }

            List<String> seedLines = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    String cleanedLine = line.trim();

                    if (!cleanedLine.isEmpty()) {
                        seedLines.add(normalizeRecordLine(cleanedLine));
                    }
                }
            }

            if (!seedLines.isEmpty()) {
                atomicWrite(destinationFile.toPath(), seedLines);
            }

        } catch (IOException e) {
            logError("Unable to copy default seed data for: " + fileName, e);
        }
    }

    /**
     * Writes to a temporary file, then moves it into place.
     */
    private static void atomicWrite(Path targetPath, List<String> lines) throws IOException {
        Path parent = targetPath.getParent();

        if (parent == null) {
            throw new IOException("Target file has no parent directory: " + targetPath);
        }

        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        Path temporaryFile = Files.createTempFile(parent, "nextexam_", ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                if (line != null && !line.trim().isEmpty()) {
                    writer.write(line.trim());
                    writer.newLine();
                }
            }
        }

        try {
            Files.move(
                    temporaryFile,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    temporaryFile,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    /**
     * Creates a backup before destructive write operations.
     */
    private static void createBackupIfNeeded(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists() || sourceFile.length() == 0) {
            return;
        }

        try {
            File backupDirectory = new File(getDataDirectory(), BACKUP_FOLDER_NAME);

            if (!backupDirectory.exists()) {
                boolean created = backupDirectory.mkdirs();

                if (!created && !backupDirectory.exists()) {
                    return;
                }
            }

            String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);

            File backupFile = new File(
                    backupDirectory,
                    sourceFile.getName() + "." + timestamp + ".bak"
            );

            Files.copy(
                    sourceFile.toPath(),
                    backupFile.toPath(),
                    new CopyOption[]{StandardCopyOption.REPLACE_EXISTING}
            );

        } catch (IOException e) {
            logError("Unable to create backup for file: " + sourceFile.getName(), e);
        }
    }

    /**
     * Validates data file names to prevent path traversal.
     */
    private static String validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }

        String safeFileName = fileName.trim();

        if (safeFileName.contains("..")
                || safeFileName.contains("/")
                || safeFileName.contains("\\")
                || !safeFileName.endsWith(".txt")) {
            throw new IllegalArgumentException("Invalid data file name: " + fileName);
        }

        return safeFileName;
    }

    /**
     * Centralized error logging.
     * Later this can be replaced with java.util.logging or SLF4J.
     */
    private static void logError(String message, Exception e) {
        System.err.println("[NextExamLK FileUtil] " + message);

        if (e != null) {
            e.printStackTrace();
        }
    }
}