package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ExamIntegrityLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ExamIntegrityLogDAO manages exam integrity log file handling.
 *
 * Storage file:
 * exam_integrity_logs.txt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ExamIntegrityLogDAO {

    private static final String FILE_NAME = "exam_integrity_logs.txt";

    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean addLog(ServletContext context,
                          String studentId,
                          String examId,
                          String eventType,
                          String description) {

        ExamIntegrityLog log = new ExamIntegrityLog(
                FileUtil.generateId("INT"),
                FileUtil.clean(studentId),
                FileUtil.clean(examId),
                FileUtil.clean(eventType).toUpperCase(),
                FileUtil.clean(description),
                now()
        );

        if (!log.isCompleteForSave()) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, log.toFileString());
    }

    public List<ExamIntegrityLog> getAllLogs(ServletContext context) {
        List<ExamIntegrityLog> logs = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            ExamIntegrityLog log = ExamIntegrityLog.fromFileString(line);

            if (log != null && !log.getLogId().isEmpty()) {
                logs.add(log);
            }
        }

        return logs;
    }

    public List<ExamIntegrityLog> getLogsByStudentAndExam(ServletContext context,
                                                          String studentId,
                                                          String examId) {
        List<ExamIntegrityLog> selectedLogs = new ArrayList<>();

        String cleanStudentId = FileUtil.clean(studentId);
        String cleanExamId = FileUtil.clean(examId);

        for (ExamIntegrityLog log : getAllLogs(context)) {
            boolean studentMatches = log.getStudentId().equalsIgnoreCase(cleanStudentId);
            boolean examMatches = log.getExamId().equalsIgnoreCase(cleanExamId);

            if (studentMatches && examMatches) {
                selectedLogs.add(log);
            }
        }

        return selectedLogs;
    }

    public int countLogsByStudentAndExam(ServletContext context,
                                         String studentId,
                                         String examId) {
        return getLogsByStudentAndExam(context, studentId, examId).size();
    }

    public String now() {
        return LocalDateTime.now().format(DISPLAY_DATE_TIME);
    }
}