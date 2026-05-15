package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * NotificationDAO manages notification file handling operations.
 *
 * Storage file:
 * notifications.txt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class NotificationDAO {

    private static final String FILE_NAME = "notifications.txt";

    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean addNotification(ServletContext context,
                                   String targetUserId,
                                   String targetRole,
                                   String title,
                                   String message,
                                   String type) {

        Notification notification = new Notification(
                FileUtil.generateId("NOT"),
                FileUtil.clean(targetUserId),
                FileUtil.clean(targetRole),
                FileUtil.clean(title),
                FileUtil.clean(message),
                FileUtil.clean(type),
                Notification.STATUS_UNREAD,
                now()
        );

        if (!notification.isCompleteForSave()) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, notification.toFileString());
    }

    public List<Notification> getAllNotifications(ServletContext context) {
        List<Notification> notifications = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Notification notification = Notification.fromFileString(line);

            if (notification != null && !notification.getNotificationId().isEmpty()) {
                notifications.add(notification);
            }
        }

        notifications.sort(
                Comparator.comparing(Notification::getCreatedAt, String.CASE_INSENSITIVE_ORDER).reversed()
        );

        return notifications;
    }

    public List<Notification> getNotificationsForUser(ServletContext context,
                                                      String userId,
                                                      String userRole) {

        List<Notification> selected = new ArrayList<>();
        String cleanUserId = FileUtil.clean(userId);
        String cleanRole = FileUtil.clean(userRole);

        for (Notification notification : getAllNotifications(context)) {
            boolean userMatches = !cleanUserId.isEmpty()
                    && notification.getTargetUserId().equalsIgnoreCase(cleanUserId);

            boolean roleMatches = !cleanRole.isEmpty()
                    && notification.getTargetRole().equalsIgnoreCase(cleanRole);

            boolean allUsers = "ALL".equalsIgnoreCase(notification.getTargetUserId())
                    || "ALL".equalsIgnoreCase(notification.getTargetRole());

            if (userMatches || roleMatches || allUsers) {
                selected.add(notification);
            }
        }

        return selected;
    }

    public List<Notification> getUnreadNotificationsForUser(ServletContext context,
                                                            String userId,
                                                            String userRole) {

        List<Notification> unread = new ArrayList<>();

        for (Notification notification : getNotificationsForUser(context, userId, userRole)) {
            if (notification.isUnread()) {
                unread.add(notification);
            }
        }

        return unread;
    }

    public int countUnreadForUser(ServletContext context, String userId, String userRole) {
        return getUnreadNotificationsForUser(context, userId, userRole).size();
    }

    public boolean markAsRead(ServletContext context, String notificationId) {
        String cleanId = FileUtil.clean(notificationId);

        if (cleanId.isEmpty()) {
            return false;
        }

        for (Notification notification : getAllNotifications(context)) {
            if (notification.getNotificationId().equalsIgnoreCase(cleanId)) {
                notification.setStatus(Notification.STATUS_READ);
                return FileUtil.updateLineById(
                        context,
                        FILE_NAME,
                        notification.getNotificationId(),
                        notification.toFileString()
                );
            }
        }

        return false;
    }

    public boolean markAllAsRead(ServletContext context, String userId, String userRole) {
        boolean allSuccess = true;

        for (Notification notification : getUnreadNotificationsForUser(context, userId, userRole)) {
            boolean success = markAsRead(context, notification.getNotificationId());

            if (!success) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    public boolean deleteNotification(ServletContext context, String notificationId) {
        String cleanId = FileUtil.clean(notificationId);

        if (cleanId.isEmpty()) {
            return false;
        }

        return FileUtil.deleteLineById(context, FILE_NAME, cleanId);
    }

    public String now() {
        return LocalDateTime.now().format(DISPLAY_DATE_TIME);
    }
}