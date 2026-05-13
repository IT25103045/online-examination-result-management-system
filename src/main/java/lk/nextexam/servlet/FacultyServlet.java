package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.nextexam.dao.FacultyDAO;
import lk.nextexam.model.Faculty;

import java.io.IOException;
import java.util.List;

@WebServlet("/faculties")
public class FacultyServlet extends HttpServlet {

    private final FacultyDAO facultyDAO = new FacultyDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("delete".equalsIgnoreCase(action)) {
            String facultyId = request.getParameter("id");
            boolean deleted = facultyDAO.deleteFaculty(getServletContext(), facultyId);

            if (deleted) {
                response.sendRedirect(request.getContextPath() + "/faculties?success=deleted");
            } else {
                response.sendRedirect(request.getContextPath() + "/faculties?error=deleteFailed");
            }
            return;
        }

        List<Faculty> faculties = facultyDAO.getAllFaculties(getServletContext());
        request.setAttribute("faculties", faculties);
        request.getRequestDispatcher("/faculties/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String action = request.getParameter("action");

        String facultyId = request.getParameter("facultyId");
        String facultyName = request.getParameter("facultyName");
        String deanName = request.getParameter("deanName");
        String contactEmail = request.getParameter("contactEmail");
        String status = request.getParameter("status");

        Faculty faculty = new Faculty(
                facultyId,
                facultyName,
                deanName,
                contactEmail,
                status
        );

        boolean success;

        if ("update".equalsIgnoreCase(action)) {
            success = facultyDAO.updateFaculty(getServletContext(), faculty);
            response.sendRedirect(request.getContextPath() + "/faculties?" + (success ? "success=updated" : "error=updateFailed"));
        } else {
            success = facultyDAO.addFaculty(getServletContext(), faculty);
            response.sendRedirect(request.getContextPath() + "/faculties?" + (success ? "success=added" : "error=addFailed"));
        }
    }
}