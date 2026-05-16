package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ManualMark;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ManualMarkDAO manages essay/manual marking records.
 *
 * Storage file:
 * manual_marks.txt
 *
 * Format:
 * markId|submissionId|examId|studentId|questionId|marksAwarded|feedback|markedBy|markedAt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ManualMarkDAO {

    private static final String FILE_NAME = "manual_marks.txt";

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<ManualMark> getAllMarks(ServletContext context) {
        List<ManualMark> marks = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            ManualMark mark = ManualMark.fromFileString(line);

            if (mark != null && !mark.getMarkId().isEmpty()) {
                marks.add(mark);
            }
        }

        return marks;
    }

    public ManualMark getMarkById(ServletContext context, String markId) {
        String cleanMarkId = FileUtil.clean(markId);

        if (cleanMarkId.isEmpty()) {
            return null;
        }

        for (ManualMark mark : getAllMarks(context)) {
            if (mark.getMarkId().equalsIgnoreCase(cleanMarkId)) {
                return mark;
            }
        }

        return null;
    }

    public List<ManualMark> getMarksBySubmission(ServletContext context, String submissionId) {
        List<ManualMark> selectedMarks = new ArrayList<>();
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return selectedMarks;
        }

        for (ManualMark mark : getAllMarks(context)) {
            if (mark.getSubmissionId().equalsIgnoreCase(cleanSubmissionId)) {
                selectedMarks.add(mark);
            }
        }

        return selectedMarks;
    }

    public ManualMark getMarkBySubmissionAndQuestion(ServletContext context,
                                                     String submissionId,
                                                     String questionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);
        String cleanQuestionId = FileUtil.clean(questionId);

        if (cleanSubmissionId.isEmpty() || cleanQuestionId.isEmpty()) {
            return null;
        }

        for (ManualMark mark : getMarksBySubmission(context, cleanSubmissionId)) {
            if (mark.getQuestionId().equalsIgnoreCase(cleanQuestionId)) {
                return mark;
            }
        }

        return null;
    }

    public boolean saveOrUpdateMark(ServletContext context, ManualMark mark) {
        if (mark == null) {
            return false;
        }

        ManualMark existingMark = getMarkBySubmissionAndQuestion(
                context,
                mark.getSubmissionId(),
                mark.getQuestionId()
        );

        if (existingMark != null) {
            mark.setMarkId(existingMark.getMarkId());
            return updateMark(context, mark);
        }

        return addMark(context, mark);
    }

    public boolean addMark(ServletContext context, ManualMark mark) {
        if (!isValidForSave(mark)) {
            return false;
        }

        if (mark.getMarkId().isEmpty()) {
            mark.setMarkId(FileUtil.generateId("MM"));
        }

        return FileUtil.appendLine(context, FILE_NAME, mark.toFileString());
    }

    public boolean updateMark(ServletContext context, ManualMark mark) {
        if (!isValidForSave(mark)) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                mark.getMarkId(),
                mark.toFileString()
        );
    }

    public double getTotalAwardedMarksBySubmission(ServletContext context, String submissionId) {
        double total = 0.0;

        for (ManualMark mark : getMarksBySubmission(context, submissionId)) {
            total += mark.getMarksAwardedAsDouble();
        }

        return total;
    }

    public int countMarksBySubmission(ServletContext context, String submissionId) {
        return getMarksBySubmission(context, submissionId).size();
    }

    public String now() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME);
    }

    private boolean isValidForSave(ManualMark mark) {
        return mark != null && mark.isCompleteForSave();
    }
}