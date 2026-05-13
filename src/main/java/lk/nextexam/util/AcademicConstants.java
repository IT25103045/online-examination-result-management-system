package lk.nextexam.util;

import java.util.Arrays;
import java.util.List;

public class AcademicConstants {

    public static final List<String> SEMESTERS = Arrays.asList(
            "Y1S1", "Y1S2",
            "Y2S1", "Y2S2",
            "Y3S1", "Y3S2",
            "Y4S1", "Y4S2"
    );

    public static final List<String> FACULTIES = Arrays.asList(
            "Faculty of Computing",
            "Faculty of Engineering"
    );

    public static boolean isValidSemester(String semester) {
        return semester != null && SEMESTERS.contains(semester.trim());
    }

    public static boolean isValidFaculty(String faculty) {
        return faculty != null && FACULTIES.contains(faculty.trim());
    }

    public static String cleanSemester(String semester) {
        if (semester == null) {
            return "";
        }
        return semester.trim().toUpperCase();
    }

    private AcademicConstants() {
    }
}