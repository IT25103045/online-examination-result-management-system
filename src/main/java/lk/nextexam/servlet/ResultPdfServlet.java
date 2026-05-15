package lk.nextexam.servlet;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ActivityLogDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.ResultDAO;
import lk.nextexam.dao.StudentDAO;
import lk.nextexam.model.Result;
import lk.nextexam.model.Student;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ResultPdfServlet generates a downloadable PDF result sheet.
 *
 * URL:
 * /result-pdf?id=RESULT_ID
 *
 * Security:
 * - Admin and Lecturer can download any result.
 * - Student can download only their own published result.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/result-pdf")
public class ResultPdfServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";
    private static final String ROLE_STUDENT = "Student";

    private final ResultDAO resultDAO = new ResultDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String resultId = FileUtil.clean(request.getParameter("id"));

        if (FileUtil.isBlank(resultId)) {
            redirectBack(request, response, "missingResultId");
            return;
        }

        Result result = resultDAO.getResultById(getServletContext(), resultId);

        if (result == null) {
            redirectBack(request, response, "resultNotFound");
            return;
        }

        String userRole = getSessionValue(session, "userRole");
        String userId = getSessionValue(session, "userId");

        if (!canDownloadResult(userRole, userId, result)) {
            redirectBack(request, response, "accessDenied");
            return;
        }

        Student student = studentDAO.getStudentById(getServletContext(), result.getStudentId());

        String studentName = student != null ? student.getDisplayName() : result.getStudentId();
        String course = student != null ? student.getCourse() : "Not available";
        String batch = student != null ? student.getBatch() : "Not available";

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"Nextexam_Result_" + safeFileName(result.getResultId()) + ".pdf\""
        );

        generatePdf(response, result, studentName, course, batch);

        activityLogDAO.addLog(
                getServletContext(),
                userId,
                userRole,
                "RESULT_PDF_DOWNLOAD",
                getSessionValue(session, "displayName") + " downloaded result PDF " + result.getResultId()
        );
    }

    private boolean canDownloadResult(String userRole, String userId, Result result) {
        if (ROLE_ADMIN.equalsIgnoreCase(userRole) || ROLE_LECTURER.equalsIgnoreCase(userRole)) {
            return true;
        }

        if (ROLE_STUDENT.equalsIgnoreCase(userRole)) {
            return result.isPublished()
                    && result.getStudentId().equalsIgnoreCase(userId);
        }

        return false;
    }

    private void generatePdf(HttpServletResponse response,
                             Result result,
                             String studentName,
                             String course,
                             String batch)
            throws IOException {

        Document document = new Document(PageSize.A4, 44, 44, 42, 42);

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(15, 23, 42));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(37, 99, 235));
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(15, 23, 42));
            Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(100, 116, 139));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(71, 85, 105));
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(15, 23, 42));
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, getStatusColor(result));

            Paragraph title = new Paragraph("NextExamLK", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph subtitle = new Paragraph("Official Student Result Sheet", subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(18);
            document.add(subtitle);

            Paragraph generated = new Paragraph(
                    "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    mutedFont
            );
            generated.setAlignment(Element.ALIGN_RIGHT);
            generated.setSpacingAfter(12);
            document.add(generated);

            PdfPTable studentTable = new PdfPTable(2);
            studentTable.setWidthPercentage(100);
            studentTable.setWidths(new float[]{32, 68});
            studentTable.setSpacingAfter(16);

            addSectionHeader(studentTable, "Student Information");
            addRow(studentTable, "Student ID", result.getStudentId(), labelFont, valueFont);
            addRow(studentTable, "Student Name", studentName, labelFont, valueFont);
            addRow(studentTable, "Course", course, labelFont, valueFont);
            addRow(studentTable, "Batch", batch, labelFont, valueFont);

            document.add(studentTable);

            PdfPTable resultTable = new PdfPTable(2);
            resultTable.setWidthPercentage(100);
            resultTable.setWidths(new float[]{32, 68});
            resultTable.setSpacingAfter(18);

            addSectionHeader(resultTable, "Result Details");
            addRow(resultTable, "Result ID", result.getResultId(), labelFont, valueFont);
            addRow(resultTable, "Exam ID", result.getExamId(), labelFont, valueFont);
            addRow(resultTable, "Marks", result.getDisplayMarks() + " / 100", labelFont, valueFont);
            addRow(resultTable, "Grade", result.getGrade(), labelFont, valueFont);
            addRow(resultTable, "Status", result.getStatus(), labelFont, statusFont);
            addRow(resultTable, "Performance", result.getPerformanceLabel(), labelFont, valueFont);
            addRow(resultTable, "Verification", result.getVerification(), labelFont, valueFont);
            addRow(resultTable, "Published Status", result.getPublished(), labelFont, valueFont);

            document.add(resultTable);

            Paragraph noteTitle = new Paragraph("Academic Note", subTitleFont);
            noteTitle.setSpacingAfter(6);
            document.add(noteTitle);

            Paragraph note = new Paragraph(
                    "This result sheet is generated by the Nextexam online examination and academic management system. "
                            + "Only verified and published results are available for student download.",
                    normalFont
            );
            note.setAlignment(Element.ALIGN_JUSTIFIED);
            note.setSpacingAfter(20);
            document.add(note);

            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{50, 50});

            PdfPCell leftSign = new PdfPCell(new Phrase("\n\nAuthorized Officer\nNextExamLK", mutedFont));
            leftSign.setBorder(Rectangle.NO_BORDER);
            leftSign.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell rightSign = new PdfPCell(new Phrase("\n\nSystem Generated Report\nNo manual signature required", mutedFont));
            rightSign.setBorder(Rectangle.NO_BORDER);
            rightSign.setHorizontalAlignment(Element.ALIGN_RIGHT);

            footerTable.addCell(leftSign);
            footerTable.addCell(rightSign);

            document.add(footerTable);

        } catch (Exception e) {
            throw new IOException("PDF generation failed", e);
        } finally {
            document.close();
        }
    }

    private void addSectionHeader(PdfPTable table, String text) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);

        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setColspan(2);
        cell.setBackgroundColor(new Color(37, 99, 235));
        cell.setPadding(9);
        cell.setBorderColor(new Color(37, 99, 235));

        table.addCell(cell);
    }

    private void addRow(PdfPTable table,
                        String label,
                        String value,
                        Font labelFont,
                        Font valueFont) {

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new Color(248, 250, 252));
        labelCell.setBorderColor(new Color(226, 232, 240));

        PdfPCell valueCell = new PdfPCell(new Phrase(value == null ? "" : value, valueFont));
        valueCell.setPadding(8);
        valueCell.setBorderColor(new Color(226, 232, 240));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private Color getStatusColor(Result result) {
        if (result.isPass()) {
            return new Color(22, 163, 74);
        }

        if (result.isFail()) {
            return new Color(220, 38, 38);
        }

        return new Color(245, 158, 11);
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null
                && session.getAttribute("loggedUser") != null
                && session.getAttribute("userId") != null
                && session.getAttribute("userRole") != null
                && "authenticated".equals(String.valueOf(session.getAttribute("loginStatus")));
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safeFileName(String value) {
        String safe = FileUtil.clean(value);

        if (safe.isEmpty()) {
            return "result";
        }

        return safe.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private void redirectBack(HttpServletRequest request,
                              HttpServletResponse response,
                              String error)
            throws IOException {

        HttpSession session = request.getSession(false);
        String role = getSessionValue(session, "userRole");

        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/my-results?error=" + error);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/results?error=" + error);
    }
}