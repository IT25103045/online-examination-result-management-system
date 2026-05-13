package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ExamSubmission;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional DAO for exam submissions.
 *
 * Storage file:
 * exam_submissions.txt
 *
 * Format:
 * submissionId|examId|studentId|studentName|submittedAt|answersData|score|totalMarks|status
 */
public class ExamSubmissionDAO {

    private static final String FILE_NAME = "exam_submissions.txt";

    public List<ExamSubmission> getAllSubmissions(ServletContext context) {
        List<ExamSubmission> submissions = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            ExamSubmission submission = ExamSubmission.fromFileString(line);

            if (submission != null && !submission.getSubmissionId().isEmpty()) {
                submissions.add(submission);
            }
        }

        submissions.sort(submissionDateComparator());
        return submissions;
    }

    public ExamSubmission getSubmissionById(ServletContext context, String submissionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return null;
        }

        for (ExamSubmission submission : getAllSubmissions(context)) {
            if (submission.getSubmissionId().equalsIgnoreCase(cleanSubmissionId)) {
                return submission;
            }
        }

        return null;
    }

    public List<ExamSubmission> getSubmissionsByStudent(ServletContext context, String studentId) {
        List<ExamSubmission> selectedSubmissions = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedSubmissions;
        }

        for (ExamSubmission submission : getAllSubmissions(context)) {
            if (submission.getStudentId().equalsIgnoreCase(cleanStudentId)) {
                selectedSubmissions.add(submission);
            }
        }

        selectedSubmissions.sort(submissionDateComparator());
        return selectedSubmissions;
    }

    public List<ExamSubmission> getSubmissionsByExam(ServletContext context, String examId) {
        List<ExamSubmission> selectedSubmissions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return selectedSubmissions;
        }

        for (ExamSubmission submission : getAllSubmissions(context)) {
            if (submission.getExamId().equalsIgnoreCase(cleanExamId)) {
                selectedSubmissions.add(submission);
            }
        }

        selectedSubmissions.sort(submissionDateComparator());
        return selectedSubmissions;
    }

    public List<ExamSubmission> getSubmissionsByStatus(ServletContext context, String status) {
        List<ExamSubmission> selectedSubmissions = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedSubmissions;
        }

        for (ExamSubmission submission : getAllSubmissions(context)) {
            if (submission.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedSubmissions.add(submission);
            }
        }

        selectedSubmissions.sort(submissionDateComparator());
        return selectedSubmissions;
    }

    public List<ExamSubmission> getSubmissionsByExamAndStatus(ServletContext context,
                                                              String examId,
                                                              String status) {
        List<ExamSubmission> selectedSubmissions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);
        String cleanStatus = FileUtil.clean(status);

        if (cleanExamId.isEmpty() || cleanStatus.isEmpty()) {
            return selectedSubmissions;
        }

        for (ExamSubmission submission : getSubmissionsByExam(context, cleanExamId)) {
            if (submission.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedSubmissions.add(submission);
            }
        }

        selectedSubmissions.sort(submissionDateComparator());
        return selectedSubmissions;
    }

    public ExamSubmission getSubmissionByStudentAndExam(ServletContext context,
                                                        String studentId,
                                                        String examId) {
        String cleanStudentId = FileUtil.clean(studentId);
        String cleanExamId = FileUtil.clean(examId);

        if (cleanStudentId.isEmpty() || cleanExamId.isEmpty()) {
            return null;
        }

        for (ExamSubmission submission : getAllSubmissions(context)) {
            boolean sameStudent = submission.getStudentId().equalsIgnoreCase(cleanStudentId);
            boolean sameExam = submission.getExamId().equalsIgnoreCase(cleanExamId);

            if (sameStudent && sameExam && !submission.isCancelled()) {
                return submission;
            }
        }

        return null;
    }

    public boolean hasStudentSubmitted(ServletContext context, String studentId, String examId) {
        return getSubmissionByStudentAndExam(context, studentId, examId) != null;
    }

    public boolean addSubmission(ServletContext context, ExamSubmission submission) {
        if (!isValidForCreate(context, submission)) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, submission.toFileString());
    }

    public boolean updateSubmission(ServletContext context, ExamSubmission submission) {
        if (!isValidForUpdate(context, submission)) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                submission.getSubmissionId(),
                submission.toFileString()
        );
    }

    public boolean deleteSubmission(ServletContext context, String submissionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return false;
        }

        ExamSubmission submission = getSubmissionById(context, cleanSubmissionId);

        if (submission == null) {
            return false;
        }

        /*
         * Professional rule:
         * Published submissions should not be physically deleted.
         * Cancel them instead to preserve audit history.
         */
        if (submission.isPublished()) {
            return false;
        }

        return FileUtil.deleteLineById(context, FILE_NAME, cleanSubmissionId);
    }

    public boolean markAsAutoMarked(ServletContext context, String submissionId) {
        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_AUTO_MARKED);
    }

    public boolean markAsManualReviewRequired(ServletContext context, String submissionId) {
        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_MANUAL_REVIEW_REQUIRED);
    }

    public boolean markAsMarked(ServletContext context, String submissionId) {
        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_MARKED);
    }

    public boolean publishSubmission(ServletContext context, String submissionId) {
        ExamSubmission submission = getSubmissionById(context, submissionId);

        if (submission == null) {
            return false;
        }

        if (!submission.canBePublished()) {
            return false;
        }

        submission.setStatus(ExamSubmission.STATUS_PUBLISHED);
        return updateSubmission(context, submission);
    }

    public boolean cancelSubmission(ServletContext context, String submissionId) {
        return updateSubmissionStatus(context, submissionId, ExamSubmission.STATUS_CANCELLED);
    }

    public boolean updateSubmissionStatus(ServletContext context, String submissionId, String newStatus) {
        String cleanSubmissionId = FileUtil.clean(submissionId);
        String cleanStatus = FileUtil.clean(newStatus);

        if (cleanSubmissionId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        ExamSubmission submission = getSubmissionById(context, cleanSubmissionId);

        if (submission == null) {
            return false;
        }

        submission.setStatus(cleanStatus);

        if (!submission.isValidStatus()) {
            return false;
        }

        return updateSubmission(context, submission);
    }

    public boolean updateScore(ServletContext context,
                               String submissionId,
                               double score,
                               double totalMarks,
                               String status) {

        ExamSubmission submission = getSubmissionById(context, submissionId);

        if (submission == null) {
            return false;
        }

        if (score < 0 || totalMarks <= 0 || score > totalMarks) {
            return false;
        }

        submission.setScore(formatNumber(score));
        submission.setTotalMarks(formatNumber(totalMarks));

        if (!FileUtil.isBlank(status)) {
            submission.setStatus(status);
        }

        if (!submission.isValidStatus()) {
            return false;
        }

        return updateSubmission(context, submission);
    }

    public int countAllSubmissions(ServletContext context) {
        return getAllSubmissions(context).size();
    }

    public int countSubmissionsByExam(ServletContext context, String examId) {
        return getSubmissionsByExam(context, examId).size();
    }

    public int countSubmissionsByStudent(ServletContext context, String studentId) {
        return getSubmissionsByStudent(context, studentId).size();
    }

    public int countByStatus(ServletContext context, String status) {
        return getSubmissionsByStatus(context, status).size();
    }

    public int countSubmitted(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_SUBMITTED);
    }

    public int countAutoMarked(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_AUTO_MARKED);
    }

    public int countManualReviewRequired(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_MANUAL_REVIEW_REQUIRED);
    }

    public int countMarked(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_MARKED);
    }

    public int countPublished(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_PUBLISHED);
    }

    public int countCancelled(ServletContext context) {
        return countByStatus(context, ExamSubmission.STATUS_CANCELLED);
    }

    public double calculateAveragePercentageByExam(ServletContext context, String examId) {
        List<ExamSubmission> submissions = getSubmissionsByExam(context, examId);

        if (submissions.isEmpty()) {
            return 0.0;
        }

        double totalPercentage = 0.0;
        int counted = 0;

        for (ExamSubmission submission : submissions) {
            if (!submission.isCancelled()) {
                totalPercentage += submission.getPercentage();
                counted++;
            }
        }

        if (counted == 0) {
            return 0.0;
        }

        return totalPercentage / counted;
    }

    public double calculateHighestPercentageByExam(ServletContext context, String examId) {
        double highest = 0.0;

        for (ExamSubmission submission : getSubmissionsByExam(context, examId)) {
            if (!submission.isCancelled() && submission.getPercentage() > highest) {
                highest = submission.getPercentage();
            }
        }

        return highest;
    }

    public double calculateLowestPercentageByExam(ServletContext context, String examId) {
        double lowest = 0.0;
        boolean found = false;

        for (ExamSubmission submission : getSubmissionsByExam(context, examId)) {
            if (submission.isCancelled()) {
                continue;
            }

            if (!found) {
                lowest = submission.getPercentage();
                found = true;
            } else if (submission.getPercentage() < lowest) {
                lowest = submission.getPercentage();
            }
        }

        return found ? lowest : 0.0;
    }

    public boolean existsById(ServletContext context, String submissionId) {
        return FileUtil.existsById(context, FILE_NAME, submissionId);
    }

    private boolean isValidForCreate(ServletContext context, ExamSubmission submission) {
        if (!isSubmissionObjectValid(submission)) {
            return false;
        }

        if (FileUtil.existsById(context, FILE_NAME, submission.getSubmissionId())) {
            return false;
        }

        /*
         * One active submission per student per exam.
         * Cancelled submissions do not block future attempts.
         */
        return !hasStudentSubmitted(context, submission.getStudentId(), submission.getExamId());
    }

    private boolean isValidForUpdate(ServletContext context, ExamSubmission submission) {
        if (!isSubmissionObjectValid(submission)) {
            return false;
        }

        return FileUtil.existsById(context, FILE_NAME, submission.getSubmissionId());
    }

    private boolean isSubmissionObjectValid(ExamSubmission submission) {
        return submission != null && submission.isCompleteForSave();
    }

    private Comparator<ExamSubmission> submissionDateComparator() {
        return Comparator
                .comparing(
                        (ExamSubmission submission) -> {
                            LocalDateTime dateTime = submission.getSubmittedDateTime();
                            return dateTime == null ? LocalDateTime.MIN : dateTime;
                        }
                )
                .reversed()
                .thenComparing(ExamSubmission::getSubmissionId, String.CASE_INSENSITIVE_ORDER);
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }
}