package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional DAO for result management.
 *
 * Storage file:
 * results.txt
 *
 * Format:
 * resultId|studentId|examId|marks|grade|status|verification|published
 */
public class ResultDAO {

    private static final String FILE_NAME = "results.txt";

    public List<Result> getAllResults(ServletContext context) {
        List<Result> results = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Result result = Result.fromFileString(line);

            if (result != null && !result.getResultId().isEmpty()) {
                results.add(result);
            }
        }

        results.sort(Comparator.comparing(Result::getResultId, String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    public Result getResultById(ServletContext context, String resultId) {
        String cleanResultId = FileUtil.clean(resultId);

        if (cleanResultId.isEmpty()) {
            return null;
        }

        for (Result result : getAllResults(context)) {
            if (result.getResultId().equalsIgnoreCase(cleanResultId)) {
                return result;
            }
        }

        return null;
    }

    public List<Result> getResultsByStudentId(ServletContext context, String studentId) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedResults;
        }

        for (Result result : getAllResults(context)) {
            if (result.getStudentId().equalsIgnoreCase(cleanStudentId)) {
                selectedResults.add(result);
            }
        }

        selectedResults.sort(Comparator.comparing(Result::getExamId, String.CASE_INSENSITIVE_ORDER));
        return selectedResults;
    }

    public List<Result> getResultsByExamId(ServletContext context, String examId) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return selectedResults;
        }

        for (Result result : getAllResults(context)) {
            if (result.getExamId().equalsIgnoreCase(cleanExamId)) {
                selectedResults.add(result);
            }
        }

        selectedResults.sort(Comparator.comparing(Result::getStudentId, String.CASE_INSENSITIVE_ORDER));
        return selectedResults;
    }

    public Result getResultByStudentAndExam(ServletContext context, String studentId, String examId) {
        String cleanStudentId = FileUtil.clean(studentId);
        String cleanExamId = FileUtil.clean(examId);

        if (cleanStudentId.isEmpty() || cleanExamId.isEmpty()) {
            return null;
        }

        for (Result result : getAllResults(context)) {
            boolean sameStudent = result.getStudentId().equalsIgnoreCase(cleanStudentId);
            boolean sameExam = result.getExamId().equalsIgnoreCase(cleanExamId);

            if (sameStudent && sameExam) {
                return result;
            }
        }

        return null;
    }

    public List<Result> getPublishedResultsByStudentId(ServletContext context, String studentId) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedResults;
        }

        for (Result result : getResultsByStudentId(context, cleanStudentId)) {
            if (result.isPublished()) {
                selectedResults.add(result);
            }
        }

        selectedResults.sort(Comparator.comparing(Result::getExamId, String.CASE_INSENSITIVE_ORDER));
        return selectedResults;
    }

    public List<Result> getResultsByVerification(ServletContext context, String verification) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanVerification = FileUtil.clean(verification);

        if (cleanVerification.isEmpty()) {
            return selectedResults;
        }

        for (Result result : getAllResults(context)) {
            if (result.getVerification().equalsIgnoreCase(cleanVerification)) {
                selectedResults.add(result);
            }
        }

        return selectedResults;
    }

    public List<Result> getResultsByPublishedStatus(ServletContext context, String published) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanPublished = FileUtil.clean(published);

        if (cleanPublished.isEmpty()) {
            return selectedResults;
        }

        for (Result result : getAllResults(context)) {
            if (result.getPublished().equalsIgnoreCase(cleanPublished)) {
                selectedResults.add(result);
            }
        }

        return selectedResults;
    }

    public boolean addResult(ServletContext context, Result result) {
        if (!isValidForCreate(context, result)) {
            return false;
        }

        result.applyGradeAndStatusFromMarks();

        return FileUtil.appendLine(context, FILE_NAME, result.toFileString());
    }

    public boolean updateResult(ServletContext context, Result result) {
        if (!isValidForUpdate(context, result)) {
            return false;
        }

        result.applyGradeAndStatusFromMarks();

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                result.getResultId(),
                result.toFileString()
        );
    }

    public boolean deleteResult(ServletContext context, String resultId) {
        String cleanResultId = FileUtil.clean(resultId);

        if (cleanResultId.isEmpty()) {
            return false;
        }

        Result existingResult = getResultById(context, cleanResultId);

        if (existingResult == null) {
            return false;
        }

        /*
         * Professional rule:
         * Published result records should not be deleted directly.
         * Unpublish first, then delete if needed.
         */
        if (existingResult.isPublished()) {
            return false;
        }

        return FileUtil.deleteLineById(context, FILE_NAME, cleanResultId);
    }

    public boolean publishResult(ServletContext context, String resultId) {
        Result result = getResultById(context, resultId);

        if (result == null || !result.canPublish()) {
            return false;
        }

        result.setPublished(Result.PUBLISHED_YES);
        return updateResultWithoutRegrading(context, result);
    }

    public boolean unpublishResult(ServletContext context, String resultId) {
        Result result = getResultById(context, resultId);

        if (result == null) {
            return false;
        }

        result.setPublished(Result.PUBLISHED_NO);
        return updateResultWithoutRegrading(context, result);
    }

    public boolean verifyResult(ServletContext context, String resultId) {
        return updateVerification(context, resultId, Result.VERIFICATION_VERIFIED);
    }

    public boolean markResultForReview(ServletContext context, String resultId) {
        return updateVerification(context, resultId, Result.VERIFICATION_REVIEW);
    }

    public boolean markResultPending(ServletContext context, String resultId) {
        return updateVerification(context, resultId, Result.VERIFICATION_PENDING);
    }

    public boolean updateVerification(ServletContext context, String resultId, String verification) {
        Result result = getResultById(context, resultId);

        if (result == null) {
            return false;
        }

        result.setVerification(verification);

        if (!result.isValidVerification()) {
            return false;
        }

        return updateResultWithoutRegrading(context, result);
    }

    public boolean updatePublishedStatus(ServletContext context, String resultId, String published) {
        Result result = getResultById(context, resultId);

        if (result == null) {
            return false;
        }

        result.setPublished(published);

        if (!result.isValidPublishedStatus()) {
            return false;
        }

        if (result.isPublished() && !result.isVerified()) {
            return false;
        }

        return updateResultWithoutRegrading(context, result);
    }

    public int countAllResults(ServletContext context) {
        return getAllResults(context).size();
    }

    public int countByStatus(ServletContext context, String status) {
        int count = 0;
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return count;
        }

        for (Result result : getAllResults(context)) {
            if (result.getStatus().equalsIgnoreCase(cleanStatus)) {
                count++;
            }
        }

        return count;
    }

    public int countPass(ServletContext context) {
        return countByStatus(context, Result.STATUS_PASS);
    }

    public int countFail(ServletContext context) {
        return countByStatus(context, Result.STATUS_FAIL);
    }

    public int countPending(ServletContext context) {
        return countByStatus(context, Result.STATUS_PENDING);
    }

    public int countByGrade(ServletContext context, String grade) {
        int count = 0;
        String cleanGrade = FileUtil.clean(grade);

        if (cleanGrade.isEmpty()) {
            return count;
        }

        for (Result result : getAllResults(context)) {
            if (result.getGrade().equalsIgnoreCase(cleanGrade)) {
                count++;
            }
        }

        return count;
    }

    public int countVerified(ServletContext context) {
        return getResultsByVerification(context, Result.VERIFICATION_VERIFIED).size();
    }

    public int countVerificationPending(ServletContext context) {
        return getResultsByVerification(context, Result.VERIFICATION_PENDING).size();
    }

    public int countReview(ServletContext context) {
        return getResultsByVerification(context, Result.VERIFICATION_REVIEW).size();
    }

    public int countPublished(ServletContext context) {
        return getResultsByPublishedStatus(context, Result.PUBLISHED_YES).size();
    }

    public int countNotPublished(ServletContext context) {
        return getResultsByPublishedStatus(context, Result.PUBLISHED_NO).size();
    }

    public double calculateAverageMarks(ServletContext context) {
        List<Result> results = getAllResults(context);

        if (results.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        int counted = 0;

        for (Result result : results) {
            total += result.getMarksAsDouble();
            counted++;
        }

        if (counted == 0) {
            return 0.0;
        }

        return total / counted;
    }

    public double calculateAverageMarksByExam(ServletContext context, String examId) {
        List<Result> results = getResultsByExamId(context, examId);

        if (results.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        int counted = 0;

        for (Result result : results) {
            total += result.getMarksAsDouble();
            counted++;
        }

        if (counted == 0) {
            return 0.0;
        }

        return total / counted;
    }

    public double calculateHighestMarksByExam(ServletContext context, String examId) {
        double highest = 0.0;

        for (Result result : getResultsByExamId(context, examId)) {
            if (result.getMarksAsDouble() > highest) {
                highest = result.getMarksAsDouble();
            }
        }

        return highest;
    }

    public double calculateLowestMarksByExam(ServletContext context, String examId) {
        double lowest = 0.0;
        boolean found = false;

        for (Result result : getResultsByExamId(context, examId)) {
            if (!found) {
                lowest = result.getMarksAsDouble();
                found = true;
            } else if (result.getMarksAsDouble() < lowest) {
                lowest = result.getMarksAsDouble();
            }
        }

        return found ? lowest : 0.0;
    }

    public boolean existsById(ServletContext context, String resultId) {
        return FileUtil.existsById(context, FILE_NAME, resultId);
    }

    private boolean isValidForCreate(ServletContext context, Result result) {
        if (!isResultObjectValid(result)) {
            return false;
        }

        if (FileUtil.existsById(context, FILE_NAME, result.getResultId())) {
            return false;
        }

        /*
         * One result record per student per exam.
         */
        return getResultByStudentAndExam(context, result.getStudentId(), result.getExamId()) == null;
    }

    private boolean isValidForUpdate(ServletContext context, Result result) {
        if (!isResultObjectValid(result)) {
            return false;
        }

        Result existingResult = getResultById(context, result.getResultId());

        if (existingResult == null) {
            return false;
        }

        /*
         * Published results should not be edited directly.
         * Unpublish first if edits are required.
         */
        if (existingResult.isPublished()) {
            return false;
        }

        Result duplicate = getResultByStudentAndExam(context, result.getStudentId(), result.getExamId());

        if (duplicate != null && !duplicate.getResultId().equalsIgnoreCase(result.getResultId())) {
            return false;
        }

        return true;
    }

    private boolean updateResultWithoutRegrading(ServletContext context, Result result) {
        if (result == null || result.getResultId().isEmpty()) {
            return false;
        }

        if (!result.isCompleteForSave()) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                result.getResultId(),
                result.toFileString()
        );
    }

    private boolean isResultObjectValid(Result result) {
        if (result == null) {
            return false;
        }

        /*
         * Grade and status are recalculated from marks before save.
         * But if the JSP already sends them correctly, this still passes.
         */
        if (result.getGrade().isEmpty() || result.getStatus().isEmpty()) {
            result.applyGradeAndStatusFromMarks();
        }

        if (result.isPublished() && !result.isVerified()) {
            return false;
        }

        return result.isCompleteForSave();
    }
}