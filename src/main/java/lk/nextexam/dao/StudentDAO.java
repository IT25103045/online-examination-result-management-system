package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional DAO for Student Management.
 *
 * Storage file:
 * students.txt
 *
 * Format:
 * studentId|name|email|course|batch|contact|examStatus
 */
public class StudentDAO {

    private static final String FILE_NAME = "students.txt";

    public List<Student> getAllStudents(ServletContext context) {
        List<Student> students = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Student student = Student.fromFileString(line);

            if (student != null && !student.getStudentId().isEmpty()) {
                students.add(student);
            }
        }

        students.sort(Comparator.comparing(Student::getStudentId, String.CASE_INSENSITIVE_ORDER));
        return students;
    }

    public Student getStudentById(ServletContext context, String studentId) {
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return null;
        }

        for (Student student : getAllStudents(context)) {
            if (student.getStudentId().equalsIgnoreCase(cleanStudentId)) {
                return student;
            }
        }

        return null;
    }

    public Student getStudentByEmail(ServletContext context, String email) {
        String cleanEmail = FileUtil.clean(email);

        if (cleanEmail.isEmpty()) {
            return null;
        }

        for (Student student : getAllStudents(context)) {
            if (student.getEmail().equalsIgnoreCase(cleanEmail)) {
                return student;
            }
        }

        return null;
    }

    public List<Student> getStudentsByBatch(ServletContext context, String batch) {
        List<Student> selectedStudents = new ArrayList<>();
        String cleanBatch = FileUtil.clean(batch);

        if (cleanBatch.isEmpty()) {
            return selectedStudents;
        }

        for (Student student : getAllStudents(context)) {
            if (student.getBatch().equalsIgnoreCase(cleanBatch)) {
                selectedStudents.add(student);
            }
        }

        selectedStudents.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
        return selectedStudents;
    }

    public List<Student> getStudentsByStatus(ServletContext context, String examStatus) {
        List<Student> selectedStudents = new ArrayList<>();
        String cleanStatus = FileUtil.clean(examStatus);

        if (cleanStatus.isEmpty()) {
            return selectedStudents;
        }

        for (Student student : getAllStudents(context)) {
            if (student.getExamStatus().equalsIgnoreCase(cleanStatus)) {
                selectedStudents.add(student);
            }
        }

        selectedStudents.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
        return selectedStudents;
    }

    public List<Student> getEligibleStudents(ServletContext context) {
        return getStudentsByStatus(context, Student.STATUS_ELIGIBLE);
    }

    public List<Student> getPendingStudents(ServletContext context) {
        return getStudentsByStatus(context, Student.STATUS_PENDING);
    }

    public List<Student> getBlockedStudents(ServletContext context) {
        return getStudentsByStatus(context, Student.STATUS_BLOCKED);
    }

    public boolean addStudent(ServletContext context, Student student) {
        if (!isValidForCreate(context, student)) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, student.toFileString());
    }

    public boolean updateStudent(ServletContext context, Student student) {
        if (!isValidForUpdate(context, student)) {
            return false;
        }

        return FileUtil.updateLineById(
                context,
                FILE_NAME,
                student.getStudentId(),
                student.toFileString()
        );
    }

    public boolean deleteStudent(ServletContext context, String studentId) {
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return false;
        }

        Student existingStudent = getStudentById(context, cleanStudentId);

        if (existingStudent == null) {
            return false;
        }

        return FileUtil.deleteLineById(context, FILE_NAME, cleanStudentId);
    }

    public boolean updateStudentStatus(ServletContext context, String studentId, String examStatus) {
        Student student = getStudentById(context, studentId);

        if (student == null) {
            return false;
        }

        student.setExamStatus(examStatus);

        if (!student.isValidExamStatus()) {
            return false;
        }

        return updateStudent(context, student);
    }

    public boolean markEligible(ServletContext context, String studentId) {
        return updateStudentStatus(context, studentId, Student.STATUS_ELIGIBLE);
    }

    public boolean markPending(ServletContext context, String studentId) {
        return updateStudentStatus(context, studentId, Student.STATUS_PENDING);
    }

    public boolean markBlocked(ServletContext context, String studentId) {
        return updateStudentStatus(context, studentId, Student.STATUS_BLOCKED);
    }

    public boolean canStudentAttemptExam(ServletContext context, String studentId) {
        Student student = getStudentById(context, studentId);

        if (student == null) {
            return false;
        }

        return student.canAttemptExam();
    }

    public String getStudentAttemptValidationMessage(ServletContext context, String studentId) {
        Student student = getStudentById(context, studentId);

        if (student == null) {
            return "Student record not found.";
        }

        if (student.isBlocked()) {
            return "This student is blocked from attempting exams.";
        }

        if (student.isPending()) {
            return "This student is still pending eligibility review.";
        }

        if (!student.isEligible()) {
            return "This student is not eligible for online exams.";
        }

        return "OK";
    }

    public int countAllStudents(ServletContext context) {
        return getAllStudents(context).size();
    }

    public int countEligibleStudents(ServletContext context) {
        return getEligibleStudents(context).size();
    }

    public int countPendingStudents(ServletContext context) {
        return getPendingStudents(context).size();
    }

    public int countBlockedStudents(ServletContext context) {
        return getBlockedStudents(context).size();
    }

    public int countByBatch(ServletContext context, String batch) {
        return getStudentsByBatch(context, batch).size();
    }

    public int countYearOneStudents(ServletContext context) {
        return countByBatch(context, Student.BATCH_Y1S1)
                + countByBatch(context, Student.BATCH_Y1S2);
    }

    public int countYearTwoStudents(ServletContext context) {
        return countByBatch(context, Student.BATCH_Y2S1)
                + countByBatch(context, Student.BATCH_Y2S2);
    }

    public int countYearThreeStudents(ServletContext context) {
        return countByBatch(context, Student.BATCH_Y3S1)
                + countByBatch(context, Student.BATCH_Y3S2);
    }

    public int countYearFourStudents(ServletContext context) {
        return countByBatch(context, Student.BATCH_Y4S1)
                + countByBatch(context, Student.BATCH_Y4S2);
    }

    public int calculateEligibilityRate(ServletContext context) {
        int total = countAllStudents(context);

        if (total == 0) {
            return 0;
        }

        return (countEligibleStudents(context) * 100) / total;
    }

    public boolean existsById(ServletContext context, String studentId) {
        return FileUtil.existsById(context, FILE_NAME, studentId);
    }

    public boolean existsByEmail(ServletContext context, String email) {
        return getStudentByEmail(context, email) != null;
    }

    private boolean isValidForCreate(ServletContext context, Student student) {
        if (!isStudentObjectValid(student)) {
            return false;
        }

        if (FileUtil.existsById(context, FILE_NAME, student.getStudentId())) {
            return false;
        }

        /*
         * Professional rule:
         * Avoid duplicate email records because email is a student identity field.
         */
        return getStudentByEmail(context, student.getEmail()) == null;
    }

    private boolean isValidForUpdate(ServletContext context, Student student) {
        if (!isStudentObjectValid(student)) {
            return false;
        }

        Student existingStudent = getStudentById(context, student.getStudentId());

        if (existingStudent == null) {
            return false;
        }

        Student duplicateEmailStudent = getStudentByEmail(context, student.getEmail());

        if (duplicateEmailStudent != null
                && !duplicateEmailStudent.getStudentId().equalsIgnoreCase(student.getStudentId())) {
            return false;
        }

        return true;
    }

    private boolean isStudentObjectValid(Student student) {
        if (student == null) {
            return false;
        }

        return student.isCompleteForSave();
    }
}