package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ExamSubmission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional MySQL DAO for exam submissions.
 *
 * MySQL table:
 * exam_submissions
 *
 * Columns:
 * submission_id, exam_id, student_id, student_name, submitted_at,
 * answers_data, score, total_marks, status
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ExamSubmissionDAO {

    public List<ExamSubmission> getAllSubmissions(ServletContext context) {
        List<ExamSubmission> submissions = new ArrayList<>();

        String sql = "SELECT submission_id, exam_id, student_id, student_name, submitted_at, " +
                "answers_data, score, total_marks, status " +
                "FROM exam_submissions " +
                "ORDER BY submitted_at DESC, submission_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                submissions.add(mapResultSetToSubmission(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> getAllSubmissions failed");
            e.printStackTrace();
        }

        submissions.sort(submissionDateComparator());
        return submissions;
    }

    public ExamSubmission getSubmissionById(ServletContext context, String submissionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return null;
        }

        String sql = "SELECT submission_id, exam_id, student_id, student_name, submitted_at, " +
                "answers_data, score, total_marks, status " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanSubmissionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToSubmission(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> getSubmissionById failed for " + cleanSubmissionId);
            e.printStackTrace();
        }

        return null;
    }

    public List<ExamSubmission> getSubmissionsByStudent(ServletContext context, String studentId) {
        List<ExamSubmission> selectedSubmissions = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedSubmissions;
        }

        String sql = "SELECT submission_id, exam_id, student_id, student_name, submitted_at, " +
                "answers_data, score, total_marks, status " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "ORDER BY submitted_at DESC, submission_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedSubmissions.add(mapResultSetToSubmission(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> getSubmissionsByStudent failed for " + cleanStudentId);
            e.printStackTrace();
        }

        selectedSubmissions.sort(submissionDateComparator());
        return selectedSubmissions;
    }

    public List<ExamSubmission> getSubmissionsByExam(ServletContext context, String examId) {
        List<ExamSubmission> selectedSubmissions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return selectedSubmissions;
        }

        String sql = "SELECT submission_id, exam_id, student_id, student_name, submitted_at, " +
                "answers_data, score, total_marks, status " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "ORDER BY submitted_at DESC, submission_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedSubmissions.add(mapResultSetToSubmission(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> getSubmissionsByExam failed for " + cleanExamId);
            e.printStackTrace();
        }

        selectedSubmissions.sort(submissionDateComparator());
        return selectedSubmissions;
    }

    public List<ExamSubmission> getSubmissionsByStatus(ServletContext context, String status) {
        List<ExamSubmission> selectedSubmissions = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return selectedSubmissions;
        }

        String sql = "SELECT submission_id, exam_id, student_id, student_name, submitted_at, " +
                "answers_data, score, total_marks, status " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY submitted_at DESC, submission_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedSubmissions.add(mapResultSetToSubmission(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> getSubmissionsByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        selectedSubmissions.sort(submissionDateComparator());
        return selectedSubmissions;
    }

    public List<ExamSubmission> getSubmissionsByExamAndStatus(ServletContext context,
                                                              String examId,
                                                              String status) {
        List<ExamSubmission> selectedSubmissions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanExamId.isEmpty() || cleanStatus.isEmpty()) {
            return selectedSubmissions;
        }

        String sql = "SELECT submission_id, exam_id, student_id, student_name, submitted_at, " +
                "answers_data, score, total_marks, status " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY submitted_at DESC, submission_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);
            statement.setString(2, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedSubmissions.add(mapResultSetToSubmission(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> getSubmissionsByExamAndStatus failed");
            e.printStackTrace();
        }

        selectedSubmissions.sort(submissionDateComparator());
        return selectedSubmissions;
    }

    public ExamSubmission getSubmissionByStudentAndExam(ServletContext context,
                                                        String studentId,
                                                        String examId) {
        String cleanStudentId = FileUtil.clean(studentId);
        String cleanExamId = FileUtil.clean(examId);

        if (cleanStudentId.isEmpty() || cleanExamId.isEmpty()) {
            return null;
        }

        String sql = "SELECT submission_id, exam_id, student_id, student_name, submitted_at, " +
                "answers_data, score, total_marks, status " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) <> LOWER(TRIM(?)) " +
                "ORDER BY submitted_at DESC " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);
            statement.setString(2, cleanExamId);
            statement.setString(3, ExamSubmission.STATUS_CANCELLED);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToSubmission(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> getSubmissionByStudentAndExam failed");
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasStudentSubmitted(ServletContext context, String studentId, String examId) {
        return getSubmissionByStudentAndExam(context, studentId, examId) != null;
    }

    public boolean addSubmission(ServletContext context, ExamSubmission submission) {
        if (!isValidForCreate(context, submission)) {
            return false;
        }

        String sql = "INSERT INTO exam_submissions " +
                "(submission_id, exam_id, student_id, student_name, submitted_at, answers_data, score, total_marks, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillSubmissionStatement(statement, submission);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> addSubmission failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSubmission(ServletContext context, ExamSubmission submission) {
        if (!isValidForUpdate(context, submission)) {
            return false;
        }

        String sql = "UPDATE exam_submissions SET " +
                "exam_id = ?, " +
                "student_id = ?, " +
                "student_name = ?, " +
                "submitted_at = ?, " +
                "answers_data = ?, " +
                "score = ?, " +
                "total_marks = ?, " +
                "status = ? " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, submission.getExamId());
            statement.setString(2, submission.getStudentId());
            statement.setString(3, submission.getStudentName());
            statement.setTimestamp(4, toTimestamp(submission.getSubmittedDateTime()));
            statement.setString(5, submission.getAnswersData());
            statement.setDouble(6, submission.getScoreAsDouble());
            statement.setDouble(7, submission.getTotalMarksAsDouble());
            statement.setString(8, submission.getStatus());
            statement.setString(9, submission.getSubmissionId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> updateSubmission failed for " +
                    (submission != null ? submission.getSubmissionId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSubmission(ServletContext context, String submissionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return false;
        }

        ExamSubmission submission = getSubmissionById(context, cleanSubmissionId);

        if (submission == null) {
            return false;
        }

        /*
         * Professional rule:
         * Published submissions should not be physically deleted.
         * Cancel them instead to preserve audit history.
         */
        if (submission.isPublished()) {
            return false;
        }

        String sql = "DELETE FROM exam_submissions " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanSubmissionId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> deleteSubmission failed for " + cleanSubmissionId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAsAutoMarked(ServletContext context, String submissionId) {
        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_AUTO_MARKED);
    }

    public boolean markAsManualReviewRequired(ServletContext context, String submissionId) {
        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_MANUAL_REVIEW_REQUIRED);
    }

    public boolean markAsMarked(ServletContext context, String submissionId) {
        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_MARKED);
    }

    public boolean publishSubmission(ServletContext context, String submissionId) {
        ExamSubmission submission = getSubmissionById(context, submissionId);

        if (submission == null) {
            return false;
        }

        if (!submission.canBePublished()) {
            return false;
        }

        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_PUBLISHED);
    }

    public boolean cancelSubmission(ServletContext context, String submissionId) {
        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_CANCELLED);
    }

    public boolean updateSubmissionStatus(ServletContext context, String submissionId, String newStatus) {
        String cleanSubmissionId = FileUtil.clean(submissionId);
        String cleanStatus = normalizeStatusInput(newStatus);

        if (cleanSubmissionId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        ExamSubmission submission = getSubmissionById(context, cleanSubmissionId);

        if (submission == null) {
            return false;
        }

        submission.setStatus(cleanStatus);

        if (!submission.isValidStatus()) {
            return false;
        }

        String sql = "UPDATE exam_submissions SET status = ? " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);
            statement.setString(2, cleanSubmissionId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> updateSubmissionStatus failed for " + cleanSubmissionId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateScore(ServletContext context,
                               String submissionId,
                               double score,
                               double totalMarks,
                               String status) {

        String cleanSubmissionId = FileUtil.clean(submissionId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanSubmissionId.isEmpty()) {
            return false;
        }

        if (score < 0 || totalMarks <= 0 || score > totalMarks) {
            return false;
        }

        ExamSubmission submission = getSubmissionById(context, cleanSubmissionId);

        if (submission == null) {
            return false;
        }

        if (!FileUtil.isBlank(cleanStatus)) {
            submission.setStatus(cleanStatus);

            if (!submission.isValidStatus()) {
                return false;
            }
        }

        String sql = "UPDATE exam_submissions SET " +
                "score = ?, " +
                "total_marks = ?, " +
                "status = ? " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, score);
            statement.setDouble(2, totalMarks);
            statement.setString(3, FileUtil.isBlank(cleanStatus) ? submission.getStatus() : cleanStatus);
            statement.setString(4, cleanSubmissionId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> updateScore failed for " + cleanSubmissionId);
            e.printStackTrace();
            return false;
        }
    }

    public int countAllSubmissions(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM exam_submissions");
    }

    public int countSubmissionsByExam(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM exam_submissions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanExamId);
    }

    public int countSubmissionsByStudent(ServletContext context, String studentId) {
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM exam_submissions " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStudentId);
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM exam_submissions " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    public int countSubmitted(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_SUBMITTED);
    }

    public int countAutoMarked(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_AUTO_MARKED);
    }

    public int countManualReviewRequired(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_MANUAL_REVIEW_REQUIRED);
    }

    public int countMarked(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_MARKED);
    }

    public int countPublished(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_PUBLISHED);
    }

    public int countCancelled(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_CANCELLED);
    }

    public double calculateAveragePercentageByExam(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0.0;
        }

        String sql = "SELECT AVG((score / total_marks) * 100) AS average_percentage " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) <> LOWER(TRIM(?)) " +
                "AND total_marks > 0";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);
            statement.setString(2, ExamSubmission.STATUS_CANCELLED);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("average_percentage");
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> calculateAveragePercentageByExam failed");
            e.printStackTrace();
        }

        return 0.0;
    }

    public double calculateHighestPercentageByExam(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0.0;
        }

        String sql = "SELECT MAX((score / total_marks) * 100) AS highest_percentage " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) <> LOWER(TRIM(?)) " +
                "AND total_marks > 0";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);
            statement.setString(2, ExamSubmission.STATUS_CANCELLED);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("highest_percentage");
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> calculateHighestPercentageByExam failed");
            e.printStackTrace();
        }

        return 0.0;
    }

    public double calculateLowestPercentageByExam(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0.0;
        }

        String sql = "SELECT MIN((score / total_marks) * 100) AS lowest_percentage " +
                "FROM exam_submissions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) <> LOWER(TRIM(?)) " +
                "AND total_marks > 0";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);
            statement.setString(2, ExamSubmission.STATUS_CANCELLED);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("lowest_percentage");
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> calculateLowestPercentageByExam failed");
            e.printStackTrace();
        }

        return 0.0;
    }

    public boolean existsById(ServletContext context, String submissionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return false;
        }

        String sql = "SELECT submission_id FROM exam_submissions " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanSubmissionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> existsById failed for " + cleanSubmissionId);
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidForCreate(ServletContext context, ExamSubmission submission) {
        if (!isSubmissionObjectValid(submission)) {
            return false;
        }

        if (existsById(context, submission.getSubmissionId())) {
            return false;
        }

        /*
         * One active submission per student per exam.
         * Cancelled submissions do not block future attempts.
         */
        return !hasStudentSubmitted(context, submission.getStudentId(), submission.getExamId());
    }

    private boolean isValidForUpdate(ServletContext context, ExamSubmission submission) {
        if (!isSubmissionObjectValid(submission)) {
            return false;
        }

        return existsById(context, submission.getSubmissionId());
    }

    private boolean isSubmissionObjectValid(ExamSubmission submission) {
        return submission != null && submission.isCompleteForSave();
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("EXAMSUBMISSIONDAO ERROR -> countByQuery failed");
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
            System.out.println("EXAMSUBMISSIONDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillSubmissionStatement(PreparedStatement statement,
                                         ExamSubmission submission) throws SQLException {

        statement.setString(1, submission.getSubmissionId());
        statement.setString(2, submission.getExamId());
        statement.setString(3, submission.getStudentId());
        statement.setString(4, submission.getStudentName());
        statement.setTimestamp(5, toTimestamp(submission.getSubmittedDateTime()));
        statement.setString(6, submission.getAnswersData());
        statement.setDouble(7, submission.getScoreAsDouble());
        statement.setDouble(8, submission.getTotalMarksAsDouble());
        statement.setString(9, submission.getStatus());
    }

    private ExamSubmission mapResultSetToSubmission(ResultSet resultSet) throws SQLException {
        Timestamp submittedTimestamp = resultSet.getTimestamp("submitted_at");
        String submittedAt = submittedTimestamp == null
                ? ""
                : submittedTimestamp.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return new ExamSubmission(
                safe(resultSet.getString("submission_id")),
                safe(resultSet.getString("exam_id")),
                safe(resultSet.getString("student_id")),
                safe(resultSet.getString("student_name")),
                submittedAt,
                safe(resultSet.getString("answers_data")),
                formatNumber(resultSet.getDouble("score")),
                formatNumber(resultSet.getDouble("total_marks")),
                normalizeStatusInput(resultSet.getString("status"))
        );
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return Timestamp.valueOf(LocalDateTime.now());
        }

        return Timestamp.valueOf(dateTime);
    }

    private Comparator<ExamSubmission> submissionDateComparator() {
        return Comparator
                .comparing(
                        (ExamSubmission submission) -> {
                            LocalDateTime dateTime = submission.getSubmittedDateTime();
                            return dateTime == null ? LocalDateTime.MIN : dateTime;
                        }
                )
                .reversed()
                .thenComparing(ExamSubmission::getSubmissionId, String.CASE_INSENSITIVE_ORDER);
    }

    private String normalizeStatusInput(String value) {
        String statusValue = safe(value);

        if (statusValue.equalsIgnoreCase(ExamSubmission.STATUS_SUBMITTED)) {
            return ExamSubmission.STATUS_SUBMITTED;
        }

        if (statusValue.equalsIgnoreCase(ExamSubmission.STATUS_AUTO_MARKED)) {
            return ExamSubmission.STATUS_AUTO_MARKED;
        }

        if (statusValue.equalsIgnoreCase(ExamSubmission.STATUS_MANUAL_REVIEW_REQUIRED)) {
            return ExamSubmission.STATUS_MANUAL_REVIEW_REQUIRED;
        }

        if (statusValue.equalsIgnoreCase(ExamSubmission.STATUS_MARKED)) {
            return ExamSubmission.STATUS_MARKED;
        }

        if (statusValue.equalsIgnoreCase(ExamSubmission.STATUS_PUBLISHED)) {
            return ExamSubmission.STATUS_PUBLISHED;
        }

        if (statusValue.equalsIgnoreCase(ExamSubmission.STATUS_CANCELLED)) {
            return ExamSubmission.STATUS_CANCELLED;
        }

        return statusValue;
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}