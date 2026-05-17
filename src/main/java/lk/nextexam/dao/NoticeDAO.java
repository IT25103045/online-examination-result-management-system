package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Notice;

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
 * Professional MySQL DAO for notice management.
 *
 * MySQL table:
 * notices
 *
 * Columns:
 * notice_id, title, description, notice_date, target_group, priority, status
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class NoticeDAO {

    public List<Notice> getAllNotices(ServletContext context) {
        List<Notice> notices = new ArrayList<>();

        String sql = "SELECT notice_id, title, description, notice_date, target_group, priority, status " +
                "FROM notices " +
                "ORDER BY notice_date DESC, notice_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                notices.add(mapResultSetToNotice(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> getAllNotices failed");
            e.printStackTrace();
        }

        notices.sort(noticeComparator());
        return notices;
    }

    public Notice getNoticeById(ServletContext context, String noticeId) {
        String cleanNoticeId = FileUtil.clean(noticeId);

        if (cleanNoticeId.isEmpty()) {
            return null;
        }

        String sql = "SELECT notice_id, title, description, notice_date, target_group, priority, status " +
                "FROM notices " +
                "WHERE LOWER(TRIM(notice_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanNoticeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToNotice(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> getNoticeById failed for " + cleanNoticeId);
            e.printStackTrace();
        }

        return null;
    }

    public List<Notice> getPublishedNotices(ServletContext context) {
        return getNoticesByStatus(context, Notice.STATUS_PUBLISHED);
    }

    public List<Notice> getVisibleNoticesForRole(ServletContext context, String role) {
        List<Notice> visibleNotices = new ArrayList<>();
        String cleanRole = normalizeTargetGroupInput(role);

        if (cleanRole.isEmpty()) {
            return visibleNotices;
        }

        String sql = "SELECT notice_id, title, description, notice_date, target_group, priority, status " +
                "FROM notices " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "AND (LOWER(TRIM(target_group)) = LOWER(TRIM(?)) " +
                "OR LOWER(TRIM(target_group)) = LOWER(TRIM(?))) " +
                "ORDER BY notice_date DESC, notice_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Notice.STATUS_PUBLISHED);
            statement.setString(2, cleanRole);
            statement.setString(3, Notice.TARGET_ALL);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    visibleNotices.add(mapResultSetToNotice(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> getVisibleNoticesForRole failed for " + cleanRole);
            e.printStackTrace();
        }

        visibleNotices.sort(noticeComparator());
        return visibleNotices;
    }

    public List<Notice> getNoticesByTargetGroup(ServletContext context, String targetGroup) {
        List<Notice> selectedNotices = new ArrayList<>();
        String cleanTargetGroup = normalizeTargetGroupInput(targetGroup);

        if (cleanTargetGroup.isEmpty()) {
            return selectedNotices;
        }

        String sql = "SELECT notice_id, title, description, notice_date, target_group, priority, status " +
                "FROM notices " +
                "WHERE LOWER(TRIM(target_group)) = LOWER(TRIM(?)) " +
                "ORDER BY notice_date DESC, notice_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanTargetGroup);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedNotices.add(mapResultSetToNotice(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> getNoticesByTargetGroup failed for " + cleanTargetGroup);
            e.printStackTrace();
        }

        selectedNotices.sort(noticeComparator());
        return selectedNotices;
    }

    public List<Notice> getNoticesByPriority(ServletContext context, String priority) {
        List<Notice> selectedNotices = new ArrayList<>();
        String cleanPriority = normalizePriorityInput(priority);

        if (cleanPriority.isEmpty()) {
            return selectedNotices;
        }

        String sql = "SELECT notice_id, title, description, notice_date, target_group, priority, status " +
                "FROM notices " +
                "WHERE LOWER(TRIM(priority)) = LOWER(TRIM(?)) " +
                "ORDER BY notice_date DESC, notice_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanPriority);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedNotices.add(mapResultSetToNotice(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> getNoticesByPriority failed for " + cleanPriority);
            e.printStackTrace();
        }

        selectedNotices.sort(noticeComparator());
        return selectedNotices;
    }

    public List<Notice> getNoticesByStatus(ServletContext context, String status) {
        List<Notice> selectedNotices = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return selectedNotices;
        }

        String sql = "SELECT notice_id, title, description, notice_date, target_group, priority, status " +
                "FROM notices " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY notice_date DESC, notice_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedNotices.add(mapResultSetToNotice(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> getNoticesByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        selectedNotices.sort(noticeComparator());
        return selectedNotices;
    }

    public List<Notice> getUrgentPublishedNoticesForRole(ServletContext context, String role) {
        List<Notice> urgentNotices = new ArrayList<>();
        String cleanRole = normalizeTargetGroupInput(role);

        if (cleanRole.isEmpty()) {
            return urgentNotices;
        }

        String sql = "SELECT notice_id, title, description, notice_date, target_group, priority, status " +
                "FROM notices " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(priority)) = LOWER(TRIM(?)) " +
                "AND (LOWER(TRIM(target_group)) = LOWER(TRIM(?)) " +
                "OR LOWER(TRIM(target_group)) = LOWER(TRIM(?))) " +
                "ORDER BY notice_date DESC, notice_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Notice.STATUS_PUBLISHED);
            statement.setString(2, Notice.PRIORITY_URGENT);
            statement.setString(3, cleanRole);
            statement.setString(4, Notice.TARGET_ALL);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    urgentNotices.add(mapResultSetToNotice(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> getUrgentPublishedNoticesForRole failed for " + cleanRole);
            e.printStackTrace();
        }

        urgentNotices.sort(noticeComparator());
        return urgentNotices;
    }

    public boolean addNotice(ServletContext context, Notice notice) {
        if (!isValidForCreate(context, notice)) {
            return false;
        }

        String sql = "INSERT INTO notices " +
                "(notice_id, title, description, notice_date, target_group, priority, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillNoticeStatement(statement, notice);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> addNotice failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateNotice(ServletContext context, Notice notice) {
        if (!isValidForUpdate(context, notice)) {
            return false;
        }

        String sql = "UPDATE notices SET " +
                "title = ?, " +
                "description = ?, " +
                "notice_date = ?, " +
                "target_group = ?, " +
                "priority = ?, " +
                "status = ? " +
                "WHERE LOWER(TRIM(notice_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getDescription());
            statement.setDate(3, Date.valueOf(notice.getNoticeLocalDate()));
            statement.setString(4, notice.getTargetGroup());
            statement.setString(5, notice.getPriority());
            statement.setString(6, notice.getStatus());
            statement.setString(7, notice.getNoticeId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> updateNotice failed for " +
                    (notice != null ? notice.getNoticeId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNotice(ServletContext context, String noticeId) {
        String cleanNoticeId = FileUtil.clean(noticeId);

        if (cleanNoticeId.isEmpty()) {
            return false;
        }

        Notice notice = getNoticeById(context, cleanNoticeId);

        if (notice == null) {
            return false;
        }

        /*
         * Professional rule:
         * Published notices should not be physically deleted.
         * Archive them instead to preserve communication history.
         */
        if (notice.isPublished()) {
            return false;
        }

        String sql = "DELETE FROM notices WHERE LOWER(TRIM(notice_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanNoticeId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> deleteNotice failed for " + cleanNoticeId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean publishNotice(ServletContext context, String noticeId) {
        return updateNoticeStatus(context, noticeId, Notice.STATUS_PUBLISHED);
    }

    public boolean archiveNotice(ServletContext context, String noticeId) {
        return updateNoticeStatus(context, noticeId, Notice.STATUS_ARCHIVED);
    }

    public boolean setNoticeDraft(ServletContext context, String noticeId) {
        return updateNoticeStatus(context, noticeId, Notice.STATUS_DRAFT);
    }

    public boolean updateNoticeStatus(ServletContext context, String noticeId, String status) {
        String cleanNoticeId = FileUtil.clean(noticeId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanNoticeId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        Notice notice = getNoticeById(context, cleanNoticeId);

        if (notice == null) {
            return false;
        }

        notice.setStatus(cleanStatus);

        if (!notice.isValidStatus()) {
            return false;
        }

        String sql = "UPDATE notices SET status = ? " +
                "WHERE LOWER(TRIM(notice_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);
            statement.setString(2, cleanNoticeId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> updateNoticeStatus failed for " + cleanNoticeId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsById(ServletContext context, String noticeId) {
        String cleanNoticeId = FileUtil.clean(noticeId);

        if (cleanNoticeId.isEmpty()) {
            return false;
        }

        String sql = "SELECT notice_id FROM notices " +
                "WHERE LOWER(TRIM(notice_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanNoticeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> existsById failed for " + cleanNoticeId);
            e.printStackTrace();
            return false;
        }
    }

    public int countAllNotices(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM notices");
    }

    public int countPublishedNotices(ServletContext context) {
        return countByStatus(context, Notice.STATUS_PUBLISHED);
    }

    public int countDraftNotices(ServletContext context) {
        return countByStatus(context, Notice.STATUS_DRAFT);
    }

    public int countArchivedNotices(ServletContext context) {
        return countByStatus(context, Notice.STATUS_ARCHIVED);
    }

    public int countUrgentNotices(ServletContext context) {
        return countByPriority(context, Notice.PRIORITY_URGENT);
    }

    public int countVisibleNoticesForRole(ServletContext context, String role) {
        String cleanRole = normalizeTargetGroupInput(role);

        if (cleanRole.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM notices " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "AND (LOWER(TRIM(target_group)) = LOWER(TRIM(?)) " +
                "OR LOWER(TRIM(target_group)) = LOWER(TRIM(?)))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Notice.STATUS_PUBLISHED);
            statement.setString(2, cleanRole);
            statement.setString(3, Notice.TARGET_ALL);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> countVisibleNoticesForRole failed");
            e.printStackTrace();
        }

        return 0;
    }

    private boolean isValidForCreate(ServletContext context, Notice notice) {
        if (!isNoticeObjectValid(notice)) {
            return false;
        }

        return !existsById(context, notice.getNoticeId());
    }

    private boolean isValidForUpdate(ServletContext context, Notice notice) {
        if (!isNoticeObjectValid(notice)) {
            return false;
        }

        return existsById(context, notice.getNoticeId());
    }

    private boolean isNoticeObjectValid(Notice notice) {
        return notice != null && notice.isCompleteForSave();
    }

    private int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM notices WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    private int countByPriority(ServletContext context, String priority) {
        String cleanPriority = normalizePriorityInput(priority);

        if (cleanPriority.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM notices WHERE LOWER(TRIM(priority)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanPriority);
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("NOTICEDAO ERROR -> countByQuery failed");
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
            System.out.println("NOTICEDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillNoticeStatement(PreparedStatement statement, Notice notice) throws SQLException {
        statement.setString(1, notice.getNoticeId());
        statement.setString(2, notice.getTitle());
        statement.setString(3, notice.getDescription());
        statement.setDate(4, Date.valueOf(notice.getNoticeLocalDate()));
        statement.setString(5, notice.getTargetGroup());
        statement.setString(6, notice.getPriority());
        statement.setString(7, notice.getStatus());
    }

    private Notice mapResultSetToNotice(ResultSet resultSet) throws SQLException {
        Date sqlDate = resultSet.getDate("notice_date");
        String noticeDate = sqlDate == null ? "" : sqlDate.toLocalDate().toString();

        return new Notice(
                safe(resultSet.getString("notice_id")),
                safe(resultSet.getString("title")),
                safe(resultSet.getString("description")),
                noticeDate,
                normalizeTargetGroupInput(resultSet.getString("target_group")),
                normalizePriorityInput(resultSet.getString("priority")),
                normalizeStatusInput(resultSet.getString("status"))
        );
    }

    private String normalizeTargetGroupInput(String value) {
        String targetGroup = safe(value);

        if (targetGroup.equalsIgnoreCase(Notice.TARGET_ALL)) {
            return Notice.TARGET_ALL;
        }

        if (targetGroup.equalsIgnoreCase(Notice.TARGET_ADMIN)) {
            return Notice.TARGET_ADMIN;
        }

        if (targetGroup.equalsIgnoreCase(Notice.TARGET_LECTURER)) {
            return Notice.TARGET_LECTURER;
        }

        if (targetGroup.equalsIgnoreCase(Notice.TARGET_STUDENT)) {
            return Notice.TARGET_STUDENT;
        }

        return targetGroup;
    }

    private String normalizePriorityInput(String value) {
        String priority = safe(value);

        if (priority.equalsIgnoreCase(Notice.PRIORITY_LOW)) {
            return Notice.PRIORITY_LOW;
        }

        if (priority.equalsIgnoreCase(Notice.PRIORITY_NORMAL)) {
            return Notice.PRIORITY_NORMAL;
        }

        if (priority.equalsIgnoreCase(Notice.PRIORITY_HIGH)) {
            return Notice.PRIORITY_HIGH;
        }

        if (priority.equalsIgnoreCase(Notice.PRIORITY_URGENT)) {
            return Notice.PRIORITY_URGENT;
        }

        return priority;
    }

    private String normalizeStatusInput(String value) {
        String status = safe(value);

        if (status.equalsIgnoreCase(Notice.STATUS_DRAFT)) {
            return Notice.STATUS_DRAFT;
        }

        if (status.equalsIgnoreCase(Notice.STATUS_PUBLISHED)) {
            return Notice.STATUS_PUBLISHED;
        }

        if (status.equalsIgnoreCase(Notice.STATUS_ARCHIVED)) {
            return Notice.STATUS_ARCHIVED;
        }

        return status;
    }

    private Comparator<Notice> noticeComparator() {
        return Comparator
                .comparing(
                        (Notice notice) -> {
                            LocalDate noticeDate = notice.getNoticeLocalDate();
                            return noticeDate == null ? LocalDate.MIN : noticeDate;
                        }
                )
                .reversed()
                .thenComparing(Notice::getNoticeId, String.CASE_INSENSITIVE_ORDER);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}