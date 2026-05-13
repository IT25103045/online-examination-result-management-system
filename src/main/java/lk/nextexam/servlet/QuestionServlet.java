package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.QuestionDAO;
import lk.nextexam.model.Question;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Professional controller for Question Bank management.
 *
 * Supported actions:
 * - GET  /questions
 * - GET  /questions?examId=EX001
 * - POST /questions action=add
 * - POST /questions action=update
 * - POST /questions action=delete
 *
 * Backward compatibility:
 * - GET /questions?action=delete&id=Q001 still works,
 *   but POST delete is recommended.
 */
@WebServlet("/questions")
public class QuestionServlet extends HttpServlet {

    private static final String ACTION_ADD = "add";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";

    private final QuestionDAO questionDAO = new QuestionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteQuestionFromGet(request, response);
            return;
        }

        showQuestionPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_ADD.equalsIgnoreCase(action)) {
            addQuestion(request, response);
            return;
        }

        if (ACTION_UPDATE.equalsIgnoreCase(action)) {
            updateQuestion(request, response);
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteQuestion(request, response);
            return;
        }

        redirectToQuestions(request, response, "error", "invalidAction", "");
    }

    private void showQuestionPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String examId = FileUtil.clean(request.getParameter("examId"));

        List<Question> questions;

        if (!examId.isEmpty()) {
            questions = questionDAO.getQuestionsByExamId(getServletContext(), examId);
            request.setAttribute("selectedExamId", examId);
        } else {
            questions = questionDAO.getAllQuestions(getServletContext());
            request.setAttribute("selectedExamId", "");
        }

        int totalQuestions = questions != null ? questions.size() : 0;
        int studentVisibleQuestions = 0;
        int activeQuestions = 0;
        int publishedQuestions = 0;
        int draftQuestions = 0;
        int inactiveQuestions = 0;
        int archivedQuestions = 0;
        int mcqQuestions = 0;
        int essayQuestions = 0;

        double allQuestionMarks = 0.0;
        double studentVisibleMarks = 0.0;
        double mcqMarks = 0.0;
        double essayMarks = 0.0;

        if (questions != null) {
            for (Question question : questions) {
                if (question.isVisibleToStudent()) {
                    studentVisibleQuestions++;
                    studentVisibleMarks += question.getMarksAsDouble();
                }

                if (question.isActive()) {
                    activeQuestions++;
                }

                if (question.isPublished()) {
                    publishedQuestions++;
                }

                if (question.isDraft()) {
                    draftQuestions++;
                }

                if (question.isInactive()) {
                    inactiveQuestions++;
                }

                if (question.isArchived()) {
                    archivedQuestions++;
                }

                if (question.isMcq()) {
                    mcqQuestions++;
                    mcqMarks += question.getMarksAsDouble();
                }

                if (question.isEssay()) {
                    essayQuestions++;
                    essayMarks += question.getMarksAsDouble();
                }

                allQuestionMarks += question.getMarksAsDouble();
            }
        }

        String readinessMessage = "";
        if (!examId.isEmpty()) {
            readinessMessage = questionDAO.getExamReadinessMessage(getServletContext(), examId);
        }

        request.setAttribute("questions", questions);

        request.setAttribute("totalQuestions", totalQuestions);
        request.setAttribute("activeQuestions", activeQuestions);
        request.setAttribute("publishedQuestions", publishedQuestions);
        request.setAttribute("studentVisibleQuestions", studentVisibleQuestions);
        request.setAttribute("draftQuestions", draftQuestions);
        request.setAttribute("inactiveQuestions", inactiveQuestions);
        request.setAttribute("archivedQuestions", archivedQuestions);
        request.setAttribute("mcqQuestions", mcqQuestions);
        request.setAttribute("essayQuestions", essayQuestions);

        request.setAttribute("questionTotalMarks", studentVisibleMarks);
        request.setAttribute("allQuestionMarks", allQuestionMarks);
        request.setAttribute("studentVisibleMarks", studentVisibleMarks);
        request.setAttribute("mcqMarks", mcqMarks);
        request.setAttribute("essayMarks", essayMarks);
        request.setAttribute("readinessMessage", readinessMessage);

        request.getRequestDispatcher("/questions/index.jsp").forward(request, response);
    }

    private void addQuestion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Question question = buildQuestionFromRequest(request);
        String validationError = validateQuestion(question);

        if (validationError != null) {
            redirectToQuestions(request, response, "error", validationError, question.getExamId());
            return;
        }

        boolean success = questionDAO.addQuestion(getServletContext(), question);

        if (success) {
            redirectToQuestions(request, response, "success", "questionAdded", question.getExamId());
        } else {
            redirectToQuestions(request, response, "error", "questionAddFailed", question.getExamId());
        }
    }

    private void updateQuestion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Question question = buildQuestionFromRequest(request);
        String validationError = validateQuestion(question);

        if (validationError != null) {
            redirectToQuestions(request, response, "error", validationError, question.getExamId());
            return;
        }

        boolean success = questionDAO.updateQuestion(getServletContext(), question);

        if (success) {
            redirectToQuestions(request, response, "success", "questionUpdated", question.getExamId());
        } else {
            redirectToQuestions(request, response, "error", "questionUpdateFailed", question.getExamId());
        }
    }

    private void deleteQuestion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String questionId = firstNonBlank(
                request.getParameter("recordId"),
                request.getParameter("questionId"),
                request.getParameter("id")
        );

        String examId = FileUtil.clean(request.getParameter("examId"));

        deleteQuestionById(request, response, questionId, examId);
    }

    /**
     * Backward-compatible GET delete.
     * Prefer POST delete in professional UI.
     */
    private void deleteQuestionFromGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String questionId = firstNonBlank(
                request.getParameter("id"),
                request.getParameter("recordId"),
                request.getParameter("questionId")
        );

        String examId = FileUtil.clean(request.getParameter("examId"));

        deleteQuestionById(request, response, questionId, examId);
    }

    private void deleteQuestionById(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String questionId,
                                    String examId)
            throws IOException {

        String cleanQuestionId = FileUtil.clean(questionId);
        String cleanExamId = FileUtil.clean(examId);

        if (cleanQuestionId.isEmpty()) {
            redirectToQuestions(request, response, "error", "missingQuestionId", cleanExamId);
            return;
        }

        Question existingQuestion = questionDAO.getQuestionById(getServletContext(), cleanQuestionId);

        if (existingQuestion == null) {
            redirectToQuestions(request, response, "error", "questionNotFound", cleanExamId);
            return;
        }

        if (cleanExamId.isEmpty()) {
            cleanExamId = existingQuestion.getExamId();
        }

        boolean success = questionDAO.deleteQuestion(getServletContext(), cleanQuestionId);

        if (success) {
            redirectToQuestions(request, response, "success", "questionDeleted", cleanExamId);
        } else {
            redirectToQuestions(request, response, "error", "questionDeleteFailed", cleanExamId);
        }
    }

    private Question buildQuestionFromRequest(HttpServletRequest request) {
        String questionType = FileUtil.clean(request.getParameter("questionType"));
        String status = FileUtil.clean(request.getParameter("status"));

        String optionA = FileUtil.clean(request.getParameter("optionA"));
        String optionB = FileUtil.clean(request.getParameter("optionB"));
        String optionC = FileUtil.clean(request.getParameter("optionC"));
        String optionD = FileUtil.clean(request.getParameter("optionD"));
        String correctAnswer = FileUtil.clean(request.getParameter("correctAnswer"));
        String modelAnswer = FileUtil.clean(request.getParameter("modelAnswer"));

        if (Question.TYPE_ESSAY.equalsIgnoreCase(questionType)) {
            optionA = "";
            optionB = "";
            optionC = "";
            optionD = "";
            correctAnswer = "";
        }

        if (Question.TYPE_MCQ.equalsIgnoreCase(questionType) && modelAnswer.isEmpty()) {
            modelAnswer = "No explanation provided.";
        }

        return new Question(
                FileUtil.clean(request.getParameter("questionId")),
                FileUtil.clean(request.getParameter("examId")),
                questionType,
                FileUtil.clean(request.getParameter("questionText")),
                optionA,
                optionB,
                optionC,
                optionD,
                correctAnswer,
                FileUtil.clean(request.getParameter("marks")),
                status,
                modelAnswer
        );
    }

    private String validateQuestion(Question question) {
        if (question == null) {
            return "invalidQuestion";
        }

        if (question.getQuestionId().isEmpty()) {
            return "missingQuestionId";
        }

        if (question.getExamId().isEmpty()) {
            return "missingExamId";
        }

        if (question.getQuestionType().isEmpty()) {
            return "missingQuestionType";
        }

        if (!question.isValidQuestionType()) {
            return "invalidQuestionType";
        }

        if (question.getQuestionText().isEmpty()) {
            return "missingQuestionText";
        }

        if (question.getMarks().isEmpty()) {
            return "missingMarks";
        }

        if (!question.isValidMarks()) {
            return "invalidMarks";
        }

        if (question.getStatus().isEmpty()) {
            return "missingStatus";
        }

        if (!question.isValidStatus()) {
            return "invalidStatus";
        }

        if (question.isMcq()) {
            if (question.getOptionA().isEmpty()) {
                return "missingOptionA";
            }

            if (question.getOptionB().isEmpty()) {
                return "missingOptionB";
            }

            if (question.getOptionC().isEmpty()) {
                return "missingOptionC";
            }

            if (question.getOptionD().isEmpty()) {
                return "missingOptionD";
            }

            if (question.getCorrectAnswer().isEmpty()) {
                return "missingCorrectAnswer";
            }

            if (!question.hasValidCorrectAnswer()) {
                return "invalidCorrectAnswer";
            }
        }

        if (question.isEssay() && question.getModelAnswer().isEmpty()) {
            return "missingModelAnswer";
        }

        if (!question.isCompleteForSave()) {
            return "incompleteQuestion";
        }

        return null;
    }

    private void redirectToQuestions(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String messageType,
                                     String messageCode,
                                     String examId)
            throws IOException {

        StringBuilder redirectUrl = new StringBuilder();

        redirectUrl.append(request.getContextPath())
                .append("/questions?")
                .append(urlEncode(messageType))
                .append("=")
                .append(urlEncode(messageCode));

        String cleanExamId = FileUtil.clean(examId);

        if (!cleanExamId.isEmpty()) {
            redirectUrl.append("&examId=")
                    .append(urlEncode(cleanExamId));
        }

        response.sendRedirect(redirectUrl.toString());
    }

    private void prepareRequestResponse(HttpServletRequest request,
                                        HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
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

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}