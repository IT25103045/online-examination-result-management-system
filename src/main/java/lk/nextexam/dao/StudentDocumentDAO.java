package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.StudentDocument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * StudentDocumentDAO handles document verification file operations.
 *
 * Storage file:
 * documents.txt
 *
 * This DAO demonstrates abstraction by hiding file read/write logic
 * from servlet and JSP layers.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class StudentDocumentDAO {

    private static final String FILE_NAME = "documents.txt";

    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean addDocument(ServletContext context, StudentDocument document) {
        if (document == null || !document.isCompleteForSave()) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, document.toFileString());
    }

    public List<StudentDocument> getAllDocuments(ServletContext context) {
        List<StudentDocument> documents = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            StudentDocument document = StudentDocument.fromFileString(line);

            if (document != null && !document.getDocumentId().isEmpty()) {
                documents.add(document);
            }
        }

        documents.sort(Comparator.comparing(StudentDocument::getUploadedAt, String.CASE_INSENSITIVE_ORDER).reversed());
        return documents;
    }

    public StudentDocument getDocumentById(ServletContext context, String documentId) {
        String safeDocumentId = FileUtil.clean(documentId);

        if (safeDocumentId.isEmpty()) {
            return null;
        }

        for (StudentDocument document : getAllDocuments(context)) {
            if (document.getDocumentId().equalsIgnoreCase(safeDocumentId)) {
                return document;
            }
        }

        return null;
    }

    public List<StudentDocument> getDocumentsByStudentId(ServletContext context, String studentId) {
        List<StudentDocument> selectedDocuments = new ArrayList<>();
        String safeStudentId = FileUtil.clean(studentId);

        if (safeStudentId.isEmpty()) {
            return selectedDocuments;
        }

        for (StudentDocument document : getAllDocuments(context)) {
            if (document.getStudentId().equalsIgnoreCase(safeStudentId)) {
                selectedDocuments.add(document);
            }
        }

        return selectedDocuments;
    }

    public List<StudentDocument> getDocumentsByStatus(ServletContext context, String status) {
        List<StudentDocument> selectedDocuments = new ArrayList<>();
        String safeStatus = FileUtil.clean(status);

        if (safeStatus.isEmpty()) {
            return selectedDocuments;
        }

        for (StudentDocument document : getAllDocuments(context)) {
            if (document.getStatus().equalsIgnoreCase(safeStatus)) {
                selectedDocuments.add(document);
            }
        }

        return selectedDocuments;
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
        StudentDocument document = getDocumentById(context, documentId);

        if (document == null) {
            return false;
        }

        document.setStatus(status);
        document.setReviewNote(FileUtil.isBlank(reviewNote) ? "-" : reviewNote);
        document.setReviewedAt(now());

        if (!document.isValidStatus()) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                document.getDocumentId(),
                document.toFileString()
        );
    }

    public boolean deleteDocument(ServletContext context, String documentId) {
        String safeDocumentId = FileUtil.clean(documentId);

        if (safeDocumentId.isEmpty()) {
            return false;
        }

        return FileUtil.deleteLineById(context, FILE_NAME, safeDocumentId);
    }

    public int countAllDocuments(ServletContext context) {
        return getAllDocuments(context).size();
    }

    public int countPendingDocuments(ServletContext context) {
        return getDocumentsByStatus(context, StudentDocument.STATUS_PENDING).size();
    }

    public int countApprovedDocuments(ServletContext context) {
        return getDocumentsByStatus(context, StudentDocument.STATUS_APPROVED).size();
    }

    public int countRejectedDocuments(ServletContext context) {
        return getDocumentsByStatus(context, StudentDocument.STATUS_REJECTED).size();
    }

    public String now() {
        return LocalDateTime.now().format(DISPLAY_DATE_TIME);
    }
}