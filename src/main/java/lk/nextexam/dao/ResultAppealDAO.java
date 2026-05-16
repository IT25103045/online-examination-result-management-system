package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ResultAppeal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultAppealDAO manages result appeal/recheck request records.
 *
 * Storage file:
 * result_appeals.txt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ResultAppealDAO {

    private static final String FILE_NAME = "result_appeals.txt";

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<ResultAppeal> getAllAppeals(ServletContext context) {
        List<ResultAppeal> appeals = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            ResultAppeal appeal = ResultAppeal.fromFileString(line);

            if (appeal != null && !appeal.getAppealId().isEmpty()) {
                appeals.add(appeal);
            }
        }

        return appeals;
    }

    public ResultAppeal getAppealById(ServletContext context, String appealId) {
        String cleanAppealId = FileUtil.clean(appealId);

        if (cleanAppealId.isEmpty()) {
            return null;
        }

        for (ResultAppeal appeal : getAllAppeals(context)) {
            if (appeal.getAppealId().equalsIgnoreCase(cleanAppealId)) {
                return appeal;
            }
        }

        return null;
    }

    public List<ResultAppeal> getAppealsByStudent(ServletContext context, String studentId) {
        List<ResultAppeal> selectedAppeals = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedAppeals;
        }

        for (ResultAppeal appeal : getAllAppeals(context)) {
            if (appeal.getStudentId().equalsIgnoreCase(cleanStudentId)) {
                selectedAppeals.add(appeal);
            }
        }

        return selectedAppeals;
    }

    public List<ResultAppeal> getAppealsByStatus(ServletContext context, String status) {
        List<ResultAppeal> selectedAppeals = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedAppeals;
        }

        for (ResultAppeal appeal : getAllAppeals(context)) {
            if (appeal.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedAppeals.add(appeal);
            }
        }

        return selectedAppeals;
    }

    public ResultAppeal getAppealByStudentAndResult(ServletContext context,
                                                    String studentId,
                                                    String resultId) {
        String cleanStudentId = FileUtil.clean(studentId);
        String cleanResultId = FileUtil.clean(resultId);

        if (cleanStudentId.isEmpty() || cleanResultId.isEmpty()) {
            return null;
        }

        for (ResultAppeal appeal : getAppealsByStudent(context, cleanStudentId)) {
            if (appeal.getResultId().equalsIgnoreCase(cleanResultId)) {
                return appeal;
            }
        }

        return null;
    }

    public boolean hasStudentAppealedResult(ServletContext context,
                                            String studentId,
                                            String resultId) {
        return getAppealByStudentAndResult(context, studentId, resultId) != null;
    }

    public boolean addAppeal(ServletContext context, ResultAppeal appeal) {
        if (appeal == null) {
            return false;
        }

        if (appeal.getAppealId().isEmpty()) {
            appeal.setAppealId(FileUtil.generateId("RA"));
        }

        if (!appeal.isCompleteForSave()) {
            return false;
        }

        if (hasStudentAppealedResult(context, appeal.getStudentId(), appeal.getResultId())) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, appeal.toFileString());
    }

    public boolean updateAppeal(ServletContext context, ResultAppeal appeal) {
        if (appeal == null || appeal.getAppealId().isEmpty()) {
            return false;
        }

        if (!appeal.isCompleteForSave()) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                appeal.getAppealId(),
                appeal.toFileString()
        );
    }

    public int countAll(ServletContext context) {
        return getAllAppeals(context).size();
    }

    public int countByStatus(ServletContext context, String status) {
        return getAppealsByStatus(context, status).size();
    }

    public String now() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME);
    }
}