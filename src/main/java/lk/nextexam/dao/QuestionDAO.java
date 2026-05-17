package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional MySQL DAO for question bank management.
 *
 * MySQL table:
 * questions
 *
 * Columns:
 * question_id, exam_id, question_type, question_text,
 * option_a, option_b, option_c, option_d,
 * correct_answer, marks, status, model_answer
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class QuestionDAO {

    public List<Question> getAllQuestions(ServletContext context) {
        List<Question> questions = new ArrayList<>();

        String sql = "SELECT question_id, exam_id, question_type, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer " +
                "FROM questions " +
                "ORDER BY exam_id ASC, question_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                questions.add(mapResultSetToQuestion(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> getAllQuestions failed");
            e.printStackTrace();
        }

        questions.sort(questionComparator());
        return questions;
    }

    public Question getQuestionById(ServletContext context, String questionId) {
        String cleanQuestionId = FileUtil.clean(questionId);

        if (cleanQuestionId.isEmpty()) {
            return null;
        }

        String sql = "SELECT question_id, exam_id, question_type, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer " +
                "FROM questions " +
                "WHERE LOWER(TRIM(question_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanQuestionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToQuestion(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> getQuestionById failed for " + cleanQuestionId);
            e.printStackTrace();
        }

        return null;
    }

    public List<Question> getQuestionsByExamId(ServletContext context, String examId) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return selectedQuestions;
        }

        String sql = "SELECT question_id, exam_id, question_type, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer " +
                "FROM questions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "ORDER BY question_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedQuestions.add(mapResultSetToQuestion(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> getQuestionsByExamId failed for " + cleanExamId);
            e.printStackTrace();
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    /**
     * Professional student-visible filter.
     * Supports both Active and Published for backward compatibility.
     */
    public List<Question> getActiveQuestionsByExamId(ServletContext context, String examId) {
        return getStudentVisibleQuestionsByExamId(context, examId);
    }

    public List<Question> getStudentVisibleQuestionsByExamId(ServletContext context, String examId) {
        List<Question> visibleQuestions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return visibleQuestions;
        }

        String sql = "SELECT question_id, exam_id, question_type, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer " +
                "FROM questions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) IN (LOWER(?), LOWER(?)) " +
                "ORDER BY question_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);
            statement.setString(2, Question.STATUS_ACTIVE);
            statement.setString(3, Question.STATUS_PUBLISHED);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Question question = mapResultSetToQuestion(resultSet);

                    if (question.isReadyForStudent()) {
                        visibleQuestions.add(question);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> getStudentVisibleQuestionsByExamId failed for " + cleanExamId);
            e.printStackTrace();
        }

        visibleQuestions.sort(questionComparator());
        return visibleQuestions;
    }

    public List<Question> getQuestionsByType(ServletContext context, String questionType) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanType = normalizeTypeInput(questionType);

        if (cleanType.isEmpty()) {
            return selectedQuestions;
        }

        String sql = "SELECT question_id, exam_id, question_type, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer " +
                "FROM questions " +
                "WHERE LOWER(TRIM(question_type)) = LOWER(TRIM(?)) " +
                "ORDER BY exam_id ASC, question_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedQuestions.add(mapResultSetToQuestion(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> getQuestionsByType failed for " + cleanType);
            e.printStackTrace();
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    public List<Question> getQuestionsByStatus(ServletContext context, String status) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return selectedQuestions;
        }

        String sql = "SELECT question_id, exam_id, question_type, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer " +
                "FROM questions " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY exam_id ASC, question_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedQuestions.add(mapResultSetToQuestion(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> getQuestionsByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    public List<Question> getQuestionsByExamAndType(ServletContext context, String examId, String questionType) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);
        String cleanType = normalizeTypeInput(questionType);

        if (cleanExamId.isEmpty() || cleanType.isEmpty()) {
            return selectedQuestions;
        }

        String sql = "SELECT question_id, exam_id, question_type, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer " +
                "FROM questions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(question_type)) = LOWER(TRIM(?)) " +
                "ORDER BY question_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);
            statement.setString(2, cleanType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedQuestions.add(mapResultSetToQuestion(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> getQuestionsByExamAndType failed");
            e.printStackTrace();
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    public List<Question> getQuestionsByExamAndStatus(ServletContext context, String examId, String status) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanExamId.isEmpty() || cleanStatus.isEmpty()) {
            return selectedQuestions;
        }

        String sql = "SELECT question_id, exam_id, question_type, question_text, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer " +
                "FROM questions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY question_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);
            statement.setString(2, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedQuestions.add(mapResultSetToQuestion(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> getQuestionsByExamAndStatus failed");
            e.printStackTrace();
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    public boolean addQuestion(ServletContext context, Question question) {
        if (!isValidForCreate(context, question)) {
            return false;
        }

        String sql = "INSERT INTO questions " +
                "(question_id, exam_id, question_type, question_text, option_a, option_b, option_c, option_d, correct_answer, marks, status, model_answer) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillQuestionStatement(statement, question);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> addQuestion failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateQuestion(ServletContext context, Question question) {
        if (!isValidForUpdate(context, question)) {
            return false;
        }

        String sql = "UPDATE questions SET " +
                "exam_id = ?, " +
                "question_type = ?, " +
                "question_text = ?, " +
                "option_a = ?, " +
                "option_b = ?, " +
                "option_c = ?, " +
                "option_d = ?, " +
                "correct_answer = ?, " +
                "marks = ?, " +
                "status = ?, " +
                "model_answer = ? " +
                "WHERE LOWER(TRIM(question_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, question.getExamId());
            statement.setString(2, question.getQuestionType());
            statement.setString(3, question.getQuestionText());
            statement.setString(4, question.isMcq() ? question.getOptionA() : "");
            statement.setString(5, question.isMcq() ? question.getOptionB() : "");
            statement.setString(6, question.isMcq() ? question.getOptionC() : "");
            statement.setString(7, question.isMcq() ? question.getOptionD() : "");
            statement.setString(8, question.isMcq() ? question.getCorrectAnswer() : "");
            statement.setDouble(9, question.getMarksAsDouble());
            statement.setString(10, question.getStatus());
            statement.setString(11, question.getModelAnswer());
            statement.setString(12, question.getQuestionId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> updateQuestion failed for " +
                    (question != null ? question.getQuestionId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteQuestion(ServletContext context, String questionId) {
        String cleanQuestionId = FileUtil.clean(questionId);

        if (cleanQuestionId.isEmpty()) {
            return false;
        }

        Question question = getQuestionById(context, cleanQuestionId);

        if (question == null) {
            return false;
        }

        /*
         * Professional rule:
         * Archived questions should remain as historical records.
         */
        if (question.isArchived()) {
            return false;
        }

        String sql = "DELETE FROM questions WHERE LOWER(TRIM(question_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanQuestionId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> deleteQuestion failed for " + cleanQuestionId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean archiveQuestion(ServletContext context, String questionId) {
        return updateQuestionStatus(questionId, Question.STATUS_ARCHIVED);
    }

    public boolean activateQuestion(ServletContext context, String questionId) {
        return updateQuestionStatus(questionId, Question.STATUS_ACTIVE);
    }

    public boolean publishQuestion(ServletContext context, String questionId) {
        return updateQuestionStatus(questionId, Question.STATUS_PUBLISHED);
    }

    public boolean setQuestionDraft(ServletContext context, String questionId) {
        return updateQuestionStatus(questionId, Question.STATUS_DRAFT);
    }

    public boolean deactivateQuestion(ServletContext context, String questionId) {
        return updateQuestionStatus(questionId, Question.STATUS_INACTIVE);
    }

    private boolean updateQuestionStatus(String questionId, String status) {
        String cleanQuestionId = FileUtil.clean(questionId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanQuestionId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        String sql = "UPDATE questions SET status = ? " +
                "WHERE LOWER(TRIM(question_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);
            statement.setString(2, cleanQuestionId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> updateQuestionStatus failed for " + cleanQuestionId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsById(ServletContext context, String questionId) {
        String cleanQuestionId = FileUtil.clean(questionId);

        if (cleanQuestionId.isEmpty()) {
            return false;
        }

        String sql = "SELECT question_id FROM questions " +
                "WHERE LOWER(TRIM(question_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanQuestionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> existsById failed for " + cleanQuestionId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasQuestions(ServletContext context, String examId) {
        return !getQuestionsByExamId(context, examId).isEmpty();
    }

    public boolean hasActiveQuestions(ServletContext context, String examId) {
        return !getStudentVisibleQuestionsByExamId(context, examId).isEmpty();
    }

    public boolean isExamReadyForStudent(ServletContext context, String examId) {
        return hasActiveQuestions(context, examId);
    }

    public String getExamReadinessMessage(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return "Exam ID is missing.";
        }

        List<Question> allQuestions = getQuestionsByExamId(context, cleanExamId);

        if (allQuestions.isEmpty()) {
            return "This exam does not have any questions yet.";
        }

        List<Question> visibleQuestions = getStudentVisibleQuestionsByExamId(context, cleanExamId);

        if (visibleQuestions.isEmpty()) {
            return "This exam does not have any active or published student-visible questions.";
        }

        for (Question question : visibleQuestions) {
            if (!question.isCompleteForSave()) {
                return "Some student-visible questions are incomplete.";
            }
        }

        return "OK";
    }

    public int countAllQuestions(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM questions");
    }

    public int countQuestionsByExamId(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM questions WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanExamId);
    }

    public int countActiveQuestionsByExamId(ServletContext context, String examId) {
        return countStudentVisibleQuestionsByExamId(context, examId);
    }

    public int countStudentVisibleQuestionsByExamId(ServletContext context, String examId) {
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM questions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) IN (LOWER(?), LOWER(?))";

        return countByThreeParameterQuery(
                sql,
                cleanExamId,
                Question.STATUS_ACTIVE,
                Question.STATUS_PUBLISHED
        );
    }

    public int countMcqQuestionsByExamId(ServletContext context, String examId) {
        return countQuestionsByExamAndType(context, examId, Question.TYPE_MCQ);
    }

    public int countEssayQuestionsByExamId(ServletContext context, String examId) {
        return countQuestionsByExamAndType(context, examId, Question.TYPE_ESSAY);
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM questions WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    public int countByType(ServletContext context, String questionType) {
        String cleanType = normalizeTypeInput(questionType);

        if (cleanType.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM questions WHERE LOWER(TRIM(question_type)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanType);
    }

    public double calculateTotalMarksByExamId(ServletContext context, String examId) {
        return calculateMarksByExamAndOptionalType(examId, "", true);
    }

    public double calculateTotalMarksAllQuestionsByExamId(ServletContext context, String examId) {
        return calculateMarksByExamAndOptionalType(examId, "", false);
    }

    public double calculateMcqMarksByExamId(ServletContext context, String examId) {
        return calculateMarksByExamAndOptionalType(examId, Question.TYPE_MCQ, true);
    }

    public double calculateEssayMarksByExamId(ServletContext context, String examId) {
        return calculateMarksByExamAndOptionalType(examId, Question.TYPE_ESSAY, true);
    }

    private boolean isValidForCreate(ServletContext context, Question question) {
        if (!isQuestionObjectValid(question)) {
            return false;
        }

        return !existsById(context, question.getQuestionId());
    }

    private boolean isValidForUpdate(ServletContext context, Question question) {
        if (!isQuestionObjectValid(question)) {
            return false;
        }

        return existsById(context, question.getQuestionId());
    }

    private boolean isQuestionObjectValid(Question question) {
        return question != null && question.isCompleteForSave();
    }

    private int countQuestionsByExamAndType(ServletContext context, String examId, String type) {
        String cleanExamId = FileUtil.clean(examId);
        String cleanType = normalizeTypeInput(type);

        if (cleanExamId.isEmpty() || cleanType.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM questions " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(question_type)) = LOWER(TRIM(?))";

        return countByTwoParameterQuery(sql, cleanExamId, cleanType);
    }

    private double calculateMarksByExamAndOptionalType(String examId, String questionType, boolean visibleOnly) {
        String cleanExamId = FileUtil.clean(examId);
        String cleanType = normalizeTypeInput(questionType);

        if (cleanExamId.isEmpty()) {
            return 0.0;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(marks), 0) AS total_marks_sum FROM questions " +
                        "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) "
        );

        if (visibleOnly) {
            sql.append("AND LOWER(TRIM(status)) IN (LOWER('Active'), LOWER('Published')) ");
        }

        if (!cleanType.isEmpty()) {
            sql.append("AND LOWER(TRIM(question_type)) = LOWER(TRIM(?)) ");
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            statement.setString(1, cleanExamId);

            if (!cleanType.isEmpty()) {
                statement.setString(2, cleanType);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("total_marks_sum");
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> calculateMarksByExamAndOptionalType failed");
            e.printStackTrace();
        }

        return 0.0;
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> countByQuery failed");
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
            System.out.println("QUESTIONDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private int countByTwoParameterQuery(String sql, String first, String second) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, first);
            statement.setString(2, second);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> countByTwoParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private int countByThreeParameterQuery(String sql, String first, String second, String third) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, first);
            statement.setString(2, second);
            statement.setString(3, third);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("QUESTIONDAO ERROR -> countByThreeParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillQuestionStatement(PreparedStatement statement, Question question) throws SQLException {
        statement.setString(1, question.getQuestionId());
        statement.setString(2, question.getExamId());
        statement.setString(3, question.getQuestionType());
        statement.setString(4, question.getQuestionText());
        statement.setString(5, question.isMcq() ? question.getOptionA() : "");
        statement.setString(6, question.isMcq() ? question.getOptionB() : "");
        statement.setString(7, question.isMcq() ? question.getOptionC() : "");
        statement.setString(8, question.isMcq() ? question.getOptionD() : "");
        statement.setString(9, question.isMcq() ? question.getCorrectAnswer() : "");
        statement.setDouble(10, question.getMarksAsDouble());
        statement.setString(11, question.getStatus());
        statement.setString(12, question.getModelAnswer());
    }

    private Question mapResultSetToQuestion(ResultSet resultSet) throws SQLException {
        return new Question(
                safe(resultSet.getString("question_id")),
                safe(resultSet.getString("exam_id")),
                normalizeTypeInput(resultSet.getString("question_type")),
                safe(resultSet.getString("question_text")),
                safe(resultSet.getString("option_a")),
                safe(resultSet.getString("option_b")),
                safe(resultSet.getString("option_c")),
                safe(resultSet.getString("option_d")),
                safe(resultSet.getString("correct_answer")),
                formatMarks(resultSet.getDouble("marks")),
                normalizeStatusInput(resultSet.getString("status")),
                safe(resultSet.getString("model_answer"))
        );
    }

    private String normalizeTypeInput(String value) {
        String type = safe(value);

        if (type.equalsIgnoreCase(Question.TYPE_MCQ)
                || type.equalsIgnoreCase("Multiple Choice")
                || type.equalsIgnoreCase("Multiple Choice Question")) {
            return Question.TYPE_MCQ;
        }

        if (type.equalsIgnoreCase(Question.TYPE_ESSAY)
                || type.equalsIgnoreCase("Structured")
                || type.equalsIgnoreCase("Written")
                || type.equalsIgnoreCase("Theory")) {
            return Question.TYPE_ESSAY;
        }

        return type;
    }

    private String normalizeStatusInput(String value) {
        String statusValue = safe(value);

        if (statusValue.equalsIgnoreCase(Question.STATUS_DRAFT)) {
            return Question.STATUS_DRAFT;
        }

        if (statusValue.equalsIgnoreCase(Question.STATUS_ACTIVE)) {
            return Question.STATUS_ACTIVE;
        }

        if (statusValue.equalsIgnoreCase(Question.STATUS_PUBLISHED)) {
            return Question.STATUS_PUBLISHED;
        }

        if (statusValue.equalsIgnoreCase(Question.STATUS_INACTIVE)) {
            return Question.STATUS_INACTIVE;
        }

        if (statusValue.equalsIgnoreCase(Question.STATUS_ARCHIVED)) {
            return Question.STATUS_ARCHIVED;
        }

        return statusValue;
    }

    private String formatMarks(double marks) {
        if (marks == Math.floor(marks)) {
            return String.valueOf((int) marks);
        }

        return String.format("%.2f", marks);
    }

    private Comparator<Question> questionComparator() {
        return Comparator
                .comparing(Question::getExamId, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Question::getQuestionId, String.CASE_INSENSITIVE_ORDER);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}