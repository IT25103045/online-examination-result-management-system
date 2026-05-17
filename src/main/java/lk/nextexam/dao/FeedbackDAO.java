package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Feedback;

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
 * Professional MySQL DAO for feedback management.
 *
 * MySQL table:
 * feedback
 *
 * Columns:
 * feedback_id, student_id, category, message, feedback_date, status
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class FeedbackDAO {

    public List<Feedback> getAllFeedback(ServletContext context) {
        List<Feedback> feedbackList = new ArrayList<>();

        String sql = "SELECT feedback_id, student_id, category, message, feedback_date, status " +
                "FROM feedback " +
                "ORDER BY feedback_date DESC, feedback_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                feedbackList.add(mapResultSetToFeedback(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> getAllFeedback failed");
            e.printStackTrace();
        }

        feedbackList.sort(feedbackComparator());
        return feedbackList;
    }

    public Feedback getFeedbackById(ServletContext context, String feedbackId) {
        String cleanFeedbackId = FileUtil.clean(feedbackId);

        if (cleanFeedbackId.isEmpty()) {
            return null;
        }

        String sql = "SELECT feedback_id, student_id, category, message, feedback_date, status " +
                "FROM feedback " +
                "WHERE LOWER(TRIM(feedback_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanFeedbackId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToFeedback(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> getFeedbackById failed for " + cleanFeedbackId);
            e.printStackTrace();
        }

        return null;
    }

    public List<Feedback> getFeedbackByStudentId(ServletContext context, String studentId) {
        List<Feedback> selectedFeedback = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedFeedback;
        }

        String sql = "SELECT feedback_id, student_id, category, message, feedback_date, status " +
                "FROM feedback " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "ORDER BY feedback_date DESC, feedback_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedFeedback.add(mapResultSetToFeedback(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> getFeedbackByStudentId failed for " + cleanStudentId);
            e.printStackTrace();
        }

        selectedFeedback.sort(feedbackComparator());
        return selectedFeedback;
    }

    public List<Feedback> getFeedbackByCategory(ServletContext context, String category) {
        List<Feedback> selectedFeedback = new ArrayList<>();
        String cleanCategory = normalizeCategoryInput(category);

        if (cleanCategory.isEmpty()) {
            return selectedFeedback;
        }

        String sql = "SELECT feedback_id, student_id, category, message, feedback_date, status " +
                "FROM feedback " +
                "WHERE LOWER(TRIM(category)) = LOWER(TRIM(?)) " +
                "ORDER BY feedback_date DESC, feedback_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanCategory);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedFeedback.add(mapResultSetToFeedback(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> getFeedbackByCategory failed for " + cleanCategory);
            e.printStackTrace();
        }

        selectedFeedback.sort(feedbackComparator());
        return selectedFeedback;
    }

    public List<Feedback> getFeedbackByStatus(ServletContext context, String status) {
        List<Feedback> selectedFeedback = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return selectedFeedback;
        }

        String sql = "SELECT feedback_id, student_id, category, message, feedback_date, status " +
                "FROM feedback " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY feedback_date DESC, feedback_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedFeedback.add(mapResultSetToFeedback(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> getFeedbackByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        selectedFeedback.sort(feedbackComparator());
        return selectedFeedback;
    }

    public List<Feedback> getOpenFeedback(ServletContext context) {
        List<Feedback> openFeedback = new ArrayList<>();

        String sql = "SELECT feedback_id, student_id, category, message, feedback_date, status " +
                "FROM feedback " +
                "WHERE LOWER(TRIM(status)) IN (LOWER(?), LOWER(?)) " +
                "ORDER BY feedback_date DESC, feedback_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Feedback.STATUS_NEW);
            statement.setString(2, Feedback.STATUS_IN_REVIEW);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    openFeedback.add(mapResultSetToFeedback(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> getOpenFeedback failed");
            e.printStackTrace();
        }

        openFeedback.sort(feedbackComparator());
        return openFeedback;
    }

    public List<Feedback> getCompletedFeedback(ServletContext context) {
        List<Feedback> completedFeedback = new ArrayList<>();

        String sql = "SELECT feedback_id, student_id, category, message, feedback_date, status " +
                "FROM feedback " +
                "WHERE LOWER(TRIM(status)) IN (LOWER(?), LOWER(?)) " +
                "ORDER BY feedback_date DESC, feedback_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Feedback.STATUS_RESOLVED);
            statement.setString(2, Feedback.STATUS_CLOSED);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    completedFeedback.add(mapResultSetToFeedback(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> getCompletedFeedback failed");
            e.printStackTrace();
        }

        completedFeedback.sort(feedbackComparator());
        return completedFeedback;
    }

    public List<Feedback> getTodayFeedback(ServletContext context) {
        List<Feedback> todayFeedback = new ArrayList<>();

        String sql = "SELECT feedback_id, student_id, category, message, feedback_date, status " +
                "FROM feedback " +
                "WHERE feedback_date = CURRENT_DATE() " +
                "ORDER BY feedback_date DESC, feedback_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                todayFeedback.add(mapResultSetToFeedback(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> getTodayFeedback failed");
            e.printStackTrace();
        }

        todayFeedback.sort(feedbackComparator());
        return todayFeedback;
    }

    public boolean addFeedback(ServletContext context, Feedback feedback) {
        if (feedback == null) {
            return false;
        }

        if (feedback.getFeedbackId().isEmpty()) {
            feedback.setFeedbackId(FileUtil.generateId("FB"));
        }

        if (feedback.getDate().isEmpty()) {
            feedback.setDate(LocalDate.now().toString());
        }

        if (feedback.getStatus().isEmpty()) {
            feedback.setStatus(Feedback.STATUS_NEW);
        }

        if (!isValidForCreate(context, feedback)) {
            return false;
        }

        String sql = "INSERT INTO feedback " +
                "(feedback_id, student_id, category, message, feedback_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillFeedbackStatement(statement, feedback);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> addFeedback failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFeedback(ServletContext context, Feedback feedback) {
        if (!isValidForUpdate(context, feedback)) {
            return false;
        }

        String sql = "UPDATE feedback SET " +
                "student_id = ?, " +
                "category = ?, " +
                "message = ?, " +
                "feedback_date = ?, " +
                "status = ? " +
                "WHERE LOWER(TRIM(feedback_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, feedback.getStudentId());
            statement.setString(2, feedback.getCategory());
            statement.setString(3, feedback.getMessage());
            statement.setDate(4, Date.valueOf(feedback.getFeedbackLocalDate()));
            statement.setString(5, feedback.getStatus());
            statement.setString(6, feedback.getFeedbackId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> updateFeedback failed for " +
                    (feedback != null ? feedback.getFeedbackId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFeedback(ServletContext context, String feedbackId) {
        String cleanFeedbackId = FileUtil.clean(feedbackId);

        if (cleanFeedbackId.isEmpty()) {
            return false;
        }

        Feedback feedback = getFeedbackById(context, cleanFeedbackId);

        if (feedback == null) {
            return false;
        }

        /*
         * Professional rule:
         * Resolved and Closed feedback should remain as history.
         * Only New or In Review feedback can be physically deleted.
         */
        if (feedback.isCompleted()) {
            return false;
        }

        String sql = "DELETE FROM feedback WHERE LOWER(TRIM(feedback_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanFeedbackId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> deleteFeedback failed for " + cleanFeedbackId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFeedbackStatus(ServletContext context, String feedbackId, String status) {
        String cleanFeedbackId = FileUtil.clean(feedbackId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanFeedbackId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        Feedback feedback = getFeedbackById(context, cleanFeedbackId);

        if (feedback == null) {
            return false;
        }

        feedback.setStatus(cleanStatus);

        if (!feedback.isValidStatus()) {
            return false;
        }

        String sql = "UPDATE feedback SET status = ? " +
                "WHERE LOWER(TRIM(feedback_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);
            statement.setString(2, cleanFeedbackId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> updateFeedbackStatus failed for " + cleanFeedbackId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean markInReview(ServletContext context, String feedbackId) {
        return updateFeedbackStatus(context, feedbackId, Feedback.STATUS_IN_REVIEW);
    }

    public boolean markResolved(ServletContext context, String feedbackId) {
        return updateFeedbackStatus(context, feedbackId, Feedback.STATUS_RESOLVED);
    }

    public boolean closeFeedback(ServletContext context, String feedbackId) {
        return updateFeedbackStatus(context, feedbackId, Feedback.STATUS_CLOSED);
    }

    public boolean reopenFeedback(ServletContext context, String feedbackId) {
        return updateFeedbackStatus(context, feedbackId, Feedback.STATUS_NEW);
    }

    public boolean existsById(ServletContext context, String feedbackId) {
        String cleanFeedbackId = FileUtil.clean(feedbackId);

        if (cleanFeedbackId.isEmpty()) {
            return false;
        }

        String sql = "SELECT feedback_id FROM feedback " +
                "WHERE LOWER(TRIM(feedback_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanFeedbackId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> existsById failed for " + cleanFeedbackId);
            e.printStackTrace();
            return false;
        }
    }

    public int countAllFeedback(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM feedback");
    }

    public int countFeedbackByStudentId(ServletContext context, String studentId) {
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM feedback WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStudentId);
    }

    public int countNewFeedback(ServletContext context) {
        return countByStatus(context, Feedback.STATUS_NEW);
    }

    public int countInReviewFeedback(ServletContext context) {
        return countByStatus(context, Feedback.STATUS_IN_REVIEW);
    }

    public int countResolvedFeedback(ServletContext context) {
        return countByStatus(context, Feedback.STATUS_RESOLVED);
    }

    public int countClosedFeedback(ServletContext context) {
        return countByStatus(context, Feedback.STATUS_CLOSED);
    }

    public int countOpenFeedback(ServletContext context) {
        String sql = "SELECT COUNT(*) FROM feedback " +
                "WHERE LOWER(TRIM(status)) IN (LOWER(?), LOWER(?))";
        return countByTwoParameterQuery(sql, Feedback.STATUS_NEW, Feedback.STATUS_IN_REVIEW);
    }

    public int countCompletedFeedback(ServletContext context) {
        String sql = "SELECT COUNT(*) FROM feedback " +
                "WHERE LOWER(TRIM(status)) IN (LOWER(?), LOWER(?))";
        return countByTwoParameterQuery(sql, Feedback.STATUS_RESOLVED, Feedback.STATUS_CLOSED);
    }

    public int countTodayFeedback(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM feedback WHERE feedback_date = CURRENT_DATE()");
    }

    public int countTechnicalFeedback(ServletContext context) {
        return countByCategory(context, Feedback.CATEGORY_TECHNICAL);
    }

    public int countExamFeedback(ServletContext context) {
        return countByCategory(context, Feedback.CATEGORY_EXAM);
    }

    public int countResultFeedback(ServletContext context) {
        return countByCategory(context, Feedback.CATEGORY_RESULT);
    }

    private boolean isValidForCreate(ServletContext context, Feedback feedback) {
        if (!isFeedbackObjectValid(feedback)) {
            return false;
        }

        return !existsById(context, feedback.getFeedbackId());
    }

    private boolean isValidForUpdate(ServletContext context, Feedback feedback) {
        if (!isFeedbackObjectValid(feedback)) {
            return false;
        }

        return existsById(context, feedback.getFeedbackId());
    }

    private boolean isFeedbackObjectValid(Feedback feedback) {
        return feedback != null && feedback.isCompleteForSave();
    }

    private int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM feedback WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    private int countByCategory(ServletContext context, String category) {
        String cleanCategory = normalizeCategoryInput(category);

        if (cleanCategory.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM feedback WHERE LOWER(TRIM(category)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanCategory);
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> countByQuery failed");
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
            System.out.println("FEEDBACKDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private int countByTwoParameterQuery(String sql, String first, String second) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, first);
            statement.setString(2, second);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("FEEDBACKDAO ERROR -> countByTwoParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillFeedbackStatement(PreparedStatement statement, Feedback feedback) throws SQLException {
        statement.setString(1, feedback.getFeedbackId());
        statement.setString(2, feedback.getStudentId());
        statement.setString(3, feedback.getCategory());
        statement.setString(4, feedback.getMessage());
        statement.setDate(5, Date.valueOf(feedback.getFeedbackLocalDate()));
        statement.setString(6, feedback.getStatus());
    }

    private Feedback mapResultSetToFeedback(ResultSet resultSet) throws SQLException {
        Date sqlDate = resultSet.getDate("feedback_date");
        String feedbackDate = sqlDate == null ? "" : sqlDate.toLocalDate().toString();

        return new Feedback(
                safe(resultSet.getString("feedback_id")),
                safe(resultSet.getString("student_id")),
                normalizeCategoryInput(resultSet.getString("category")),
                safe(resultSet.getString("message")),
                feedbackDate,
                normalizeStatusInput(resultSet.getString("status"))
        );
    }

    private String normalizeCategoryInput(String value) {
        String category = safe(value);

        if (category.equalsIgnoreCase(Feedback.CATEGORY_EXAM)) {
            return Feedback.CATEGORY_EXAM;
        }

        if (category.equalsIgnoreCase(Feedback.CATEGORY_RESULT)) {
            return Feedback.CATEGORY_RESULT;
        }

        if (category.equalsIgnoreCase(Feedback.CATEGORY_TECHNICAL)) {
            return Feedback.CATEGORY_TECHNICAL;
        }

        if (category.equalsIgnoreCase(Feedback.CATEGORY_ACCOUNT)) {
            return Feedback.CATEGORY_ACCOUNT;
        }

        if (category.equalsIgnoreCase(Feedback.CATEGORY_GENERAL)) {
            return Feedback.CATEGORY_GENERAL;
        }

        return category;
    }

    private String normalizeStatusInput(String value) {
        String status = safe(value);

        if (status.equalsIgnoreCase(Feedback.STATUS_NEW)) {
            return Feedback.STATUS_NEW;
        }

        if (status.equalsIgnoreCase(Feedback.STATUS_IN_REVIEW)
                || status.equalsIgnoreCase("InReview")
                || status.equalsIgnoreCase("Review")) {
            return Feedback.STATUS_IN_REVIEW;
        }

        if (status.equalsIgnoreCase(Feedback.STATUS_RESOLVED)) {
            return Feedback.STATUS_RESOLVED;
        }

        if (status.equalsIgnoreCase(Feedback.STATUS_CLOSED)) {
            return Feedback.STATUS_CLOSED;
        }

        return status;
    }

    private Comparator<Feedback> feedbackComparator() {
        return Comparator
                .comparing(
                        (Feedback feedback) -> {
                            LocalDate feedbackDate = feedback.getFeedbackLocalDate();
                            return feedbackDate == null ? LocalDate.MIN : feedbackDate;
                        }
                )
                .reversed()
                .thenComparing(Feedback::getFeedbackId, String.CASE_INSENSITIVE_ORDER);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}