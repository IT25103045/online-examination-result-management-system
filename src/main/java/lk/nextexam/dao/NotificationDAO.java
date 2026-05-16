package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationDAO manages file-based notification records.
 *
 * Storage file:
 * notifications.txt
 *
 * Storage format:
 * notificationId|targetUserId|targetRole|title|message|type|status|createdAt|readAt|targetUrl
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class NotificationDAO {

    private static final String FILE_NAME = "notifications.txt";

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Returns all notifications from notifications.txt.
     */
    public List<Notification> getAllNotifications(ServletContext context) {
        List<Notification> notifications = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Notification notification = Notification.fromFileString(line);

            if (notification != null && !notification.getNotificationId().isEmpty()) {
                notifications.add(notification);
            }
        }

        return notifications;
    }

    /**
     * Finds a notification by notification ID.
     */
    public Notification getNotificationById(ServletContext context, String notificationId) {
        String cleanNotificationId = FileUtil.clean(notificationId);

        if (cleanNotificationId.isEmpty()) {
            return null;
        }

        for (Notification notification : getAllNotifications(context)) {
            if (notification.getNotificationId().equalsIgnoreCase(cleanNotificationId)) {
                return notification;
            }
        }

        return null;
    }

    /**
     * Returns notifications visible to a specific logged-in user.
     *
     * Matching rules:
     * 1. Exact target user ID
     * 2. Target role
     * 3. Target role = All
     */
    public List<Notification> getNotificationsForUser(ServletContext context,
                                                      String userId,
                                                      String role) {
        List<Notification> selectedNotifications = new ArrayList<>();

        String cleanUserId = FileUtil.clean(userId);
        String cleanRole = FileUtil.clean(role);

        for (Notification notification : getAllNotifications(context)) {
            if (!notification.isArchived()
                    && notification.isTargetedToUser(cleanUserId, cleanRole)) {
                selectedNotifications.add(notification);
            }
        }

        return selectedNotifications;
    }

    /**
     * Returns notifications for a specific role.
     */
    public List<Notification> getNotificationsByRole(ServletContext context, String role) {
        List<Notification> selectedNotifications = new ArrayList<>();
        String cleanRole = FileUtil.clean(role);

        if (cleanRole.isEmpty()) {
            return selectedNotifications;
        }

        for (Notification notification : getAllNotifications(context)) {
            if (!notification.isArchived()
                    && notification.getTargetRole().equalsIgnoreCase(cleanRole)) {
                selectedNotifications.add(notification);
            }
        }

        return selectedNotifications;
    }

    /**
     * Returns notifications for a specific user ID.
     */
    public List<Notification> getNotificationsByUserId(ServletContext context, String userId) {
        List<Notification> selectedNotifications = new ArrayList<>();
        String cleanUserId = FileUtil.clean(userId);

        if (cleanUserId.isEmpty()) {
            return selectedNotifications;
        }

        for (Notification notification : getAllNotifications(context)) {
            if (!notification.isArchived()
                    && notification.getTargetUserId().equalsIgnoreCase(cleanUserId)) {
                selectedNotifications.add(notification);
            }
        }

        return selectedNotifications;
    }

    /**
     * Returns notifications by type.
     */
    public List<Notification> getNotificationsByType(ServletContext context,
                                                     String userId,
                                                     String role,
                                                     String type) {
        List<Notification> selectedNotifications = new ArrayList<>();
        String cleanType = FileUtil.clean(type);

        if (cleanType.isEmpty()) {
            return selectedNotifications;
        }

        for (Notification notification : getNotificationsForUser(context, userId, role)) {
            if (notification.getType().equalsIgnoreCase(cleanType)) {
                selectedNotifications.add(notification);
            }
        }

        return selectedNotifications;
    }

    /**
     * Counts unread notifications for the logged-in user.
     */
    public int countUnreadForUser(ServletContext context, String userId, String role) {
        int count = 0;

        for (Notification notification : getNotificationsForUser(context, userId, role)) {
            if (notification.isUnread()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Counts read notifications for the logged-in user.
     */
    public int countReadForUser(ServletContext context, String userId, String role) {
        int count = 0;

        for (Notification notification : getNotificationsForUser(context, userId, role)) {
            if (notification.isRead()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Counts all visible notifications for the logged-in user.
     */
    public int countAllForUser(ServletContext context, String userId, String role) {
        return getNotificationsForUser(context, userId, role).size();
    }

    /**
     * Base create method.
     *
     * Use this when you already created a Notification object.
     */
    public boolean addNotification(ServletContext context, Notification notification) {
        if (notification == null) {
            return false;
        }

        if (notification.getNotificationId().isEmpty()) {
            notification.setNotificationId(FileUtil.generateId("NT"));
        }

        if (notification.getCreatedAt().isEmpty()) {
            notification.setCreatedAt(now());
        }

        if (notification.getStatus().isEmpty()) {
            notification.setStatus(Notification.STATUS_UNREAD);
        }

        if (!notification.isCompleteForSave()) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, notification.toFileString());
    }

    /**
     * Convenience create method for a specific user.
     *
     * This fixes this type of call:
     * notificationDAO.addNotification(context, userId, title, message, type, targetUrl);
     */
    public boolean addNotification(ServletContext context,
                                   String targetUserId,
                                   String title,
                                   String message,
                                   String type,
                                   String targetUrl) {

        Notification notification = new Notification(
                FileUtil.generateId("NT"),
                FileUtil.clean(targetUserId),
                "",
                FileUtil.clean(title),
                FileUtil.clean(message),
                FileUtil.clean(type),
                Notification.STATUS_UNREAD,
                now(),
                "",
                FileUtil.clean(targetUrl)
        );

        return addNotification(context, notification);
    }

    /**
     * Convenience create method for a role.
     *
     * Example targetRole values:
     * Admin, Lecturer, Student, All
     */
    public boolean addRoleNotification(ServletContext context,
                                       String targetRole,
                                       String title,
                                       String message,
                                       String type,
                                       String targetUrl) {

        Notification notification = new Notification(
                FileUtil.generateId("NT"),
                "",
                FileUtil.clean(targetRole),
                FileUtil.clean(title),
                FileUtil.clean(message),
                FileUtil.clean(type),
                Notification.STATUS_UNREAD,
                now(),
                "",
                FileUtil.clean(targetUrl)
        );

        return addNotification(context, notification);
    }

    /**
     * Alternative method name for role notifications.
     *
     * This supports possible calls like:
     * notificationDAO.createForRole(...)
     */
    public boolean createForRole(ServletContext context,
                                 String targetRole,
                                 String title,
                                 String message,
                                 String type,
                                 String targetUrl) {

        return addRoleNotification(
                context,
                targetRole,
                title,
                message,
                type,
                targetUrl
        );
    }

    /**
     * Alternative method name for user notifications.
     *
     * This supports possible calls like:
     * notificationDAO.createForUser(...)
     */
    public boolean createForUser(ServletContext context,
                                 String targetUserId,
                                 String title,
                                 String message,
                                 String type,
                                 String targetUrl) {

        return addNotification(
                context,
                targetUserId,
                title,
                message,
                type,
                targetUrl
        );
    }

    /**
     * Updates a notification record.
     */
    public boolean updateNotification(ServletContext context, Notification notification) {
        if (notification == null || notification.getNotificationId().isEmpty()) {
            return false;
        }

        if (!notification.isCompleteForSave()) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                notification.getNotificationId(),
                notification.toFileString()
        );
    }

    /**
     * Marks one notification as read.
     */
    public boolean markAsRead(ServletContext context, String notificationId) {
        Notification notification = getNotificationById(context, notificationId);

        if (notification == null) {
            return false;
        }

        notification.setStatus(Notification.STATUS_READ);
        notification.setReadAt(now());

        return updateNotification(context, notification);
    }

    /**
     * Marks one notification as unread.
     */
    public boolean markAsUnread(ServletContext context, String notificationId) {
        Notification notification = getNotificationById(context, notificationId);

        if (notification == null) {
            return false;
        }

        notification.setStatus(Notification.STATUS_UNREAD);
        notification.setReadAt("");

        return updateNotification(context, notification);
    }

    /**
     * Marks all visible unread notifications as read for a logged-in user.
     */
    public int markAllAsReadForUser(ServletContext context, String userId, String role) {
        int updatedCount = 0;

        for (Notification notification : getNotificationsForUser(context, userId, role)) {
            if (notification.isUnread()) {
                notification.setStatus(Notification.STATUS_READ);
                notification.setReadAt(now());

                if (updateNotification(context, notification)) {
                    updatedCount++;
                }
            }
        }

        return updatedCount;
    }

    /**
     * Archives one notification.
     */
    public boolean archiveNotification(ServletContext context, String notificationId) {
        Notification notification = getNotificationById(context, notificationId);

        if (notification == null) {
            return false;
        }

        notification.setStatus(Notification.STATUS_ARCHIVED);

        return updateNotification(context, notification);
    }

    /**
     * Deletes one notification from the file.
     */
    public boolean deleteNotification(ServletContext context, String notificationId) {
        String cleanNotificationId = FileUtil.clean(notificationId);

        if (cleanNotificationId.isEmpty()) {
            return false;
        }

        return FileUtil.deleteLineById(context, FILE_NAME, cleanNotificationId);
    }

    /**
     * Deletes all archived notifications.
     */
    public int deleteArchivedNotifications(ServletContext context) {
        int deletedCount = 0;
        List<Notification> notifications = getAllNotifications(context);

        for (Notification notification : notifications) {
            if (notification.isArchived()) {
                boolean deleted = deleteNotification(context, notification.getNotificationId());

                if (deleted) {
                    deletedCount++;
                }
            }
        }

        return deletedCount;
    }

    /**
     * Creates a notification when a result is published.
     */
    public boolean notifyResultPublished(ServletContext context,
                                         String studentId,
                                         String examId) {

        return addNotification(
                context,
                studentId,
                "Result Published",
                "Your result for " + FileUtil.clean(examId) + " has been published.",
                Notification.TYPE_RESULT,
                "/my-results"
        );
    }

    /**
     * Creates a staff notification when a result appeal is submitted.
     */
    public boolean notifyResultAppealSubmitted(ServletContext context,
                                               String appealId,
                                               String studentId) {

        boolean adminNotification = addRoleNotification(
                context,
                "Admin",
                "New Result Appeal",
                "Student " + FileUtil.clean(studentId) + " submitted result appeal " + FileUtil.clean(appealId) + ".",
                Notification.TYPE_APPEAL,
                "/result-appeals"
        );

        boolean lecturerNotification = addRoleNotification(
                context,
                "Lecturer",
                "New Result Appeal",
                "Student " + FileUtil.clean(studentId) + " submitted result appeal " + FileUtil.clean(appealId) + ".",
                Notification.TYPE_APPEAL,
                "/result-appeals"
        );

        return adminNotification && lecturerNotification;
    }

    /**
     * Creates a student notification when an appeal status is updated.
     */
    public boolean notifyAppealUpdated(ServletContext context,
                                       String studentId,
                                       String appealId,
                                       String status) {

        return addNotification(
                context,
                studentId,
                "Appeal Status Updated",
                "Your result appeal " + FileUtil.clean(appealId) + " is now " + FileUtil.clean(status) + ".",
                Notification.TYPE_APPEAL,
                "/result-appeals"
        );
    }

    /**
     * Creates a student notification when a document is reviewed.
     */
    public boolean notifyDocumentReviewed(ServletContext context,
                                          String studentId,
                                          String documentId,
                                          String status) {

        return addNotification(
                context,
                studentId,
                "Document " + FileUtil.clean(status),
                "Your document " + FileUtil.clean(documentId) + " has been " + FileUtil.clean(status) + ".",
                Notification.TYPE_DOCUMENT,
                "/documents"
        );
    }

    /**
     * Creates a staff notification for manual marking reminders.
     */
    public boolean notifyManualMarkingPending(ServletContext context,
                                              String submissionId) {

        return addRoleNotification(
                context,
                "Lecturer",
                "Essay Marking Pending",
                "Submission " + FileUtil.clean(submissionId) + " is waiting for manual essay marking.",
                Notification.TYPE_EXAM,
                "/submissions"
        );
    }

    /**
     * Creates a notification for all users.
     */
    public boolean notifyAllUsers(ServletContext context,
                                  String title,
                                  String message,
                                  String type,
                                  String targetUrl) {

        return addRoleNotification(
                context,
                "All",
                title,
                message,
                type,
                targetUrl
        );
    }

    /**
     * Current timestamp for file storage.
     */
    public String now() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME);
    }
}