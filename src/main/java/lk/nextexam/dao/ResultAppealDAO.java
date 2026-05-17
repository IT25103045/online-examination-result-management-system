package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ResultAppeal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultAppealDAO manages MySQL result appeal/recheck request records.
 *
 * MySQL table:
 * result_appeals
 *
 * Columns:
 * appeal_id, result_id, exam_id, student_id, student_name,
 * reason_type, message, status, staff_reply, created_at, updated_at, reviewed_by
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ResultAppealDAO {

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<ResultAppeal> getAllAppeals(ServletContext context) {
        List<ResultAppeal> appeals = new ArrayList<>();

        String sql = "SELECT appeal_id, result_id, exam_id, student_id, student_name, " +
                "reason_type, message, status, staff_reply, created_at, updated_at, reviewed_by " +
                "FROM result_appeals " +
                "ORDER BY created_at DESC, appeal_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                appeals.add(mapResultSetToAppeal(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> getAllAppeals failed");
            e.printStackTrace();
        }

        return appeals;
    }

    public ResultAppeal getAppealById(ServletContext context, String appealId) {
        String cleanAppealId = FileUtil.clean(appealId);

        if (cleanAppealId.isEmpty()) {
            return null;
        }

        String sql = "SELECT appeal_id, result_id, exam_id, student_id, student_name, " +
                "reason_type, message, status, staff_reply, created_at, updated_at, reviewed_by " +
                "FROM result_appeals " +
                "WHERE LOWER(TRIM(appeal_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanAppealId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToAppeal(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> getAppealById failed for " + cleanAppealId);
            e.printStackTrace();
        }

        return null;
    }

    public List<ResultAppeal> getAppealsByStudent(ServletContext context, String studentId) {
        List<ResultAppeal> appeals = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return appeals;
        }

        String sql = "SELECT appeal_id, result_id, exam_id, student_id, student_name, " +
                "reason_type, message, status, staff_reply, created_at, updated_at, reviewed_by " +
                "FROM result_appeals " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, appeal_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    appeals.add(mapResultSetToAppeal(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> getAppealsByStudent failed for " + cleanStudentId);
            e.printStackTrace();
        }

        return appeals;
    }

    public List<ResultAppeal> getAppealsByStatus(ServletContext context, String status) {
        List<ResultAppeal> appeals = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return appeals;
        }

        String sql = "SELECT appeal_id, result_id, exam_id, student_id, student_name, " +
                "reason_type, message, status, staff_reply, created_at, updated_at, reviewed_by " +
                "FROM result_appeals " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, appeal_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    appeals.add(mapResultSetToAppeal(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> getAppealsByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        return appeals;
    }

    public ResultAppeal getAppealByStudentAndResult(ServletContext context,
                                                    String studentId,
                                                    String resultId) {
        String cleanStudentId = FileUtil.clean(studentId);
        String cleanResultId = FileUtil.clean(resultId);

        if (cleanStudentId.isEmpty() || cleanResultId.isEmpty()) {
            return null;
        }

        String sql = "SELECT appeal_id, result_id, exam_id, student_id, student_name, " +
                "reason_type, message, status, staff_reply, created_at, updated_at, reviewed_by " +
                "FROM result_appeals " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(result_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);
            statement.setString(2, cleanResultId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToAppeal(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> getAppealByStudentAndResult failed");
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasStudentAppealedResult(ServletContext context,
                                            String studentId,
                                            String resultId) {
        return getAppealByStudentAndResult(context, studentId, resultId) != null;
    }

    public boolean addAppeal(ServletContext context, ResultAppeal appeal) {
        if (appeal == null) {
            return false;
        }

        if (appeal.getAppealId().isEmpty()) {
            appeal.setAppealId(FileUtil.generateId("RA"));
        }

        if (appeal.getStatus().isEmpty()) {
            appeal.setStatus(ResultAppeal.STATUS_PENDING);
        }

        if (appeal.getCreatedAt().isEmpty()) {
            appeal.setCreatedAt(now());
        }

        if (appeal.getUpdatedAt().isEmpty()) {
            appeal.setUpdatedAt(now());
        }

        if (!appeal.isCompleteForSave()) {
            return false;
        }

        if (existsById(appeal.getAppealId())) {
            appeal.setAppealId(FileUtil.generateId("RA"));
        }

        if (hasStudentAppealedResult(context, appeal.getStudentId(), appeal.getResultId())) {
            return false;
        }

        String sql = "INSERT INTO result_appeals " +
                "(appeal_id, result_id, exam_id, student_id, student_name, reason_type, message, status, staff_reply, created_at, updated_at, reviewed_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillAppealStatement(statement, appeal);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> addAppeal failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAppeal(ServletContext context, ResultAppeal appeal) {
        if (appeal == null || appeal.getAppealId().isEmpty()) {
            return false;
        }

        if (appeal.getUpdatedAt().isEmpty()) {
            appeal.setUpdatedAt(now());
        }

        if (!appeal.isCompleteForSave()) {
            return false;
        }

        if (!existsById(appeal.getAppealId())) {
            return false;
        }

        String sql = "UPDATE result_appeals SET " +
                "result_id = ?, " +
                "exam_id = ?, " +
                "student_id = ?, " +
                "student_name = ?, " +
                "reason_type = ?, " +
                "message = ?, " +
                "status = ?, " +
                "staff_reply = ?, " +
                "created_at = ?, " +
                "updated_at = ?, " +
                "reviewed_by = ? " +
                "WHERE LOWER(TRIM(appeal_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, appeal.getResultId());
            statement.setString(2, appeal.getExamId());
            statement.setString(3, appeal.getStudentId());
            statement.setString(4, appeal.getStudentName());
            statement.setString(5, appeal.getReasonType());
            statement.setString(6, appeal.getMessage());
            statement.setString(7, appeal.getStatus());
            statement.setString(8, appeal.getStaffReply());
            statement.setTimestamp(9, toTimestamp(appeal.getCreatedAt()));
            statement.setTimestamp(10, toTimestamp(appeal.getUpdatedAt()));
            statement.setString(11, appeal.getReviewedBy());
            statement.setString(12, appeal.getAppealId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> updateAppeal failed for " + appeal.getAppealId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAppealStatus(ServletContext context,
                                      String appealId,
                                      String status,
                                      String staffReply,
                                      String reviewedBy) {
        String cleanAppealId = FileUtil.clean(appealId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanAppealId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        ResultAppeal appeal = getAppealById(context, cleanAppealId);

        if (appeal == null) {
            return false;
        }

        appeal.setStatus(cleanStatus);
        appeal.setStaffReply(FileUtil.clean(staffReply));
        appeal.setReviewedBy(FileUtil.clean(reviewedBy));
        appeal.setUpdatedAt(now());

        if (!appeal.isValidStatus()) {
            return false;
        }

        String sql = "UPDATE result_appeals SET " +
                "status = ?, " +
                "staff_reply = ?, " +
                "updated_at = ?, " +
                "reviewed_by = ? " +
                "WHERE LOWER(TRIM(appeal_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, appeal.getStatus());
            statement.setString(2, appeal.getStaffReply());
            statement.setTimestamp(3, toTimestamp(appeal.getUpdatedAt()));
            statement.setString(4, appeal.getReviewedBy());
            statement.setString(5, cleanAppealId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> updateAppealStatus failed for " + cleanAppealId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean markUnderReview(ServletContext context, String appealId, String reviewedBy) {
        return updateAppealStatus(
                context,
                appealId,
                ResultAppeal.STATUS_UNDER_REVIEW,
                "",
                reviewedBy
        );
    }

    public boolean resolveAppeal(ServletContext context,
                                 String appealId,
                                 String staffReply,
                                 String reviewedBy) {
        return updateAppealStatus(
                context,
                appealId,
                ResultAppeal.STATUS_RESOLVED,
                staffReply,
                reviewedBy
        );
    }

    public boolean rejectAppeal(ServletContext context,
                                String appealId,
                                String staffReply,
                                String reviewedBy) {
        return updateAppealStatus(
                context,
                appealId,
                ResultAppeal.STATUS_REJECTED,
                staffReply,
                reviewedBy
        );
    }

    public int countAll(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM result_appeals");
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM result_appeals WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    public int countPending(ServletContext context) {
        return countByStatus(context, ResultAppeal.STATUS_PENDING);
    }

    public int countUnderReview(ServletContext context) {
        return countByStatus(context, ResultAppeal.STATUS_UNDER_REVIEW);
    }

    public int countResolved(ServletContext context) {
        return countByStatus(context, ResultAppeal.STATUS_RESOLVED);
    }

    public int countRejected(ServletContext context) {
        return countByStatus(context, ResultAppeal.STATUS_REJECTED);
    }

    public boolean existsById(String appealId) {
        String cleanAppealId = FileUtil.clean(appealId);

        if (cleanAppealId.isEmpty()) {
            return false;
        }

        String sql = "SELECT appeal_id FROM result_appeals " +
                "WHERE LOWER(TRIM(appeal_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanAppealId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> existsById failed for " + cleanAppealId);
            e.printStackTrace();
            return false;
        }
    }

    public String now() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME);
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("RESULTAPPEALDAO ERROR -> countByQuery failed");
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
            System.out.println("RESULTAPPEALDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillAppealStatement(PreparedStatement statement,
                                     ResultAppeal appeal) throws SQLException {

        statement.setString(1, appeal.getAppealId());
        statement.setString(2, appeal.getResultId());
        statement.setString(3, appeal.getExamId());
        statement.setString(4, appeal.getStudentId());
        statement.setString(5, appeal.getStudentName());
        statement.setString(6, appeal.getReasonType());
        statement.setString(7, appeal.getMessage());
        statement.setString(8, appeal.getStatus());
        statement.setString(9, appeal.getStaffReply());
        statement.setTimestamp(10, toTimestamp(appeal.getCreatedAt()));
        statement.setTimestamp(11, toTimestamp(appeal.getUpdatedAt()));
        statement.setString(12, appeal.getReviewedBy());
    }

    private ResultAppeal mapResultSetToAppeal(ResultSet resultSet) throws SQLException {
        return new ResultAppeal(
                safe(resultSet.getString("appeal_id")),
                safe(resultSet.getString("result_id")),
                safe(resultSet.getString("exam_id")),
                safe(resultSet.getString("student_id")),
                safe(resultSet.getString("student_name")),
                normalizeReasonInput(resultSet.getString("reason_type")),
                safe(resultSet.getString("message")),
                normalizeStatusInput(resultSet.getString("status")),
                safe(resultSet.getString("staff_reply")),
                fromTimestamp(resultSet.getTimestamp("created_at")),
                fromTimestamp(resultSet.getTimestamp("updated_at")),
                safe(resultSet.getString("reviewed_by"))
        );
    }

    private Timestamp toTimestamp(String value) {
        LocalDateTime dateTime = parseDateTime(value);

        if (dateTime == null) {
            dateTime = LocalDateTime.now();
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

        if (cleanValue.isEmpty()) {
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

        if (statusValue.isEmpty()) {
            return ResultAppeal.STATUS_PENDING;
        }

        if (statusValue.equalsIgnoreCase(ResultAppeal.STATUS_PENDING)) {
            return ResultAppeal.STATUS_PENDING;
        }

        if (statusValue.equalsIgnoreCase(ResultAppeal.STATUS_UNDER_REVIEW)) {
            return ResultAppeal.STATUS_UNDER_REVIEW;
        }

        if (statusValue.equalsIgnoreCase(ResultAppeal.STATUS_RESOLVED)) {
            return ResultAppeal.STATUS_RESOLVED;
        }

        if (statusValue.equalsIgnoreCase(ResultAppeal.STATUS_REJECTED)) {
            return ResultAppeal.STATUS_REJECTED;
        }

        return statusValue;
    }

    private String normalizeReasonInput(String value) {
        String reasonValue = safe(value);

        if (reasonValue.isEmpty()) {
            return ResultAppeal.REASON_MARK_RECHECK;
        }

        if (reasonValue.equalsIgnoreCase(ResultAppeal.REASON_MARK_RECHECK)) {
            return ResultAppeal.REASON_MARK_RECHECK;
        }

        if (reasonValue.equalsIgnoreCase(ResultAppeal.REASON_MISSING_MARKS)) {
            return ResultAppeal.REASON_MISSING_MARKS;
        }

        if (reasonValue.equalsIgnoreCase(ResultAppeal.REASON_WRONG_RESULT)) {
            return ResultAppeal.REASON_WRONG_RESULT;
        }

        if (reasonValue.equalsIgnoreCase(ResultAppeal.REASON_ESSAY_REVIEW)) {
            return ResultAppeal.REASON_ESSAY_REVIEW;
        }

        if (reasonValue.equalsIgnoreCase(ResultAppeal.REASON_TECHNICAL_ISSUE)) {
            return ResultAppeal.REASON_TECHNICAL_ISSUE;
        }

        if (reasonValue.equalsIgnoreCase(ResultAppeal.REASON_OTHER)) {
            return ResultAppeal.REASON_OTHER;
        }

        return reasonValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}