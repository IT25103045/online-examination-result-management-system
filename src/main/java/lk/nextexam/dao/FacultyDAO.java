package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Faculty;

import java.util.ArrayList;
import java.util.List;

public class FacultyDAO {

    private static final String FILE_NAME = "faculties.txt";

    public List<Faculty> getAllFaculties(ServletContext context) {
        List<Faculty> faculties = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            Faculty faculty = Faculty.fromFileString(line);
            if (faculty != null) {
                faculties.add(faculty);
            }
        }

        return faculties;
    }

    public Faculty getFacultyById(ServletContext context, String facultyId) {
        for (Faculty faculty : getAllFaculties(context)) {
            if (faculty.getFacultyId().equalsIgnoreCase(facultyId)) {
                return faculty;
            }
        }
        return null;
    }

    public boolean addFaculty(ServletContext context, Faculty faculty) {
        if (FileUtil.existsById(context, FILE_NAME, faculty.getFacultyId())) {
            return false;
        }
        return FileUtil.appendLine(context, FILE_NAME, faculty.toFileString());
    }

    public boolean updateFaculty(ServletContext context, Faculty faculty) {
        return FileUtil.updateLineById(context, FILE_NAME, faculty.getFacultyId(), faculty.toFileString());
    }

    public boolean deleteFaculty(ServletContext context, String facultyId) {
        return FileUtil.deleteLineById(context, FILE_NAME, facultyId);
    }
}