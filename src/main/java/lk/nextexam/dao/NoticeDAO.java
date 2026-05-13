package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Notice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional DAO for notice management.
 *
 * Storage file:
 * notices.txt
 *
 * Format:
 * noticeId|title|description|noticeDate|targetGroup|priority|status
 */
public class NoticeDAO {

    private static final String FILE_NAME = "notices.txt";

    public List<Notice> getAllNotices(ServletContext context) {
        List<Notice> notices = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Notice notice = Notice.fromFileString(line);

            if (notice != null && !notice.getNoticeId().isEmpty()) {
                notices.add(notice);
            }
        }

        notices.sort(noticeComparator());
        return notices;
    }

    public Notice getNoticeById(ServletContext context, String noticeId) {
        String cleanNoticeId = FileUtil.clean(noticeId);

        if (cleanNoticeId.isEmpty()) {
            return null;
        }

        for (Notice notice : getAllNotices(context)) {
            if (notice.getNoticeId().equalsIgnoreCase(cleanNoticeId)) {
                return notice;
            }
        }

        return null;
    }

    public List<Notice> getPublishedNotices(ServletContext context) {
        List<Notice> publishedNotices = new ArrayList<>();

        for (Notice notice : getAllNotices(context)) {
            if (notice.isPublished()) {
                publishedNotices.add(notice);
            }
        }

        publishedNotices.sort(noticeComparator());
        return publishedNotices;
    }

    public List<Notice> getVisibleNoticesForRole(ServletContext context, String role) {
        List<Notice> visibleNotices = new ArrayList<>();
        String cleanRole = FileUtil.clean(role);

        if (cleanRole.isEmpty()) {
            return visibleNotices;
        }

        for (Notice notice : getAllNotices(context)) {
            if (notice.isPublished() && notice.isVisibleForRole(cleanRole)) {
                visibleNotices.add(notice);
            }
        }

        visibleNotices.sort(noticeComparator());
        return visibleNotices;
    }

    public List<Notice> getNoticesByTargetGroup(ServletContext context, String targetGroup) {
        List<Notice> selectedNotices = new ArrayList<>();
        String cleanTargetGroup = FileUtil.clean(targetGroup);

        if (cleanTargetGroup.isEmpty()) {
            return selectedNotices;
        }

        for (Notice notice : getAllNotices(context)) {
            if (notice.getTargetGroup().equalsIgnoreCase(cleanTargetGroup)) {
                selectedNotices.add(notice);
            }
        }

        selectedNotices.sort(noticeComparator());
        return selectedNotices;
    }

    public List<Notice> getNoticesByPriority(ServletContext context, String priority) {
        List<Notice> selectedNotices = new ArrayList<>();
        String cleanPriority = FileUtil.clean(priority);

        if (cleanPriority.isEmpty()) {
            return selectedNotices;
        }

        for (Notice notice : getAllNotices(context)) {
            if (notice.getPriority().equalsIgnoreCase(cleanPriority)) {
                selectedNotices.add(notice);
            }
        }

        selectedNotices.sort(noticeComparator());
        return selectedNotices;
    }

    public List<Notice> getNoticesByStatus(ServletContext context, String status) {
        List<Notice> selectedNotices = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedNotices;
        }

        for (Notice notice : getAllNotices(context)) {
            if (notice.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedNotices.add(notice);
            }
        }

        selectedNotices.sort(noticeComparator());
        return selectedNotices;
    }

    public List<Notice> getUrgentPublishedNoticesForRole(ServletContext context, String role) {
        List<Notice> urgentNotices = new ArrayList<>();
        String cleanRole = FileUtil.clean(role);

        if (cleanRole.isEmpty()) {
            return urgentNotices;
        }

        for (Notice notice : getVisibleNoticesForRole(context, cleanRole)) {
            if (notice.isUrgentPriority()) {
                urgentNotices.add(notice);
            }
        }

        urgentNotices.sort(noticeComparator());
        return urgentNotices;
    }

    public boolean addNotice(ServletContext context, Notice notice) {
        if (!isValidForCreate(context, notice)) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, notice.toFileString());
    }

    public boolean updateNotice(ServletContext context, Notice notice) {
        if (!isValidForUpdate(context, notice)) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                notice.getNoticeId(),
                notice.toFileString()
        );
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

        return FileUtil.deleteLineById(context, FILE_NAME, cleanNoticeId);
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
        String cleanStatus = FileUtil.clean(status);

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

        return updateNotice(context, notice);
    }

    public boolean existsById(ServletContext context, String noticeId) {
        return FileUtil.existsById(context, FILE_NAME, noticeId);
    }

    public int countAllNotices(ServletContext context) {
        return getAllNotices(context).size();
    }

    public int countPublishedNotices(ServletContext context) {
        return getNoticesByStatus(context, Notice.STATUS_PUBLISHED).size();
    }

    public int countDraftNotices(ServletContext context) {
        return getNoticesByStatus(context, Notice.STATUS_DRAFT).size();
    }

    public int countArchivedNotices(ServletContext context) {
        return getNoticesByStatus(context, Notice.STATUS_ARCHIVED).size();
    }

    public int countUrgentNotices(ServletContext context) {
        return getNoticesByPriority(context, Notice.PRIORITY_URGENT).size();
    }

    public int countVisibleNoticesForRole(ServletContext context, String role) {
        return getVisibleNoticesForRole(context, role).size();
    }

    private boolean isValidForCreate(ServletContext context, Notice notice) {
        if (!isNoticeObjectValid(notice)) {
            return false;
        }

        return !FileUtil.existsById(context, FILE_NAME, notice.getNoticeId());
    }

    private boolean isValidForUpdate(ServletContext context, Notice notice) {
        if (!isNoticeObjectValid(notice)) {
            return false;
        }

        return FileUtil.existsById(context, FILE_NAME, notice.getNoticeId());
    }

    private boolean isNoticeObjectValid(Notice notice) {
        return notice != null && notice.isCompleteForSave();
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
}