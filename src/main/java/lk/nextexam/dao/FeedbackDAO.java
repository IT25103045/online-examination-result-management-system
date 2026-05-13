package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Feedback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional DAO for feedback management.
 *
 * Storage file:
 * feedback.txt
 *
 * Format:
 * feedbackId|studentId|category|message|date|status
 */
public class FeedbackDAO {

    private static final String FILE_NAME = "feedback.txt";

    public List<Feedback> getAllFeedback(ServletContext context) {
        List<Feedback> feedbackList = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Feedback feedback = Feedback.fromFileString(line);

            if (feedback != null && !feedback.getFeedbackId().isEmpty()) {
                feedbackList.add(feedback);
            }
        }

        feedbackList.sort(feedbackComparator());
        return feedbackList;
    }

    public Feedback getFeedbackById(ServletContext context, String feedbackId) {
        String cleanFeedbackId = FileUtil.clean(feedbackId);

        if (cleanFeedbackId.isEmpty()) {
            return null;
        }

        for (Feedback feedback : getAllFeedback(context)) {
            if (feedback.getFeedbackId().equalsIgnoreCase(cleanFeedbackId)) {
                return feedback;
            }
        }

        return null;
    }

    public List<Feedback> getFeedbackByStudentId(ServletContext context, String studentId) {
        List<Feedback> selectedFeedback = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedFeedback;
        }

        for (Feedback feedback : getAllFeedback(context)) {
            if (feedback.getStudentId().equalsIgnoreCase(cleanStudentId)) {
                selectedFeedback.add(feedback);
            }
        }

        selectedFeedback.sort(feedbackComparator());
        return selectedFeedback;
    }

    public List<Feedback> getFeedbackByCategory(ServletContext context, String category) {
        List<Feedback> selectedFeedback = new ArrayList<>();
        String cleanCategory = FileUtil.clean(category);

        if (cleanCategory.isEmpty()) {
            return selectedFeedback;
        }

        for (Feedback feedback : getAllFeedback(context)) {
            if (feedback.getCategory().equalsIgnoreCase(cleanCategory)) {
                selectedFeedback.add(feedback);
            }
        }

        selectedFeedback.sort(feedbackComparator());
        return selectedFeedback;
    }

    public List<Feedback> getFeedbackByStatus(ServletContext context, String status) {
        List<Feedback> selectedFeedback = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedFeedback;
        }

        for (Feedback feedback : getAllFeedback(context)) {
            if (feedback.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedFeedback.add(feedback);
            }
        }

        selectedFeedback.sort(feedbackComparator());
        return selectedFeedback;
    }

    public List<Feedback> getOpenFeedback(ServletContext context) {
        List<Feedback> openFeedback = new ArrayList<>();

        for (Feedback feedback : getAllFeedback(context)) {
            if (feedback.isOpen()) {
                openFeedback.add(feedback);
            }
        }

        openFeedback.sort(feedbackComparator());
        return openFeedback;
    }

    public List<Feedback> getCompletedFeedback(ServletContext context) {
        List<Feedback> completedFeedback = new ArrayList<>();

        for (Feedback feedback : getAllFeedback(context)) {
            if (feedback.isCompleted()) {
                completedFeedback.add(feedback);
            }
        }

        completedFeedback.sort(feedbackComparator());
        return completedFeedback;
    }

    public List<Feedback> getTodayFeedback(ServletContext context) {
        List<Feedback> todayFeedback = new ArrayList<>();

        for (Feedback feedback : getAllFeedback(context)) {
            if (feedback.isToday()) {
                todayFeedback.add(feedback);
            }
        }

        todayFeedback.sort(feedbackComparator());
        return todayFeedback;
    }

    public boolean addFeedback(ServletContext context, Feedback feedback) {
        if (feedback == null) {
            return false;
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

        return FileUtil.appendLine(context, FILE_NAME, feedback.toFileString());
    }

    public boolean updateFeedback(ServletContext context, Feedback feedback) {
        if (!isValidForUpdate(context, feedback)) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                feedback.getFeedbackId(),
                feedback.toFileString()
        );
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

        return FileUtil.deleteLineById(context, FILE_NAME, cleanFeedbackId);
    }

    public boolean updateFeedbackStatus(ServletContext context, String feedbackId, String status) {
        String cleanFeedbackId = FileUtil.clean(feedbackId);
        String cleanStatus = FileUtil.clean(status);

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

        return updateFeedback(context, feedback);
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
        return FileUtil.existsById(context, FILE_NAME, feedbackId);
    }

    public int countAllFeedback(ServletContext context) {
        return getAllFeedback(context).size();
    }

    public int countFeedbackByStudentId(ServletContext context, String studentId) {
        return getFeedbackByStudentId(context, studentId).size();
    }

    public int countNewFeedback(ServletContext context) {
        return getFeedbackByStatus(context, Feedback.STATUS_NEW).size();
    }

    public int countInReviewFeedback(ServletContext context) {
        return getFeedbackByStatus(context, Feedback.STATUS_IN_REVIEW).size();
    }

    public int countResolvedFeedback(ServletContext context) {
        return getFeedbackByStatus(context, Feedback.STATUS_RESOLVED).size();
    }

    public int countClosedFeedback(ServletContext context) {
        return getFeedbackByStatus(context, Feedback.STATUS_CLOSED).size();
    }

    public int countOpenFeedback(ServletContext context) {
        return getOpenFeedback(context).size();
    }

    public int countCompletedFeedback(ServletContext context) {
        return getCompletedFeedback(context).size();
    }

    public int countTodayFeedback(ServletContext context) {
        return getTodayFeedback(context).size();
    }

    public int countTechnicalFeedback(ServletContext context) {
        return getFeedbackByCategory(context, Feedback.CATEGORY_TECHNICAL).size();
    }

    public int countExamFeedback(ServletContext context) {
        return getFeedbackByCategory(context, Feedback.CATEGORY_EXAM).size();
    }

    public int countResultFeedback(ServletContext context) {
        return getFeedbackByCategory(context, Feedback.CATEGORY_RESULT).size();
    }

    private boolean isValidForCreate(ServletContext context, Feedback feedback) {
        if (!isFeedbackObjectValid(feedback)) {
            return false;
        }

        return !FileUtil.existsById(context, FILE_NAME, feedback.getFeedbackId());
    }

    private boolean isValidForUpdate(ServletContext context, Feedback feedback) {
        if (!isFeedbackObjectValid(feedback)) {
            return false;
        }

        return FileUtil.existsById(context, FILE_NAME, feedback.getFeedbackId());
    }

    private boolean isFeedbackObjectValid(Feedback feedback) {
        return feedback != null && feedback.isCompleteForSave();
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
}