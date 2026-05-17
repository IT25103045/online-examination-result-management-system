package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Exam;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional MySQL DAO for exam management.
 *
 * MySQL table:
 * exams
 *
 * Columns:
 * exam_id, subject, exam_date, duration, total_marks, status
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ExamDAO {

    public List<Exam> getAllExams(ServletContext context) {
        List<Exam> exams = new ArrayList<>();

        String sql = "SELECT exam_id, subject, exam_date, duration, total_marks, status " +
                "FROM exams " +
                "ORDER BY exam_date ASC, exam_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                exams.add(mapResultSetToExam(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> getAllExams failed");
            e.printStackTrace();
        }

        exams.sort(examDateComparator());
        return exams;
    }

    public Exam getExamById(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return null;
        }

        String sql = "SELECT exam_id, subject, exam_date, duration, total_marks, status " +
                "FROM exams " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToExam(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> getExamById failed for " + cleanExamId);
            e.printStackTrace();
        }

        return null;
    }

    public List<Exam> getExamsByStatus(ServletContext context, String status) {
        List<Exam> selectedExams = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return selectedExams;
        }

        String sql = "SELECT exam_id, subject, exam_date, duration, total_marks, status " +
                "FROM exams " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY exam_date ASC, exam_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedExams.add(mapResultSetToExam(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> getExamsByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        selectedExams.sort(examDateComparator());
        return selectedExams;
    }

    public List<Exam> getDraftExams(ServletContext context) {
        return getExamsByStatus(context, Exam.STATUS_DRAFT);
    }

    public List<Exam> getScheduledExams(ServletContext context) {
        return getExamsByStatus(context, Exam.STATUS_SCHEDULED);
    }

    public List<Exam> getActiveExams(ServletContext context) {
        return getExamsByStatus(context, Exam.STATUS_ACTIVE);
    }

    public List<Exam> getOngoingExams(ServletContext context) {
        return getExamsByStatus(context, Exam.STATUS_ONGOING);
    }

    public List<Exam> getPublishedExams(ServletContext context) {
        return getExamsByStatus(context, Exam.STATUS_PUBLISHED);
    }

    public List<Exam> getCompletedExams(ServletContext context) {
        List<Exam> completedExams = new ArrayList<>();

        String sql = "SELECT exam_id, subject, exam_date, duration, total_marks, status " +
                "FROM exams " +
                "WHERE LOWER(TRIM(status)) IN (LOWER(?), LOWER(?)) " +
                "ORDER BY exam_date ASC, exam_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Exam.STATUS_COMPLETED);
            statement.setString(2, Exam.STATUS_PUBLISHED);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    completedExams.add(mapResultSetToExam(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> getCompletedExams failed");
            e.printStackTrace();
        }

        completedExams.sort(examDateComparator());
        return completedExams;
    }

    public List<Exam> getClosedExams(ServletContext context) {
        List<Exam> closedExams = new ArrayList<>();

        String sql = "SELECT exam_id, subject, exam_date, duration, total_marks, status " +
                "FROM exams " +
                "WHERE LOWER(TRIM(status)) IN (LOWER(?), LOWER(?), LOWER(?), LOWER(?)) " +
                "ORDER BY exam_date ASC, exam_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Exam.STATUS_COMPLETED);
            statement.setString(2, Exam.STATUS_PUBLISHED);
            statement.setString(3, Exam.STATUS_CANCELLED);
            statement.setString(4, Exam.STATUS_INACTIVE);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    closedExams.add(mapResultSetToExam(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> getClosedExams failed");
            e.printStackTrace();
        }

        closedExams.sort(examDateComparator());
        return closedExams;
    }

    public List<Exam> getAttemptableExams(ServletContext context) {
        List<Exam> attemptableExams = new ArrayList<>();

        String sql = "SELECT exam_id, subject, exam_date, duration, total_marks, status " +
                "FROM exams " +
                "WHERE LOWER(TRIM(status)) IN (LOWER(?), LOWER(?), LOWER(?)) " +
                "ORDER BY exam_date ASC, exam_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Exam.STATUS_SCHEDULED);
            statement.setString(2, Exam.STATUS_ACTIVE);
            statement.setString(3, Exam.STATUS_ONGOING);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Exam exam = mapResultSetToExam(resultSet);

                    if (exam.canStudentAttempt()) {
                        attemptableExams.add(exam);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> getAttemptableExams failed");
            e.printStackTrace();
        }

        attemptableExams.sort(examDateComparator());
        return attemptableExams;
    }

    public List<Exam> getUpcomingExams(ServletContext context) {
        List<Exam> upcomingExams = new ArrayList<>();

        String sql = "SELECT exam_id, subject, exam_date, duration, total_marks, status " +
                "FROM exams " +
                "WHERE exam_date > CURRENT_DATE() " +
                "AND LOWER(TRIM(status)) NOT IN (LOWER(?), LOWER(?), LOWER(?), LOWER(?)) " +
                "ORDER BY exam_date ASC, exam_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Exam.STATUS_COMPLETED);
            statement.setString(2, Exam.STATUS_PUBLISHED);
            statement.setString(3, Exam.STATUS_CANCELLED);
            statement.setString(4, Exam.STATUS_INACTIVE);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    upcomingExams.add(mapResultSetToExam(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> getUpcomingExams failed");
            e.printStackTrace();
        }

        upcomingExams.sort(examDateComparator());
        return upcomingExams;
    }

    public List<Exam> getTodayExams(ServletContext context) {
        List<Exam> todayExams = new ArrayList<>();

        String sql = "SELECT exam_id, subject, exam_date, duration, total_marks, status " +
                "FROM exams " +
                "WHERE exam_date = CURRENT_DATE() " +
                "ORDER BY exam_date ASC, exam_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                todayExams.add(mapResultSetToExam(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> getTodayExams failed");
            e.printStackTrace();
        }

        todayExams.sort(examDateComparator());
        return todayExams;
    }

    public boolean validateExamForStudentAttempt(ServletContext context, String examId) {
        Exam exam = getExamById(context, examId);
        return exam != null && exam.canStudentAttempt();
    }

    public String getStudentAttemptValidationMessage(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return "Exam ID is missing.";
        }

        Exam exam = getExamById(context, cleanExamId);

        if (exam == null) {
            return "Exam not found.";
        }

        if (exam.isDraft()) {
            return "This exam is still in draft mode.";
        }

        if (exam.isCancelled()) {
            return "This exam has been cancelled.";
        }

        if (exam.isInactive()) {
            return "This exam is inactive.";
        }

        if (exam.isCompleted()) {
            return "This exam has already been completed.";
        }

        if (exam.isPublished()) {
            return "This exam result has already been published.";
        }

        if (!exam.canStudentAttempt()) {
            return "This exam is not currently available for student attempts.";
        }

        return "OK";
    }

    public boolean addExam(ServletContext context, Exam exam) {
        if (!isValidForCreate(context, exam)) {
            return false;
        }

        String sql = "INSERT INTO exams " +
                "(exam_id, subject, exam_date, duration, total_marks, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillExamStatement(statement, exam);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> addExam failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateExam(ServletContext context, Exam exam) {
        if (!isValidForUpdate(context, exam)) {
            return false;
        }

        String sql = "UPDATE exams SET " +
                "subject = ?, " +
                "exam_date = ?, " +
                "duration = ?, " +
                "total_marks = ?, " +
                "status = ? " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, exam.getSubject());
            statement.setDate(2, Date.valueOf(exam.getExamLocalDate()));
            statement.setInt(3, exam.getDurationMinutes());
            statement.setDouble(4, exam.getTotalMarksAsDouble());
            statement.setString(5, exam.getStatus());
            statement.setString(6, exam.getExamId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> updateExam failed for " +
                    (exam != null ? exam.getExamId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteExam(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return false;
        }

        Exam exam = getExamById(context, cleanExamId);

        if (exam == null) {
            return false;
        }

        /*
         * Professional rule:
         * Do not delete exams that are ongoing, completed, or published.
         * These should be cancelled/inactivated instead to preserve history.
         */
        if (exam.isOngoing() || exam.isCompleted() || exam.isPublished()) {
            return false;
        }

        String sql = "DELETE FROM exams WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> deleteExam failed for " + cleanExamId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsById(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return false;
        }

        String sql = "SELECT exam_id FROM exams " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> existsById failed for " + cleanExamId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateExamStatus(ServletContext context, String examId, String newStatus) {
        String cleanExamId = FileUtil.clean(examId);
        String cleanStatus = normalizeStatusInput(newStatus);

        if (cleanExamId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        Exam exam = getExamById(context, cleanExamId);

        if (exam == null) {
            return false;
        }

        exam.setStatus(cleanStatus);

        if (!exam.isValidStatus()) {
            return false;
        }

        String sql = "UPDATE exams SET status = ? " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);
            statement.setString(2, cleanExamId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> updateExamStatus failed for " + cleanExamId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAsDraft(ServletContext context, String examId) {
        return updateExamStatus(context, examId, Exam.STATUS_DRAFT);
    }

    public boolean scheduleExam(ServletContext context, String examId) {
        return updateExamStatus(context, examId, Exam.STATUS_SCHEDULED);
    }

    public boolean activateExam(ServletContext context, String examId) {
        return updateExamStatus(context, examId, Exam.STATUS_ACTIVE);
    }

    public boolean markAsOngoing(ServletContext context, String examId) {
        return updateExamStatus(context, examId, Exam.STATUS_ONGOING);
    }

    public boolean completeExam(ServletContext context, String examId) {
        return updateExamStatus(context, examId, Exam.STATUS_COMPLETED);
    }

    public boolean publishExam(ServletContext context, String examId) {
        return updateExamStatus(context, examId, Exam.STATUS_PUBLISHED);
    }

    public boolean cancelExam(ServletContext context, String examId) {
        return updateExamStatus(context, examId, Exam.STATUS_CANCELLED);
    }

    public boolean deactivateExam(ServletContext context, String examId) {
        return updateExamStatus(context, examId, Exam.STATUS_INACTIVE);
    }

    public int countAllExams(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM exams");
    }

    public int countDraftExams(ServletContext context) {
        return countByStatus(context, Exam.STATUS_DRAFT);
    }

    public int countScheduledExams(ServletContext context) {
        return countByStatus(context, Exam.STATUS_SCHEDULED);
    }

    public int countActiveExams(ServletContext context) {
        return countByStatus(context, Exam.STATUS_ACTIVE);
    }

    public int countOngoingExams(ServletContext context) {
        return countByStatus(context, Exam.STATUS_ONGOING);
    }

    public int countAttemptableExams(ServletContext context) {
        String sql = "SELECT COUNT(*) FROM exams " +
                "WHERE LOWER(TRIM(status)) IN (LOWER(?), LOWER(?), LOWER(?))";

        return countByThreeParameterQuery(
                sql,
                Exam.STATUS_SCHEDULED,
                Exam.STATUS_ACTIVE,
                Exam.STATUS_ONGOING
        );
    }

    public int countCompletedExams(ServletContext context) {
        return getCompletedExams(context).size();
    }

    public int countPublishedExams(ServletContext context) {
        return countByStatus(context, Exam.STATUS_PUBLISHED);
    }

    public int countCancelledExams(ServletContext context) {
        return countByStatus(context, Exam.STATUS_CANCELLED);
    }

    public int countInactiveExams(ServletContext context) {
        return countByStatus(context, Exam.STATUS_INACTIVE);
    }

    public int countTodayExams(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM exams WHERE exam_date = CURRENT_DATE()");
    }

    public int countUpcomingExams(ServletContext context) {
        return countByQuery(
                "SELECT COUNT(*) FROM exams " +
                        "WHERE exam_date > CURRENT_DATE() " +
                        "AND LOWER(TRIM(status)) NOT IN " +
                        "(LOWER('Completed'), LOWER('Published'), LOWER('Cancelled'), LOWER('Inactive'))"
        );
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM exams WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    public double calculateTotalExamMarks(ServletContext context) {
        double total = 0.0;

        String sql = "SELECT COALESCE(SUM(total_marks), 0) AS total_marks_sum FROM exams";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                total = resultSet.getDouble("total_marks_sum");
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> calculateTotalExamMarks failed");
            e.printStackTrace();
        }

        return total;
    }

    private boolean isValidForCreate(ServletContext context, Exam exam) {
        if (!isExamObjectValid(exam)) {
            return false;
        }

        return !existsById(context, exam.getExamId());
    }

    private boolean isValidForUpdate(ServletContext context, Exam exam) {
        if (!isExamObjectValid(exam)) {
            return false;
        }

        return existsById(context, exam.getExamId());
    }

    private boolean isExamObjectValid(Exam exam) {
        return exam != null && exam.isCompleteForSave();
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> countByQuery failed");
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
            System.out.println("EXAMDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private int countByThreeParameterQuery(String sql,
                                           String first,
                                           String second,
                                           String third) {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, first);
            statement.setString(2, second);
            statement.setString(3, third);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMDAO ERROR -> countByThreeParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillExamStatement(PreparedStatement statement, Exam exam) throws SQLException {
        statement.setString(1, exam.getExamId());
        statement.setString(2, exam.getSubject());
        statement.setDate(3, Date.valueOf(exam.getExamLocalDate()));
        statement.setInt(4, exam.getDurationMinutes());
        statement.setDouble(5, exam.getTotalMarksAsDouble());
        statement.setString(6, exam.getStatus());
    }

    private Exam mapResultSetToExam(ResultSet resultSet) throws SQLException {
        Date sqlDate = resultSet.getDate("exam_date");
        String examDate = sqlDate == null ? "" : sqlDate.toLocalDate().toString();

        return new Exam(
                safe(resultSet.getString("exam_id")),
                safe(resultSet.getString("subject")),
                examDate,
                String.valueOf(resultSet.getInt("duration")),
                formatMarks(resultSet.getDouble("total_marks")),
                normalizeStatusInput(resultSet.getString("status"))
        );
    }

    private String normalizeStatusInput(String value) {
        String statusValue = safe(value);

        if (statusValue.equalsIgnoreCase(Exam.STATUS_DRAFT)) {
            return Exam.STATUS_DRAFT;
        }

        if (statusValue.equalsIgnoreCase(Exam.STATUS_SCHEDULED)) {
            return Exam.STATUS_SCHEDULED;
        }

        if (statusValue.equalsIgnoreCase(Exam.STATUS_ACTIVE)) {
            return Exam.STATUS_ACTIVE;
        }

        if (statusValue.equalsIgnoreCase(Exam.STATUS_ONGOING)) {
            return Exam.STATUS_ONGOING;
        }

        if (statusValue.equalsIgnoreCase(Exam.STATUS_COMPLETED)) {
            return Exam.STATUS_COMPLETED;
        }

        if (statusValue.equalsIgnoreCase(Exam.STATUS_PUBLISHED)) {
            return Exam.STATUS_PUBLISHED;
        }

        if (statusValue.equalsIgnoreCase(Exam.STATUS_CANCELLED)) {
            return Exam.STATUS_CANCELLED;
        }

        if (statusValue.equalsIgnoreCase(Exam.STATUS_INACTIVE)) {
            return Exam.STATUS_INACTIVE;
        }

        return statusValue;
    }

    private String formatMarks(double marks) {
        if (marks == Math.floor(marks)) {
            return String.valueOf((int) marks);
        }

        return String.format("%.2f", marks);
    }

    private Comparator<Exam> examDateComparator() {
        return Comparator
                .comparing(
                        (Exam exam) -> {
                            LocalDate date = exam.getExamLocalDate();
                            return date == null ? LocalDate.MAX : date;
                        }
                )
                .thenComparing(Exam::getExamId, String.CASE_INSENSITIVE_ORDER);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}