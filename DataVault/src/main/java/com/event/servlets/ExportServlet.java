package com.event.servlets;

import com.event.dao.RegistrationDAO;
import com.event.dao.AdminDAO;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Servlet for data export operations
 * Handles CSV and HTML export of registration data
 */
@WebServlet("/export")
public class ExportServlet extends HttpServlet {
    
    private RegistrationDAO registrationDAO;
    private AdminDAO adminDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        registrationDAO = new RegistrationDAO();
        adminDAO = new AdminDAO();
    }
    
    /**
     * Handle GET requests for data export
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check admin authentication
        if (!AdminLoginServlet.isAdminAuthenticated(request)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Unauthorized access. Please login first.");
            
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
            return;
        }
        
        String format = request.getParameter("format");
        String eventFilter = request.getParameter("event");
        String winnerFilter = request.getParameter("winner");
        
        if ("csv".equals(format)) {
            exportCSV(request, response, eventFilter, winnerFilter);
        } else if ("html".equals(format)) {
            exportHTML(request, response, eventFilter, winnerFilter);
        } else if ("json".equals(format)) {
            exportJSON(request, response, eventFilter, winnerFilter);
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid export format. Supported formats: csv, html, json");
            
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
        }
    }
    
    /**
     * Export data as CSV
     */
    private void exportCSV(HttpServletRequest request, HttpServletResponse response, 
                          String eventFilter, String winnerFilter) throws IOException {
        
        // Set response headers for CSV download
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "tarunyam_participants_" + timestamp + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Get filtered data
            List<Map<String, Object>> participants = getFilteredParticipants(eventFilter, winnerFilter);
            
            // Write CSV header
            out.println("ID,Name,Email,College,Event,Registration Date,Winner Status");
            
            // Write CSV data
            for (Map<String, Object> participant : participants) {
                out.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s%n",
                    participant.get("id"),
                    escapeCSV((String) participant.get("name")),
                    escapeCSV((String) participant.get("email")),
                    escapeCSV((String) participant.get("college")),
                    escapeCSV((String) participant.get("event")),
                    participant.get("registration_date"),
                    (Boolean) participant.get("winner_status") ? "Winner" : "Participant"
                );
            }
            
            // Log admin action
            Integer adminId = AdminLoginServlet.getAdminId(request);
            if (adminId != null) {
                String filterInfo = buildFilterInfo(eventFilter, winnerFilter);
                adminDAO.logAdminAction(adminId, "Exported CSV data" + filterInfo);
            }
            
        } catch (Exception e) {
            System.err.println("Error exporting CSV: " + e.getMessage());
            e.printStackTrace();
            
            // Reset response for error
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to export CSV data");
            
            out.print(jsonResponse.toString());
        } finally {
            out.flush();
        }
    }
    
    /**
     * Export data as HTML
     */
    private void exportHTML(HttpServletRequest request, HttpServletResponse response, 
                           String eventFilter, String winnerFilter) throws IOException {
        
        // Set response headers for HTML download
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "tarunyam_participants_" + timestamp + ".html";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Get filtered data
            List<Map<String, Object>> participants = getFilteredParticipants(eventFilter, winnerFilter);
            
            // Generate HTML
            generateHTMLReport(out, participants, eventFilter, winnerFilter);
            
            // Log admin action
            Integer adminId = AdminLoginServlet.getAdminId(request);
            if (adminId != null) {
                String filterInfo = buildFilterInfo(eventFilter, winnerFilter);
                adminDAO.logAdminAction(adminId, "Exported HTML report" + filterInfo);
            }
            
        } catch (Exception e) {
            System.err.println("Error exporting HTML: " + e.getMessage());
            e.printStackTrace();
            
            // Reset response for error
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to export HTML report");
            
            out.print(jsonResponse.toString());
        } finally {
            out.flush();
        }
    }
    
    /**
     * Export data as JSON
     */
    private void exportJSON(HttpServletRequest request, HttpServletResponse response, 
                           String eventFilter, String winnerFilter) throws IOException {
        
        // Set response headers for JSON download
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "tarunyam_participants_" + timestamp + ".json";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Get filtered data
            List<Map<String, Object>> participants = getFilteredParticipants(eventFilter, winnerFilter);
            
            // Create JSON response
            JSONObject exportData = new JSONObject();
            exportData.put("export_date", new Date());
            exportData.put("total_records", participants.size());
            exportData.put("filters", new JSONObject()
                .put("event", eventFilter != null ? eventFilter : "all")
                .put("winner_status", winnerFilter != null ? winnerFilter : "all")
            );
            
            JSONArray participantsArray = new JSONArray();
            for (Map<String, Object> participant : participants) {
                participantsArray.put(new JSONObject(participant));
            }
            
            exportData.put("participants", participantsArray);
            
            out.print(exportData.toString(2)); // Pretty print with 2-space indentation
            
            // Log admin action
            Integer adminId = AdminLoginServlet.getAdminId(request);
            if (adminId != null) {
                String filterInfo = buildFilterInfo(eventFilter, winnerFilter);
                adminDAO.logAdminAction(adminId, "Exported JSON data" + filterInfo);
            }
            
        } catch (Exception e) {
            System.err.println("Error exporting JSON: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to export JSON data");
            
            out.print(jsonResponse.toString());
        } finally {
            out.flush();
        }
    }
    
    /**
     * Get filtered participants based on criteria
     */
    private List<Map<String, Object>> getFilteredParticipants(String eventFilter, String winnerFilter) {
        // For now, return all participants. In a more advanced implementation,
        // you would add SQL WHERE clauses based on the filters
        return registrationDAO.getAllRegistrations(0, Integer.MAX_VALUE);
    }
    
    /**
     * Generate HTML report
     */
    private void generateHTMLReport(PrintWriter out, List<Map<String, Object>> participants, 
                                   String eventFilter, String winnerFilter) {
        
        String filterInfo = "";
        if (eventFilter != null && !eventFilter.trim().isEmpty()) {
            filterInfo += " - Event: " + eventFilter;
        }
        if (winnerFilter != null && !winnerFilter.trim().isEmpty()) {
            filterInfo += " - Winner Status: " + winnerFilter;
        }
        
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("<title>TARUNYAM - Tech Event 2025 - Participants Report</title>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; background: #f5f5f5; }");
        out.println(".container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #1e3a8a; text-align: center; margin-bottom: 10px; }");
        out.println(".subtitle { text-align: center; color: #666; margin-bottom: 30px; }");
        out.println("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background: linear-gradient(135deg, #3b82f6, #1e3a8a); color: white; font-weight: bold; }");
        out.println("tr:nth-child(even) { background-color: #f8fafc; }");
        out.println("tr:hover { background-color: #e2e8f0; }");
        out.println(".winner { background-color: #fef3c7 !important; font-weight: bold; }");
        out.println(".stats { display: flex; justify-content: space-around; margin-bottom: 20px; }");
        out.println(".stat-box { background: linear-gradient(135deg, #3b82f6, #8b5cf6); color: white; padding: 20px; border-radius: 10px; text-align: center; min-width: 150px; }");
        out.println(".stat-number { font-size: 2em; font-weight: bold; }");
        out.println(".stat-label { font-size: 0.9em; opacity: 0.9; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"container\">");
        out.println("<h1>TARUNYAM - Tech Event 2025</h1>");
        out.println("<div class=\"subtitle\">Participants Report" + filterInfo + "</div>");
        out.println("<div class=\"subtitle\">Generated on: " + new Date() + "</div>");
        
        // Statistics
        int totalParticipants = participants.size();
        int winnersCount = 0;
        for (Map<String, Object> participant : participants) {
            if ((Boolean) participant.get("winner_status")) {
                winnersCount++;
            }
        }
        
        out.println("<div class=\"stats\">");
        out.println("<div class=\"stat-box\">");
        out.println("<div class=\"stat-number\">" + totalParticipants + "</div>");
        out.println("<div class=\"stat-label\">Total Participants</div>");
        out.println("</div>");
        out.println("<div class=\"stat-box\">");
        out.println("<div class=\"stat-number\">" + winnersCount + "</div>");
        out.println("<div class=\"stat-label\">Winners</div>");
        out.println("</div>");
        out.println("<div class=\"stat-box\">");
        out.println("<div class=\"stat-number\">" + (totalParticipants - winnersCount) + "</div>");
        out.println("<div class=\"stat-label\">Participants</div>");
        out.println("</div>");
        out.println("</div>");
        
        // Table
        out.println("<table>");
        out.println("<thead>");
        out.println("<tr><th>ID</th><th>Name</th><th>Email</th><th>College</th><th>Event</th><th>Registration Date</th><th>Status</th></tr>");
        out.println("</thead>");
        out.println("<tbody>");
        
        for (Map<String, Object> participant : participants) {
            boolean isWinner = (Boolean) participant.get("winner_status");
            String rowClass = isWinner ? " class=\"winner\"" : "";
            
            out.printf("<tr%s><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>%n",
                rowClass,
                participant.get("id"),
                escapeHTML((String) participant.get("name")),
                escapeHTML((String) participant.get("email")),
                escapeHTML((String) participant.get("college")),
                escapeHTML((String) participant.get("event")),
                participant.get("registration_date"),
                isWinner ? "🏆 Winner" : "✅ Participant"
            );
        }
        
        out.println("</tbody>");
        out.println("</table>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    /**
     * Build filter information string for logging
     */
    private String buildFilterInfo(String eventFilter, String winnerFilter) {
        StringBuilder filterInfo = new StringBuilder();
        
        if (eventFilter != null && !eventFilter.trim().isEmpty()) {
            filterInfo.append(" (Event: ").append(eventFilter).append(")");
        }
        
        if (winnerFilter != null && !winnerFilter.trim().isEmpty()) {
            filterInfo.append(" (Winner: ").append(winnerFilter).append(")");
        }
        
        return filterInfo.toString();
    }
    
    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
    
    /**
     * Escape HTML special characters
     */
    private String escapeHTML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
}
