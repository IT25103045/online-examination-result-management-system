package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.StudentDocument;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * StudentDocumentDAO handles MySQL document verification operations.
 *
 * MySQL table:
 * student_documents
 *
 * Columns:
 * document_id, student_id, student_name, document_type, file_name,
 * file_path, status, review_note, uploaded_at, reviewed_at
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class StudentDocumentDAO {

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean addDocument(ServletContext context, StudentDocument document) {
        if (document == null) {
            return false;
        }

        if (document.getDocumentId().isEmpty()) {
            document.setDocumentId(FileUtil.generateId("DOC"));
        }

        if (document.getUploadedAt().isEmpty()) {
            document.setUploadedAt(now());
        }

        if (document.getStatus().isEmpty()) {
            document.setStatus(StudentDocument.STATUS_PENDING);
        }

        if (!document.isCompleteForSave()) {
            return false;
        }

        if (existsById(document.getDocumentId())) {
            document.setDocumentId(FileUtil.generateId("DOC"));
        }

        String sql = "INSERT INTO student_documents " +
                "(document_id, student_id, student_name, document_type, file_name, file_path, status, review_note, uploaded_at, reviewed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillDocumentStatement(statement, document);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> addDocument failed");
            e.printStackTrace();
            return false;
        }
    }

    public List<StudentDocument> getAllDocuments(ServletContext context) {
        List<StudentDocument> documents = new ArrayList<>();

        String sql = "SELECT document_id, student_id, student_name, document_type, file_name, " +
                "file_path, status, review_note, uploaded_at, reviewed_at " +
                "FROM student_documents " +
                "ORDER BY uploaded_at DESC, document_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                documents.add(mapResultSetToDocument(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> getAllDocuments failed");
            e.printStackTrace();
        }

        documents.sort(documentComparator());
        return documents;
    }

    public StudentDocument getDocumentById(ServletContext context, String documentId) {
        String cleanDocumentId = FileUtil.clean(documentId);

        if (cleanDocumentId.isEmpty()) {
            return null;
        }

        String sql = "SELECT document_id, student_id, student_name, document_type, file_name, " +
                "file_path, status, review_note, uploaded_at, reviewed_at " +
                "FROM student_documents " +
                "WHERE LOWER(TRIM(document_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanDocumentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDocument(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> getDocumentById failed for " + cleanDocumentId);
            e.printStackTrace();
        }

        return null;
    }

    public List<StudentDocument> getDocumentsByStudentId(ServletContext context, String studentId) {
        List<StudentDocument> documents = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return documents;
        }

        String sql = "SELECT document_id, student_id, student_name, document_type, file_name, " +
                "file_path, status, review_note, uploaded_at, reviewed_at " +
                "FROM student_documents " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "ORDER BY uploaded_at DESC, document_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    documents.add(mapResultSetToDocument(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> getDocumentsByStudentId failed for " + cleanStudentId);
            e.printStackTrace();
        }

        documents.sort(documentComparator());
        return documents;
    }

    public List<StudentDocument> getDocumentsByStatus(ServletContext context, String status) {
        List<StudentDocument> documents = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return documents;
        }

        String sql = "SELECT document_id, student_id, student_name, document_type, file_name, " +
                "file_path, status, review_note, uploaded_at, reviewed_at " +
                "FROM student_documents " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY uploaded_at DESC, document_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    documents.add(mapResultSetToDocument(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> getDocumentsByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        documents.sort(documentComparator());
        return documents;
    }

    public List<StudentDocument> getDocumentsByType(ServletContext context, String documentType) {
        List<StudentDocument> documents = new ArrayList<>();
        String cleanType = normalizeDocumentTypeInput(documentType);

        if (cleanType.isEmpty()) {
            return documents;
        }

        String sql = "SELECT document_id, student_id, student_name, document_type, file_name, " +
                "file_path, status, review_note, uploaded_at, reviewed_at " +
                "FROM student_documents " +
                "WHERE LOWER(TRIM(document_type)) = LOWER(TRIM(?)) " +
                "ORDER BY uploaded_at DESC, document_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    documents.add(mapResultSetToDocument(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> getDocumentsByType failed for " + cleanType);
            e.printStackTrace();
        }

        documents.sort(documentComparator());
        return documents;
    }

    public boolean approveDocument(ServletContext context, String documentId, String reviewNote) {
        return updateDocumentStatus(
                context,
                documentId,
                StudentDocument.STATUS_APPROVED,
                reviewNote
        );
    }

    public boolean rejectDocument(ServletContext context, String documentId, String reviewNote) {
        return updateDocumentStatus(
                context,
                documentId,
                StudentDocument.STATUS_REJECTED,
                reviewNote
        );
    }

    public boolean updateDocumentStatus(ServletContext context,
                                        String documentId,
                                        String status,
                                        String reviewNote) {

        String cleanDocumentId = FileUtil.clean(documentId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanDocumentId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        StudentDocument document = getDocumentById(context, cleanDocumentId);

        if (document == null) {
            return false;
        }

        document.setStatus(cleanStatus);
        document.setReviewNote(FileUtil.isBlank(reviewNote) ? "-" : reviewNote);
        document.setReviewedAt(now());

        if (!document.isValidStatus()) {
            return false;
        }

        String sql = "UPDATE student_documents SET " +
                "status = ?, " +
                "review_note = ?, " +
                "reviewed_at = ? " +
                "WHERE LOWER(TRIM(document_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, document.getStatus());
            statement.setString(2, document.getReviewNote());
            statement.setTimestamp(3, toTimestamp(document.getReviewedAt()));
            statement.setString(4, cleanDocumentId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> updateDocumentStatus failed for " + cleanDocumentId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDocument(ServletContext context, StudentDocument document) {
        if (document == null || document.getDocumentId().isEmpty()) {
            return false;
        }

        if (!document.isCompleteForSave()) {
            return false;
        }

        String sql = "UPDATE student_documents SET " +
                "student_id = ?, " +
                "student_name = ?, " +
                "document_type = ?, " +
                "file_name = ?, " +
                "file_path = ?, " +
                "status = ?, " +
                "review_note = ?, " +
                "uploaded_at = ?, " +
                "reviewed_at = ? " +
                "WHERE LOWER(TRIM(document_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, document.getStudentId());
            statement.setString(2, document.getStudentName());
            statement.setString(3, document.getDocumentType());
            statement.setString(4, document.getFileName());
            statement.setString(5, document.getFilePath());
            statement.setString(6, document.getStatus());
            statement.setString(7, document.getReviewNote());
            statement.setTimestamp(8, toTimestamp(document.getUploadedAt()));
            statement.setTimestamp(9, toNullableTimestamp(document.getReviewedAt()));
            statement.setString(10, document.getDocumentId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> updateDocument failed for " +
                    (document != null ? document.getDocumentId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDocument(ServletContext context, String documentId) {
        String cleanDocumentId = FileUtil.clean(documentId);

        if (cleanDocumentId.isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM student_documents " +
                "WHERE LOWER(TRIM(document_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanDocumentId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> deleteDocument failed for " + cleanDocumentId);
            e.printStackTrace();
            return false;
        }
    }

    public int countAllDocuments(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM student_documents");
    }

    public int countPendingDocuments(ServletContext context) {
        return countDocumentsByStatus(StudentDocument.STATUS_PENDING);
    }

    public int countApprovedDocuments(ServletContext context) {
        return countDocumentsByStatus(StudentDocument.STATUS_APPROVED);
    }

    public int countRejectedDocuments(ServletContext context) {
        return countDocumentsByStatus(StudentDocument.STATUS_REJECTED);
    }

    public int countDocumentsByStudentId(ServletContext context, String studentId) {
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM student_documents " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?))";

        return countBySingleParameterQuery(sql, cleanStudentId);
    }

    public String now() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME);
    }

    private boolean existsById(String documentId) {
        String cleanDocumentId = FileUtil.clean(documentId);

        if (cleanDocumentId.isEmpty()) {
            return false;
        }

        String sql = "SELECT document_id FROM student_documents " +
                "WHERE LOWER(TRIM(document_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanDocumentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> existsById failed for " + cleanDocumentId);
            e.printStackTrace();
            return false;
        }
    }

    private int countDocumentsByStatus(String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM student_documents " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";

        return countBySingleParameterQuery(sql, cleanStatus);
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> countByQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private int countBySingleParameterQuery(String sql, String parameter) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDOCUMENTDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillDocumentStatement(PreparedStatement statement,
                                       StudentDocument document) throws SQLException {
        statement.setString(1, document.getDocumentId());
        statement.setString(2, document.getStudentId());
        statement.setString(3, document.getStudentName());
        statement.setString(4, document.getDocumentType());
        statement.setString(5, document.getFileName());
        statement.setString(6, document.getFilePath());
        statement.setString(7, document.getStatus());
        statement.setString(8, document.getReviewNote());
        statement.setTimestamp(9, toTimestamp(document.getUploadedAt()));
        statement.setTimestamp(10, toNullableTimestamp(document.getReviewedAt()));
    }

    private StudentDocument mapResultSetToDocument(ResultSet resultSet) throws SQLException {
        return new StudentDocument(
                safe(resultSet.getString("document_id")),
                safe(resultSet.getString("student_id")),
                safe(resultSet.getString("student_name")),
                normalizeDocumentTypeInput(resultSet.getString("document_type")),
                safe(resultSet.getString("file_name")),
                safe(resultSet.getString("file_path")),
                normalizeStatusInput(resultSet.getString("status")),
                safe(resultSet.getString("review_note")),
                fromTimestamp(resultSet.getTimestamp("uploaded_at")),
                fromTimestamp(resultSet.getTimestamp("reviewed_at"))
        );
    }

    private Timestamp toTimestamp(String value) {
        LocalDateTime dateTime = parseDateTime(value);

        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }

        return Timestamp.valueOf(dateTime);
    }

    private Timestamp toNullableTimestamp(String value) {
        LocalDateTime dateTime = parseDateTime(value);

        if (dateTime == null) {
            return null;
        }

        return Timestamp.valueOf(dateTime);
    }

    private String fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        return timestamp.toLocalDateTime().format(STORAGE_DATE_TIME);
    }

    private LocalDateTime parseDateTime(String value) {
        String cleanValue = FileUtil.clean(value);

        if (cleanValue.isEmpty() || cleanValue.equals("-")) {
            return null;
        }

        try {
            return LocalDateTime.parse(cleanValue, STORAGE_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String normalizeStatusInput(String value) {
        String statusValue = safe(value);

        if (statusValue.equalsIgnoreCase(StudentDocument.STATUS_PENDING)) {
            return StudentDocument.STATUS_PENDING;
        }

        if (statusValue.equalsIgnoreCase(StudentDocument.STATUS_APPROVED)) {
            return StudentDocument.STATUS_APPROVED;
        }

        if (statusValue.equalsIgnoreCase(StudentDocument.STATUS_REJECTED)) {
            return StudentDocument.STATUS_REJECTED;
        }

        return statusValue;
    }

    private String normalizeDocumentTypeInput(String value) {
        String typeValue = safe(value);

        if (typeValue.equalsIgnoreCase(StudentDocument.TYPE_STUDENT_ID)) {
            return StudentDocument.TYPE_STUDENT_ID;
        }

        if (typeValue.equalsIgnoreCase(StudentDocument.TYPE_MEDICAL)) {
            return StudentDocument.TYPE_MEDICAL;
        }

        if (typeValue.equalsIgnoreCase(StudentDocument.TYPE_EXAM_ELIGIBILITY)) {
            return StudentDocument.TYPE_EXAM_ELIGIBILITY;
        }

        if (typeValue.equalsIgnoreCase(StudentDocument.TYPE_OTHER)) {
            return StudentDocument.TYPE_OTHER;
        }

        return typeValue;
    }

    private Comparator<StudentDocument> documentComparator() {
        return Comparator
                .comparing(StudentDocument::getUploadedAt, String.CASE_INSENSITIVE_ORDER)
                .reversed()
                .thenComparing(StudentDocument::getDocumentId, String.CASE_INSENSITIVE_ORDER);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}