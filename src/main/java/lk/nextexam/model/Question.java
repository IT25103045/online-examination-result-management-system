package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * Question model for NextExamLK.
 *
 * Current storage format:
 * questionId|examId|questionType|questionText|optionA|optionB|optionC|optionD|correctAnswer|marks|status|modelAnswer
 *
 * Supported question types:
 * - MCQ
 * - Essay
 *
 * Supported statuses:
 * - Draft
 * - Active
 * - Published
 * - Inactive
 * - Archived
 */
public class Question {

    public static final String TYPE_MCQ = "MCQ";
    public static final String TYPE_ESSAY = "Essay";

    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_PUBLISHED = "Published";
    public static final String STATUS_INACTIVE = "Inactive";
    public static final String STATUS_ARCHIVED = "Archived";

    private static final double MIN_MARKS = 0.5;
    private static final double MAX_MARKS = 100.0;

    private String questionId;
    private String examId;
    private String questionType;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String marks;
    private String status;
    private String modelAnswer;

    public Question() {
    }

    public Question(String questionId,
                    String examId,
                    String questionType,
                    String questionText,
                    String optionA,
                    String optionB,
                    String optionC,
                    String optionD,
                    String correctAnswer,
                    String marks,
                    String status,
                    String modelAnswer) {
        this.questionId = questionId;
        this.examId = examId;
        this.questionType = questionType;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
        this.status = status;
        this.modelAnswer = modelAnswer;
    }

    public String getQuestionId() {
        return safe(questionId);
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getExamId() {
        return safe(examId);
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getQuestionType() {
        return normalizeQuestionType(questionType);
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return safe(questionText);
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return safe(optionA);
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return safe(optionB);
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return safe(optionC);
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return safe(optionD);
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return safe(correctAnswer).toUpperCase();
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getMarks() {
        return safe(marks);
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModelAnswer() {
        return safe(modelAnswer);
    }

    public void setModelAnswer(String modelAnswer) {
        this.modelAnswer = modelAnswer;
    }

    /*
     * Type helpers
     */
    public boolean isMcq() {
        return TYPE_MCQ.equalsIgnoreCase(getQuestionType());
    }

    public boolean isEssay() {
        return TYPE_ESSAY.equalsIgnoreCase(getQuestionType());
    }

    public boolean isValidQuestionType() {
        return isMcq() || isEssay();
    }

    /*
     * Status helpers
     */
    public boolean isDraft() {
        return STATUS_DRAFT.equalsIgnoreCase(getStatus());
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(getStatus());
    }

    public boolean isPublished() {
        return STATUS_PUBLISHED.equalsIgnoreCase(getStatus());
    }

    public boolean isInactive() {
        return STATUS_INACTIVE.equalsIgnoreCase(getStatus());
    }

    public boolean isArchived() {
        return STATUS_ARCHIVED.equalsIgnoreCase(getStatus());
    }

    public boolean isValidStatus() {
        return isDraft() || isActive() || isPublished() || isInactive() || isArchived();
    }

    /**
     * Student visibility rule.
     *
     * Professional compatibility:
     * - Active questions appear in active exams.
     * - Published questions can also appear if older data used Published status.
     */
    public boolean isVisibleToStudent() {
        return isActive() || isPublished();
    }

    public boolean canEdit() {
        return !isArchived();
    }

    public boolean canAutoMark() {
        return isMcq() && hasValidCorrectAnswer();
    }

    public boolean requiresManualMarking() {
        return isEssay();
    }

    /*
     * Marks helpers
     */
    public double getMarksAsDouble() {
        try {
            return Double.parseDouble(getMarks());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean isValidMarks() {
        double value = getMarksAsDouble();
        return value >= MIN_MARKS && value <= MAX_MARKS;
    }

    public String getDisplayMarks() {
        double value = getMarksAsDouble();

        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    /*
     * MCQ helpers
     */
    public boolean hasValidCorrectAnswer() {
        String answer = getCorrectAnswer();

        return "A".equals(answer)
                || "B".equals(answer)
                || "C".equals(answer)
                || "D".equals(answer);
    }

    public boolean hasAllMcqOptions() {
        return !getOptionA().isEmpty()
                && !getOptionB().isEmpty()
                && !getOptionC().isEmpty()
                && !getOptionD().isEmpty();
    }

    public String getOptionByLetter(String letter) {
        String value = FileUtil.clean(letter).toUpperCase();

        if ("A".equals(value)) {
            return getOptionA();
        }

        if ("B".equals(value)) {
            return getOptionB();
        }

        if ("C".equals(value)) {
            return getOptionC();
        }

        if ("D".equals(value)) {
            return getOptionD();
        }

        return "";
    }

    public boolean isCorrectMcqAnswer(String submittedAnswer) {
        if (!isMcq()) {
            return false;
        }

        String answer = FileUtil.clean(submittedAnswer).toUpperCase();

        return !answer.isEmpty()
                && hasValidCorrectAnswer()
                && answer.equals(getCorrectAnswer());
    }

    /*
     * Validation helpers
     */
    public boolean isCompleteForSave() {
        if (getQuestionId().isEmpty()
                || getExamId().isEmpty()
                || getQuestionText().isEmpty()
                || !isValidQuestionType()
                || !isValidMarks()
                || !isValidStatus()) {
            return false;
        }

        if (isMcq()) {
            return hasAllMcqOptions() && hasValidCorrectAnswer();
        }

        if (isEssay()) {
            /*
             * Model answer is strongly recommended for marking consistency.
             */
            return !getModelAnswer().isEmpty();
        }

        return false;
    }

    public boolean isReadyForStudent() {
        return isCompleteForSave() && isVisibleToStudent();
    }

    /*
     * UI helpers
     */
    public String getTypeBadgeClass() {
        if (isMcq()) {
            return "badge-soft-primary";
        }

        if (isEssay()) {
            return "badge-soft-warning";
        }

        return "badge-soft-secondary";
    }

    public String getStatusBadgeClass() {
        if (isActive() || isPublished()) {
            return "badge-soft-success";
        }

        if (isDraft()) {
            return "badge-soft-warning";
        }

        if (isInactive()) {
            return "badge-soft-secondary";
        }

        if (isArchived()) {
            return "badge-soft-danger";
        }

        return "badge-soft-secondary";
    }

    public String getShortQuestionText() {
        String text = getQuestionText();

        if (text.length() <= 90) {
            return text;
        }

        return text.substring(0, 90) + "...";
    }

    /*
     * File serialization
     */
    public String toFileString() {
        String storedOptionA = isMcq() ? getOptionA() : "";
        String storedOptionB = isMcq() ? getOptionB() : "";
        String storedOptionC = isMcq() ? getOptionC() : "";
        String storedOptionD = isMcq() ? getOptionD() : "";
        String storedCorrectAnswer = isMcq() ? getCorrectAnswer() : "";

        return FileUtil.clean(getQuestionId()) + "|"
                + FileUtil.clean(getExamId()) + "|"
                + FileUtil.clean(getQuestionType()) + "|"
                + FileUtil.clean(getQuestionText()) + "|"
                + FileUtil.clean(storedOptionA) + "|"
                + FileUtil.clean(storedOptionB) + "|"
                + FileUtil.clean(storedOptionC) + "|"
                + FileUtil.clean(storedOptionD) + "|"
                + FileUtil.clean(storedCorrectAnswer) + "|"
                + FileUtil.clean(getDisplayMarks()) + "|"
                + FileUtil.clean(getStatus()) + "|"
                + FileUtil.clean(getModelAnswer());
    }

    public static Question fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 12) {
            return null;
        }

        return new Question(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6],
                data[7],
                data[8],
                data[9],
                data[10],
                data[11]
        );
    }

    private String normalizeQuestionType(String value) {
        String type = safe(value);

        if (TYPE_MCQ.equalsIgnoreCase(type)
                || "Multiple Choice".equalsIgnoreCase(type)
                || "Multiple Choice Question".equalsIgnoreCase(type)) {
            return TYPE_MCQ;
        }

        if (TYPE_ESSAY.equalsIgnoreCase(type)
                || "Structured".equalsIgnoreCase(type)
                || "Written".equalsIgnoreCase(type)
                || "Theory".equalsIgnoreCase(type)) {
            return TYPE_ESSAY;
        }

        return type;
    }

    private String normalizeStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_DRAFT.equalsIgnoreCase(statusValue)) {
            return STATUS_DRAFT;
        }

        if (STATUS_ACTIVE.equalsIgnoreCase(statusValue)) {
            return STATUS_ACTIVE;
        }

        if (STATUS_PUBLISHED.equalsIgnoreCase(statusValue)) {
            return STATUS_PUBLISHED;
        }

        if (STATUS_INACTIVE.equalsIgnoreCase(statusValue)) {
            return STATUS_INACTIVE;
        }

        if (STATUS_ARCHIVED.equalsIgnoreCase(statusValue)) {
            return STATUS_ARCHIVED;
        }

        return statusValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}