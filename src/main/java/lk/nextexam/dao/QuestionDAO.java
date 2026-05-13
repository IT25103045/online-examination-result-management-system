package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Question;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional DAO for question bank management.
 *
 * Storage file:
 * questions.txt
 *
 * Format:
 * questionId|examId|questionType|questionText|optionA|optionB|optionC|optionD|correctAnswer|marks|status|modelAnswer
 */
public class QuestionDAO {

    private static final String FILE_NAME = "questions.txt";

    public List<Question> getAllQuestions(ServletContext context) {
        List<Question> questions = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Question question = Question.fromFileString(line);

            if (question != null && !question.getQuestionId().isEmpty()) {
                questions.add(question);
            }
        }

        questions.sort(questionComparator());
        return questions;
    }

    public Question getQuestionById(ServletContext context, String questionId) {
        String cleanQuestionId = FileUtil.clean(questionId);

        if (cleanQuestionId.isEmpty()) {
            return null;
        }

        for (Question question : getAllQuestions(context)) {
            if (question.getQuestionId().equalsIgnoreCase(cleanQuestionId)) {
                return question;
            }
        }

        return null;
    }

    public List<Question> getQuestionsByExamId(ServletContext context, String examId) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return selectedQuestions;
        }

        for (Question question : getAllQuestions(context)) {
            if (question.getExamId().equalsIgnoreCase(cleanExamId)) {
                selectedQuestions.add(question);
            }
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

        for (Question question : getQuestionsByExamId(context, examId)) {
            if (question.isReadyForStudent()) {
                visibleQuestions.add(question);
            }
        }

        visibleQuestions.sort(questionComparator());
        return visibleQuestions;
    }

    public List<Question> getQuestionsByType(ServletContext context, String questionType) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanType = FileUtil.clean(questionType);

        if (cleanType.isEmpty()) {
            return selectedQuestions;
        }

        for (Question question : getAllQuestions(context)) {
            if (question.getQuestionType().equalsIgnoreCase(cleanType)) {
                selectedQuestions.add(question);
            }
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    public List<Question> getQuestionsByStatus(ServletContext context, String status) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedQuestions;
        }

        for (Question question : getAllQuestions(context)) {
            if (question.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedQuestions.add(question);
            }
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    public List<Question> getQuestionsByExamAndType(ServletContext context, String examId, String questionType) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanType = FileUtil.clean(questionType);

        if (cleanType.isEmpty()) {
            return selectedQuestions;
        }

        for (Question question : getQuestionsByExamId(context, examId)) {
            if (question.getQuestionType().equalsIgnoreCase(cleanType)) {
                selectedQuestions.add(question);
            }
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    public List<Question> getQuestionsByExamAndStatus(ServletContext context, String examId, String status) {
        List<Question> selectedQuestions = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedQuestions;
        }

        for (Question question : getQuestionsByExamId(context, examId)) {
            if (question.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedQuestions.add(question);
            }
        }

        selectedQuestions.sort(questionComparator());
        return selectedQuestions;
    }

    public boolean addQuestion(ServletContext context, Question question) {
        if (!isValidForCreate(context, question)) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, question.toFileString());
    }

    public boolean updateQuestion(ServletContext context, Question question) {
        if (!isValidForUpdate(context, question)) {
            return false;
        }

        return FileUtil.updateLineById(context, FILE_NAME, question.getQuestionId(), question.toFileString());
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

        return FileUtil.deleteLineById(context, FILE_NAME, cleanQuestionId);
    }

    public boolean archiveQuestion(ServletContext context, String questionId) {
        Question question = getQuestionById(context, questionId);

        if (question == null) {
            return false;
        }

        question.setStatus(Question.STATUS_ARCHIVED);
        return updateQuestion(context, question);
    }

    public boolean activateQuestion(ServletContext context, String questionId) {
        Question question = getQuestionById(context, questionId);

        if (question == null) {
            return false;
        }

        question.setStatus(Question.STATUS_ACTIVE);
        return updateQuestion(context, question);
    }

    public boolean publishQuestion(ServletContext context, String questionId) {
        Question question = getQuestionById(context, questionId);

        if (question == null) {
            return false;
        }

        question.setStatus(Question.STATUS_PUBLISHED);
        return updateQuestion(context, question);
    }

    public boolean setQuestionDraft(ServletContext context, String questionId) {
        Question question = getQuestionById(context, questionId);

        if (question == null) {
            return false;
        }

        question.setStatus(Question.STATUS_DRAFT);
        return updateQuestion(context, question);
    }

    public boolean deactivateQuestion(ServletContext context, String questionId) {
        Question question = getQuestionById(context, questionId);

        if (question == null) {
            return false;
        }

        question.setStatus(Question.STATUS_INACTIVE);
        return updateQuestion(context, question);
    }

    public boolean existsById(ServletContext context, String questionId) {
        return FileUtil.existsById(context, FILE_NAME, questionId);
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
        return getAllQuestions(context).size();
    }

    public int countQuestionsByExamId(ServletContext context, String examId) {
        return getQuestionsByExamId(context, examId).size();
    }

    public int countActiveQuestionsByExamId(ServletContext context, String examId) {
        return getStudentVisibleQuestionsByExamId(context, examId).size();
    }

    public int countStudentVisibleQuestionsByExamId(ServletContext context, String examId) {
        return getStudentVisibleQuestionsByExamId(context, examId).size();
    }

    public int countMcqQuestionsByExamId(ServletContext context, String examId) {
        int count = 0;

        for (Question question : getQuestionsByExamId(context, examId)) {
            if (question.isMcq()) {
                count++;
            }
        }

        return count;
    }

    public int countEssayQuestionsByExamId(ServletContext context, String examId) {
        int count = 0;

        for (Question question : getQuestionsByExamId(context, examId)) {
            if (question.isEssay()) {
                count++;
            }
        }

        return count;
    }

    public int countByStatus(ServletContext context, String status) {
        return getQuestionsByStatus(context, status).size();
    }

    public int countByType(ServletContext context, String questionType) {
        return getQuestionsByType(context, questionType).size();
    }

    public double calculateTotalMarksByExamId(ServletContext context, String examId) {
        double totalMarks = 0.0;

        for (Question question : getStudentVisibleQuestionsByExamId(context, examId)) {
            totalMarks += question.getMarksAsDouble();
        }

        return totalMarks;
    }

    public double calculateTotalMarksAllQuestionsByExamId(ServletContext context, String examId) {
        double totalMarks = 0.0;

        for (Question question : getQuestionsByExamId(context, examId)) {
            totalMarks += question.getMarksAsDouble();
        }

        return totalMarks;
    }

    public double calculateMcqMarksByExamId(ServletContext context, String examId) {
        double totalMarks = 0.0;

        for (Question question : getStudentVisibleQuestionsByExamId(context, examId)) {
            if (question.isMcq()) {
                totalMarks += question.getMarksAsDouble();
            }
        }

        return totalMarks;
    }

    public double calculateEssayMarksByExamId(ServletContext context, String examId) {
        double totalMarks = 0.0;

        for (Question question : getStudentVisibleQuestionsByExamId(context, examId)) {
            if (question.isEssay()) {
                totalMarks += question.getMarksAsDouble();
            }
        }

        return totalMarks;
    }

    private boolean isValidForCreate(ServletContext context, Question question) {
        if (!isQuestionObjectValid(question)) {
            return false;
        }

        return !FileUtil.existsById(context, FILE_NAME, question.getQuestionId());
    }

    private boolean isValidForUpdate(ServletContext context, Question question) {
        if (!isQuestionObjectValid(question)) {
            return false;
        }

        return FileUtil.existsById(context, FILE_NAME, question.getQuestionId());
    }

    private boolean isQuestionObjectValid(Question question) {
        return question != null && question.isCompleteForSave();
    }

    private Comparator<Question> questionComparator() {
        return Comparator
                .comparing(Question::getExamId, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Question::getQuestionId, String.CASE_INSENSITIVE_ORDER);
    }
}