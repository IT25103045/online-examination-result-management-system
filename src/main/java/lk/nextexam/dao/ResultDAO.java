package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional MySQL DAO for result management.
 *
 * MySQL table:
 * results
 *
 * Columns:
 * result_id, student_id, exam_id, marks, grade, status, verification, published
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ResultDAO {

    public List<Result> getAllResults(ServletContext context) {
        List<Result> results = new ArrayList<>();

        String sql = "SELECT result_id, student_id, exam_id, marks, grade, status, verification, published " +
                "FROM results " +
                "ORDER BY result_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                results.add(mapResultSetToResult(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> getAllResults failed");
            e.printStackTrace();
        }

        results.sort(Comparator.comparing(Result::getResultId, String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    public Result getResultById(ServletContext context, String resultId) {
        String cleanResultId = FileUtil.clean(resultId);

        if (cleanResultId.isEmpty()) {
            return null;
        }

        String sql = "SELECT result_id, student_id, exam_id, marks, grade, status, verification, published " +
                "FROM results " +
                "WHERE LOWER(TRIM(result_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanResultId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToResult(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> getResultById failed for " + cleanResultId);
            e.printStackTrace();
        }

        return null;
    }

    public List<Result> getResultsByStudentId(ServletContext context, String studentId) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedResults;
        }

        String sql = "SELECT result_id, student_id, exam_id, marks, grade, status, verification, published " +
                "FROM results " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "ORDER BY exam_id ASC, result_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedResults.add(mapResultSetToResult(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> getResultsByStudentId failed for " + cleanStudentId);
            e.printStackTrace();
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

        String sql = "SELECT result_id, student_id, exam_id, marks, grade, status, verification, published " +
                "FROM results " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "ORDER BY student_id ASC, result_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedResults.add(mapResultSetToResult(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> getResultsByExamId failed for " + cleanExamId);
            e.printStackTrace();
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

        String sql = "SELECT result_id, student_id, exam_id, marks, grade, status, verification, published " +
                "FROM results " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);
            statement.setString(2, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToResult(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> getResultByStudentAndExam failed");
            e.printStackTrace();
        }

        return null;
    }

    public List<Result> getPublishedResultsByStudentId(ServletContext context, String studentId) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedResults;
        }

        String sql = "SELECT result_id, student_id, exam_id, marks, grade, status, verification, published " +
                "FROM results " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(published)) = LOWER(TRIM(?)) " +
                "ORDER BY exam_id ASC, result_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);
            statement.setString(2, Result.PUBLISHED_YES);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedResults.add(mapResultSetToResult(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> getPublishedResultsByStudentId failed for " + cleanStudentId);
            e.printStackTrace();
        }

        selectedResults.sort(Comparator.comparing(Result::getExamId, String.CASE_INSENSITIVE_ORDER));
        return selectedResults;
    }

    public List<Result> getResultsByVerification(ServletContext context, String verification) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanVerification = normalizeVerificationInput(verification);

        if (cleanVerification.isEmpty()) {
            return selectedResults;
        }

        String sql = "SELECT result_id, student_id, exam_id, marks, grade, status, verification, published " +
                "FROM results " +
                "WHERE LOWER(TRIM(verification)) = LOWER(TRIM(?)) " +
                "ORDER BY result_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanVerification);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedResults.add(mapResultSetToResult(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> getResultsByVerification failed for " + cleanVerification);
            e.printStackTrace();
        }

        return selectedResults;
    }

    public List<Result> getResultsByPublishedStatus(ServletContext context, String published) {
        List<Result> selectedResults = new ArrayList<>();
        String cleanPublished = normalizePublishedInput(published);

        if (cleanPublished.isEmpty()) {
            return selectedResults;
        }

        String sql = "SELECT result_id, student_id, exam_id, marks, grade, status, verification, published " +
                "FROM results " +
                "WHERE LOWER(TRIM(published)) = LOWER(TRIM(?)) " +
                "ORDER BY result_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanPublished);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedResults.add(mapResultSetToResult(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> getResultsByPublishedStatus failed for " + cleanPublished);
            e.printStackTrace();
        }

        return selectedResults;
    }

    public boolean addResult(ServletContext context, Result result) {
        if (result != null) {
            result.applyGradeAndStatusFromMarks();
        }

        if (!isValidForCreate(context, result)) {
            return false;
        }

        String sql = "INSERT INTO results " +
                "(result_id, student_id, exam_id, marks, grade, status, verification, published) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillResultStatement(statement, result);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> addResult failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateResult(ServletContext context, Result result) {
        if (result != null) {
            result.applyGradeAndStatusFromMarks();
        }

        if (!isValidForUpdate(context, result)) {
            return false;
        }

        String sql = "UPDATE results SET " +
                "student_id = ?, " +
                "exam_id = ?, " +
                "marks = ?, " +
                "grade = ?, " +
                "status = ?, " +
                "verification = ?, " +
                "published = ? " +
                "WHERE LOWER(TRIM(result_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, result.getStudentId());
            statement.setString(2, result.getExamId());
            statement.setDouble(3, result.getMarksAsDouble());
            statement.setString(4, result.getGrade());
            statement.setString(5, result.getStatus());
            statement.setString(6, result.getVerification());
            statement.setString(7, result.getPublished());
            statement.setString(8, result.getResultId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> updateResult failed for " +
                    (result != null ? result.getResultId() : ""));
            e.printStackTrace();
            return false;
        }
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

        String sql = "DELETE FROM results WHERE LOWER(TRIM(result_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanResultId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> deleteResult failed for " + cleanResultId);
            e.printStackTrace();
            return false;
        }
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
        String cleanResultId = FileUtil.clean(resultId);
        String cleanVerification = normalizeVerificationInput(verification);

        if (cleanResultId.isEmpty() || cleanVerification.isEmpty()) {
            return false;
        }

        Result result = getResultById(context, cleanResultId);

        if (result == null) {
            return false;
        }

        result.setVerification(cleanVerification);

        if (!result.isValidVerification()) {
            return false;
        }

        String sql = "UPDATE results SET verification = ? " +
                "WHERE LOWER(TRIM(result_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanVerification);
            statement.setString(2, cleanResultId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> updateVerification failed for " + cleanResultId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePublishedStatus(ServletContext context, String resultId, String published) {
        String cleanResultId = FileUtil.clean(resultId);
        String cleanPublished = normalizePublishedInput(published);

        if (cleanResultId.isEmpty() || cleanPublished.isEmpty()) {
            return false;
        }

        Result result = getResultById(context, cleanResultId);

        if (result == null) {
            return false;
        }

        result.setPublished(cleanPublished);

        if (!result.isValidPublishedStatus()) {
            return false;
        }

        if (result.isPublished() && !result.isVerified()) {
            return false;
        }

        String sql = "UPDATE results SET published = ? " +
                "WHERE LOWER(TRIM(result_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanPublished);
            statement.setString(2, cleanResultId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> updatePublishedStatus failed for " + cleanResultId);
            e.printStackTrace();
            return false;
        }
    }

    public int countAllResults(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM results");
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM results WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
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
        String cleanGrade = normalizeGradeInput(grade);

        if (cleanGrade.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM results WHERE LOWER(TRIM(grade)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanGrade);
    }

    public int countVerified(ServletContext context) {
        return countByVerification(Result.VERIFICATION_VERIFIED);
    }

    public int countVerificationPending(ServletContext context) {
        return countByVerification(Result.VERIFICATION_PENDING);
    }

    public int countReview(ServletContext context) {
        return countByVerification(Result.VERIFICATION_REVIEW);
    }

    public int countPublished(ServletContext context) {
        return countByPublished(Result.PUBLISHED_YES);
    }

    public int countNotPublished(ServletContext context) {
        return countByPublished(Result.PUBLISHED_NO);
    }

    public double calculateAverageMarks(ServletContext context) {
        String sql = "SELECT COALESCE(AVG(marks), 0) AS average_marks FROM results";
        return calculateSingleDouble(sql, "");
    }

    public double calculateAverageMarksByExam(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0.0;
        }

        String sql = "SELECT COALESCE(AVG(marks), 0) AS average_marks FROM results " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";
        return calculateSingleDouble(sql, cleanExamId);
    }

    public double calculateHighestMarksByExam(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0.0;
        }

        String sql = "SELECT COALESCE(MAX(marks), 0) AS highest_marks FROM results " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";
        return calculateSingleDouble(sql, cleanExamId);
    }

    public double calculateLowestMarksByExam(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0.0;
        }

        String sql = "SELECT COALESCE(MIN(marks), 0) AS lowest_marks FROM results " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";
        return calculateSingleDouble(sql, cleanExamId);
    }

    public boolean existsById(ServletContext context, String resultId) {
        String cleanResultId = FileUtil.clean(resultId);

        if (cleanResultId.isEmpty()) {
            return false;
        }

        String sql = "SELECT result_id FROM results " +
                "WHERE LOWER(TRIM(result_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanResultId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> existsById failed for " + cleanResultId);
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidForCreate(ServletContext context, Result result) {
        if (!isResultObjectValid(result)) {
            return false;
        }

        if (existsById(context, result.getResultId())) {
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

        String sql = "UPDATE results SET " +
                "student_id = ?, " +
                "exam_id = ?, " +
                "marks = ?, " +
                "grade = ?, " +
                "status = ?, " +
                "verification = ?, " +
                "published = ? " +
                "WHERE LOWER(TRIM(result_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, result.getStudentId());
            statement.setString(2, result.getExamId());
            statement.setDouble(3, result.getMarksAsDouble());
            statement.setString(4, result.getGrade());
            statement.setString(5, result.getStatus());
            statement.setString(6, result.getVerification());
            statement.setString(7, result.getPublished());
            statement.setString(8, result.getResultId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> updateResultWithoutRegrading failed for " + result.getResultId());
            e.printStackTrace();
            return false;
        }
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

    private int countByVerification(String verification) {
        String cleanVerification = normalizeVerificationInput(verification);

        if (cleanVerification.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM results WHERE LOWER(TRIM(verification)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanVerification);
    }

    private int countByPublished(String published) {
        String cleanPublished = normalizePublishedInput(published);

        if (cleanPublished.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM results WHERE LOWER(TRIM(published)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanPublished);
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> countByQuery failed");
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
            System.out.println("RESULTDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private double calculateSingleDouble(String sql, String parameter) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (!FileUtil.isBlank(parameter)) {
                statement.setString(1, parameter);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("RESULTDAO ERROR -> calculateSingleDouble failed");
            e.printStackTrace();
        }

        return 0.0;
    }

    private void fillResultStatement(PreparedStatement statement, Result result) throws SQLException {
        statement.setString(1, result.getResultId());
        statement.setString(2, result.getStudentId());
        statement.setString(3, result.getExamId());
        statement.setDouble(4, result.getMarksAsDouble());
        statement.setString(5, result.getGrade());
        statement.setString(6, result.getStatus());
        statement.setString(7, result.getVerification());
        statement.setString(8, result.getPublished());
    }

    private Result mapResultSetToResult(ResultSet resultSet) throws SQLException {
        return new Result(
                safe(resultSet.getString("result_id")),
                safe(resultSet.getString("student_id")),
                safe(resultSet.getString("exam_id")),
                formatMarks(resultSet.getDouble("marks")),
                normalizeGradeInput(resultSet.getString("grade")),
                normalizeStatusInput(resultSet.getString("status")),
                normalizeVerificationInput(resultSet.getString("verification")),
                normalizePublishedInput(resultSet.getString("published"))
        );
    }

    private String normalizeGradeInput(String value) {
        String gradeValue = safe(value).toUpperCase();

        if (gradeValue.equalsIgnoreCase(Result.GRADE_A)) {
            return Result.GRADE_A;
        }

        if (gradeValue.equalsIgnoreCase(Result.GRADE_B)) {
            return Result.GRADE_B;
        }

        if (gradeValue.equalsIgnoreCase(Result.GRADE_C)) {
            return Result.GRADE_C;
        }

        if (gradeValue.equalsIgnoreCase(Result.GRADE_S)) {
            return Result.GRADE_S;
        }

        if (gradeValue.equalsIgnoreCase(Result.GRADE_F)) {
            return Result.GRADE_F;
        }

        return gradeValue;
    }

    private String normalizeStatusInput(String value) {
        String statusValue = safe(value);

        if (statusValue.equalsIgnoreCase(Result.STATUS_PASS)) {
            return Result.STATUS_PASS;
        }

        if (statusValue.equalsIgnoreCase(Result.STATUS_FAIL)) {
            return Result.STATUS_FAIL;
        }

        if (statusValue.equalsIgnoreCase(Result.STATUS_PENDING)) {
            return Result.STATUS_PENDING;
        }

        return statusValue;
    }

    private String normalizeVerificationInput(String value) {
        String verificationValue = safe(value);

        if (verificationValue.equalsIgnoreCase(Result.VERIFICATION_VERIFIED)) {
            return Result.VERIFICATION_VERIFIED;
        }

        if (verificationValue.equalsIgnoreCase(Result.VERIFICATION_PENDING)) {
            return Result.VERIFICATION_PENDING;
        }

        if (verificationValue.equalsIgnoreCase(Result.VERIFICATION_REVIEW)) {
            return Result.VERIFICATION_REVIEW;
        }

        return verificationValue;
    }

    private String normalizePublishedInput(String value) {
        String publishedValue = safe(value);

        if (publishedValue.equalsIgnoreCase(Result.PUBLISHED_YES)) {
            return Result.PUBLISHED_YES;
        }

        if (publishedValue.equalsIgnoreCase(Result.PUBLISHED_NO)
                || publishedValue.equalsIgnoreCase("NotPublished")
                || publishedValue.equalsIgnoreCase("Unpublished")) {
            return Result.PUBLISHED_NO;
        }

        return publishedValue;
    }

    private String formatMarks(double marks) {
        if (marks == Math.floor(marks)) {
            return String.valueOf((int) marks);
        }

        return String.format("%.2f", marks);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}