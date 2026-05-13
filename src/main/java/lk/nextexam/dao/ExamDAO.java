package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Exam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional DAO for exam management.
 *
 * Storage file:
 * exams.txt
 *
 * Format:
 * examId|subject|examDate|duration|totalMarks|status
 */
public class ExamDAO {

    private static final String FILE_NAME = "exams.txt";

    public List<Exam> getAllExams(ServletContext context) {
        List<Exam> exams = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Exam exam = Exam.fromFileString(line);

            if (exam != null && !exam.getExamId().isEmpty()) {
                exams.add(exam);
            }
        }

        exams.sort(examDateComparator());
        return exams;
    }

    public Exam getExamById(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return null;
        }

        for (Exam exam : getAllExams(context)) {
            if (exam.getExamId().equalsIgnoreCase(cleanExamId)) {
                return exam;
            }
        }

        return null;
    }

    public List<Exam> getExamsByStatus(ServletContext context, String status) {
        List<Exam> selectedExams = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedExams;
        }

        for (Exam exam : getAllExams(context)) {
            if (exam.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedExams.add(exam);
            }
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

        for (Exam exam : getAllExams(context)) {
            if (exam.isCompleted() || exam.isPublished()) {
                completedExams.add(exam);
            }
        }

        completedExams.sort(examDateComparator());
        return completedExams;
    }

    public List<Exam> getClosedExams(ServletContext context) {
        List<Exam> closedExams = new ArrayList<>();

        for (Exam exam : getAllExams(context)) {
            if (exam.isClosed()) {
                closedExams.add(exam);
            }
        }

        closedExams.sort(examDateComparator());
        return closedExams;
    }

    public List<Exam> getAttemptableExams(ServletContext context) {
        List<Exam> attemptableExams = new ArrayList<>();

        for (Exam exam : getAllExams(context)) {
            if (exam.canStudentAttempt()) {
                attemptableExams.add(exam);
            }
        }

        attemptableExams.sort(examDateComparator());
        return attemptableExams;
    }

    public List<Exam> getUpcomingExams(ServletContext context) {
        List<Exam> upcomingExams = new ArrayList<>();

        for (Exam exam : getAllExams(context)) {
            if (exam.isFutureDate() && !exam.isClosed()) {
                upcomingExams.add(exam);
            }
        }

        upcomingExams.sort(examDateComparator());
        return upcomingExams;
    }

    public List<Exam> getTodayExams(ServletContext context) {
        List<Exam> todayExams = new ArrayList<>();

        for (Exam exam : getAllExams(context)) {
            if (exam.isToday()) {
                todayExams.add(exam);
            }
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

        return FileUtil.appendLine(context, FILE_NAME, exam.toFileString());
    }

    public boolean updateExam(ServletContext context, Exam exam) {
        if (!isValidForUpdate(context, exam)) {
            return false;
        }

        return FileUtil.updateLineById(context, FILE_NAME, exam.getExamId(), exam.toFileString());
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

        return FileUtil.deleteLineById(context, FILE_NAME, cleanExamId);
    }

    public boolean existsById(ServletContext context, String examId) {
        return FileUtil.existsById(context, FILE_NAME, examId);
    }

    public boolean updateExamStatus(ServletContext context, String examId, String newStatus) {
        String cleanStatus = FileUtil.clean(newStatus);

        if (cleanStatus.isEmpty()) {
            return false;
        }

        Exam exam = getExamById(context, examId);

        if (exam == null) {
            return false;
        }

        exam.setStatus(cleanStatus);

        if (!exam.isValidStatus()) {
            return false;
        }

        return updateExam(context, exam);
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
        return getAllExams(context).size();
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
        return getAttemptableExams(context).size();
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
        return getTodayExams(context).size();
    }

    public int countUpcomingExams(ServletContext context) {
        return getUpcomingExams(context).size();
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (Exam exam : getAllExams(context)) {
            if (exam.getStatus().equalsIgnoreCase(cleanStatus)) {
                count++;
            }
        }

        return count;
    }

    public double calculateTotalExamMarks(ServletContext context) {
        double total = 0.0;

        for (Exam exam : getAllExams(context)) {
            total += exam.getTotalMarksAsDouble();
        }

        return total;
    }

    private boolean isValidForCreate(ServletContext context, Exam exam) {
        if (!isExamObjectValid(exam)) {
            return false;
        }

        return !FileUtil.existsById(context, FILE_NAME, exam.getExamId());
    }

    private boolean isValidForUpdate(ServletContext context, Exam exam) {
        if (!isExamObjectValid(exam)) {
            return false;
        }

        return FileUtil.existsById(context, FILE_NAME, exam.getExamId());
    }

    private boolean isExamObjectValid(Exam exam) {
        return exam != null && exam.isCompleteForSave();
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
}