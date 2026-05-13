package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.NoticeDAO;
import lk.nextexam.model.Notice;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Professional controller for Notice management and viewing.
 *
 * Route:
 * /notices
 *
 * Staff:
 * - Admin and Lecturer can manage all notices.
 *
 * Students:
 * - Students can view only Published notices targeted to All or Student.
 */
@WebServlet("/notices")
public class NoticeServlet extends HttpServlet {

    private static final String ACTION_ADD = "add";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_STATUS = "status";

    private final NoticeDAO noticeDAO = new NoticeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);
        String userRole = getSessionValue(session, "userRole");

        boolean canManageNotices = canManageNotices(userRole);

        List<Notice> notices = canManageNotices
                ? noticeDAO.getAllNotices(getServletContext())
                : noticeDAO.getVisibleNoticesForRole(getServletContext(), userRole);

        request.setAttribute("notices", notices);
        request.setAttribute("canManageNotices", canManageNotices);
        request.setAttribute("currentUserRole", userRole);

        request.setAttribute("totalNotices", notices != null ? notices.size() : 0);
        request.setAttribute("publishedNoticeCount", noticeDAO.countPublishedNotices(getServletContext()));
        request.setAttribute("draftNoticeCount", noticeDAO.countDraftNotices(getServletContext()));
        request.setAttribute("archivedNoticeCount", noticeDAO.countArchivedNotices(getServletContext()));
        request.setAttribute("urgentNoticeCount", noticeDAO.countUrgentNotices(getServletContext()));
        request.setAttribute("visibleNoticeCount", noticeDAO.countVisibleNoticesForRole(getServletContext(), userRole));

        request.getRequestDispatcher("/notices/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);
        String userRole = getSessionValue(session, "userRole");

        if (!canManageNotices(userRole)) {
            redirectToNotices(request, response, "error", "accessDenied");
            return;
        }

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_ADD.equalsIgnoreCase(action)) {
            addNotice(request, response);
            return;
        }

        if (ACTION_UPDATE.equalsIgnoreCase(action)) {
            updateNotice(request, response);
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteNotice(request, response);
            return;
        }

        if (ACTION_STATUS.equalsIgnoreCase(action)) {
            updateNoticeStatus(request, response);
            return;
        }

        redirectToNotices(request, response, "error", "invalidAction");
    }

    private void addNotice(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Notice notice = buildNoticeFromRequest(request);
        String validationError = validateNotice(notice);

        if (validationError != null) {
            redirectToNotices(request, response, "error", validationError);
            return;
        }

        boolean success = noticeDAO.addNotice(getServletContext(), notice);

        if (success) {
            redirectToNotices(request, response, "success", "noticeAdded");
        } else {
            redirectToNotices(request, response, "error", "noticeAddFailed");
        }
    }

    private void updateNotice(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Notice notice = buildNoticeFromRequest(request);
        String validationError = validateNotice(notice);

        if (validationError != null) {
            redirectToNotices(request, response, "error", validationError);
            return;
        }

        boolean success = noticeDAO.updateNotice(getServletContext(), notice);

        if (success) {
            redirectToNotices(request, response, "success", "noticeUpdated");
        } else {
            redirectToNotices(request, response, "error", "noticeUpdateFailed");
        }
    }

    private void deleteNotice(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String noticeId = firstNonBlank(
                request.getParameter("recordId"),
                request.getParameter("noticeId"),
                request.getParameter("id")
        );

        if (noticeId.isEmpty()) {
            redirectToNotices(request, response, "error", "missingNoticeId");
            return;
        }

        boolean success = noticeDAO.deleteNotice(getServletContext(), noticeId);

        if (success) {
            redirectToNotices(request, response, "success", "noticeDeleted");
        } else {
            redirectToNotices(request, response, "error", "noticeDeleteFailed");
        }
    }

    private void updateNoticeStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String noticeId = FileUtil.clean(request.getParameter("noticeId"));
        String status = FileUtil.clean(request.getParameter("status"));

        if (noticeId.isEmpty()) {
            redirectToNotices(request, response, "error", "missingNoticeId");
            return;
        }

        if (status.isEmpty()) {
            redirectToNotices(request, response, "error", "missingStatus");
            return;
        }

        Notice notice = noticeDAO.getNoticeById(getServletContext(), noticeId);

        if (notice == null) {
            redirectToNotices(request, response, "error", "noticeNotFound");
            return;
        }

        notice.setStatus(status);

        if (!notice.isValidStatus()) {
            redirectToNotices(request, response, "error", "invalidStatus");
            return;
        }

        boolean success = noticeDAO.updateNotice(getServletContext(), notice);

        if (success) {
            redirectToNotices(request, response, "success", "noticeStatusUpdated");
        } else {
            redirectToNotices(request, response, "error", "noticeStatusUpdateFailed");
        }
    }

    private Notice buildNoticeFromRequest(HttpServletRequest request) {
        return new Notice(
                FileUtil.clean(request.getParameter("noticeId")),
                FileUtil.clean(request.getParameter("title")),
                FileUtil.clean(request.getParameter("description")),
                FileUtil.clean(request.getParameter("noticeDate")),
                FileUtil.clean(request.getParameter("targetGroup")),
                FileUtil.clean(request.getParameter("priority")),
                FileUtil.clean(request.getParameter("status"))
        );
    }

    private String validateNotice(Notice notice) {
        if (notice == null) {
            return "invalidNotice";
        }

        if (notice.getNoticeId().isEmpty()) {
            return "missingNoticeId";
        }

        if (notice.getTitle().isEmpty()) {
            return "missingTitle";
        }

        if (notice.getDescription().isEmpty()) {
            return "missingDescription";
        }

        if (notice.getNoticeDate().isEmpty()) {
            return "missingNoticeDate";
        }

        if (!notice.isValidNoticeDate()) {
            return "invalidNoticeDate";
        }

        if (notice.getTargetGroup().isEmpty()) {
            return "missingTargetGroup";
        }

        if (!notice.isValidTargetGroup()) {
            return "invalidTargetGroup";
        }

        if (notice.getPriority().isEmpty()) {
            return "missingPriority";
        }

        if (!notice.isValidPriority()) {
            return "invalidPriority";
        }

        if (notice.getStatus().isEmpty()) {
            return "missingStatus";
        }

        if (!notice.isValidStatus()) {
            return "invalidStatus";
        }

        if (!notice.isCompleteForSave()) {
            return "incompleteNotice";
        }

        return null;
    }

    private boolean canManageNotices(String role) {
        return User.ROLE_ADMIN.equalsIgnoreCase(role)
                || User.ROLE_LECTURER.equalsIgnoreCase(role);
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            String cleaned = FileUtil.clean(value);

            if (!cleaned.isEmpty()) {
                return cleaned;
            }
        }

        return "";
    }

    private void redirectToNotices(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String messageType,
                                   String messageCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/notices?"
                        + urlEncode(messageType)
                        + "="
                        + urlEncode(messageCode)
        );
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private void prepareRequestResponse(HttpServletRequest request,
                                        HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }
}