package com.ursc.sandesh.claims;

import com.ursc.sandesh.claims.util.ExcelUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * Single servlet handling ALL claims portal actions.
 *
 * URL pattern: /claims
 *
 * Actions (dispatched via "action" request parameter or path info):
 *   (none)       -> forwards to index.jsp (dashboard + claims list)
 *   list         -> returns JSON array of claims
 *   get          -> returns JSON for a single claim
 *   dashboard    -> returns JSON dashboard stats
 *   save         -> creates or updates a claim
 *   delete       -> deletes a claim
 *   export       -> downloads an Excel file
 *   import       -> imports claims from an uploaded Excel file
 *   rollback     -> restores claims from backup
 *   backup-status -> returns JSON indicating if backup exists
 *   lookup-employee -> returns JSON employee name for a staff number
 *
 * IMPORTANT: This servlet needs a ConnectionProvider to access the database.
 * IT (CIG team) must make a ConnectionProvider instance available.
 * See ConnectionProvider.java for integration instructions.
 *
 * For Sandesh integration, IT can:
 *   1. Create a class implementing ConnectionProvider using their existing mechanism
 *   2. Store the instance in a ServletContext attribute
 *   3. Modify the getProvider() method below to retrieve it
 */
@WebServlet("/claims")
public class ClaimsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // =================================================================
    // ConnectionProvider retrieval
    //
    // IT (CIG team): Replace this method with your existing connection
    // mechanism. The simplest approach:
    //
    //   1. Create a class implementing ConnectionProvider
    //   2. Store it in ServletContext during app initialization
    //   3. Retrieve it here via getServletContext().getAttribute(...)
    //
    // Example:
    //   public class SandeshConnectionProvider implements ConnectionProvider {
    //       public Connection getConnection() throws SQLException {
    //           return yourExistingConnectionMethod();
    //       }
    //   }
    //
    //   // In a @WebListener or web.xml:
    //   getServletContext().setAttribute("connectionProvider", new SandeshConnectionProvider());
    //
    //   // Then in this method:
    //   return (ConnectionProvider) getServletContext().getAttribute("connectionProvider");
    // =================================================================
    private ConnectionProvider getProvider() {
        // TODO: IT must replace this with their connection mechanism
        throw new UnsupportedOperationException(
            "IT: Implement getProvider() to return a ConnectionProvider using your existing Sandesh database mechanism.");
    }

    // =================================================================
    // DOGET - handles all read actions
    // =================================================================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = getAction(req);

        try {
            ClaimsDAO dao = new ClaimsDAO(getProvider());

            if ("list".equals(action)) {
                handleList(req, resp, dao);
            } else if ("get".equals(action)) {
                handleGet(req, resp, dao);
            } else if ("dashboard".equals(action)) {
                handleDashboard(req, resp, dao);
            } else if ("export".equals(action)) {
                handleExport(req, resp, dao);
            } else if ("backup-status".equals(action)) {
                handleBackupStatus(req, resp, dao);
            } else if ("lookup-employee".equals(action)) {
                handleLookupEmployee(req, resp, dao);
            } else {
                // No action or unknown action -> show main page
                req.getRequestDispatcher("/index.jsp").forward(req, resp);
            }
        } catch (UnsupportedOperationException e) {
            sendError(resp, 500, e.getMessage());
        } catch (Exception e) {
            sendError(resp, 500, "Server error: " + e.getMessage());
        }
    }

    // =================================================================
    // DOPOST - handles all write actions
    // =================================================================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = getAction(req);

        try {
            ClaimsDAO dao = new ClaimsDAO(getProvider());

            if ("save".equals(action)) {
                handleSave(req, resp, dao);
            } else if ("delete".equals(action)) {
                handleDelete(req, resp, dao);
            } else if ("import".equals(action)) {
                handleImport(req, resp, dao);
            } else if ("rollback".equals(action)) {
                handleRollback(req, resp, dao);
            } else {
                sendError(resp, 400, "Unknown action: " + action);
            }
        } catch (UnsupportedOperationException e) {
            sendError(resp, 500, e.getMessage());
        } catch (Exception e) {
            sendError(resp, 500, "Server error: " + e.getMessage());
        }
    }

    // =================================================================
    // ACTION: LIST CLAIMS (with filters)
    // =================================================================
    private void handleList(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        String search = req.getParameter("search");
        String status = req.getParameter("status");
        String employee = req.getParameter("employee");
        String dateFrom = req.getParameter("date_from");
        String dateTo = req.getParameter("date_to");

        List<Claim> claims = dao.getClaims(
            search, status, employee, dateFrom, dateTo);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(claimsToJson(claims));
    }

    // =================================================================
    // ACTION: GET SINGLE CLAIM
    // =================================================================
    private void handleGet(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        int id = getIntParam(req, "id");
        if (id <= 0) {
            sendError(resp, 400, "Invalid claim ID");
            return;
        }

        Claim claim = dao.getClaimById(id);
        if (claim == null) {
            sendError(resp, 404, "Claim not found");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(claimToJson(claim));
    }

    // =================================================================
    // ACTION: DASHBOARD STATS
    // =================================================================
    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        Map<String, Integer> stats = dao.getDashboardStats();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(mapToJson(stats));
    }

    // =================================================================
    // ACTION: SAVE (create or update)
    // =================================================================
    private void handleSave(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        Claim claim = extractClaim(req);

        // Validate required fields
        if (isEmpty(claim.getSerialNumber())) { sendError(resp, 400, "Serial Number is required"); return; }
        if (isEmpty(claim.getEmployeeStatus())) { sendError(resp, 400, "Employee Status is required"); return; }
        if (isEmpty(claim.getStaffNumber())) { sendError(resp, 400, "Staff Number is required"); return; }
        if (isEmpty(claim.getEmployeeName())) { sendError(resp, 400, "Name is required"); return; }
        if (claim.getClaimedAmount() <= 0) { sendError(resp, 400, "Claimed Amount must be positive"); return; }
        if (isEmpty(claim.getApprovalStatus())) { sendError(resp, 400, "Approval Status is required"); return; }

        int id = getIntParam(req, "id");
        Map<String, Object> result = new HashMap<String, Object>();

        if (id > 0) {
            dao.updateClaim(id, claim);
            result.put("message", "Claim updated successfully");
        } else {
            int newId = dao.insertClaim(claim);
            result.put("message", "Claim created successfully");
            result.put("id", newId);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(mapToJson(result));
    }

    // =================================================================
    // ACTION: DELETE
    // =================================================================
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        int id = getIntParam(req, "id");
        if (id <= 0) {
            sendError(resp, 400, "Invalid claim ID");
            return;
        }

        boolean deleted = dao.deleteClaim(id);
        if (!deleted) {
            sendError(resp, 404, "Claim not found");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"message\":\"Claim deleted successfully\"}");
    }

    // =================================================================
    // ACTION: EXPORT TO EXCEL
    // =================================================================
    private void handleExport(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        List<Claim> claims = dao.getAllClaimsForExport();
        Workbook wb = ExcelUtil.exportToExcel(claims);

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=claims_export.xlsx");
        OutputStream out = resp.getOutputStream();
        wb.write(out);
        wb.close();
        out.flush();
    }

    // =================================================================
    // ACTION: IMPORT FROM EXCEL
    // =================================================================
    private void handleImport(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        if (!req.getContentType().contains("multipart/")) {
            sendError(resp, 400, "Expected multipart form data");
            return;
        }

        javax.servlet.http.Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            sendError(resp, 400, "No file uploaded");
            return;
        }

        String filename = filePart.getSubmittedFileName();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            sendError(resp, 400, "Please upload an .xlsx file");
            return;
        }

        InputStream inputStream = filePart.getInputStream();
        ExcelUtil.ImportResult result = ExcelUtil.importFromExcel(inputStream);

        if (result.hasErrors() && result.getClaims().isEmpty()) {
            Map<String, Object> errResp = new HashMap<String, Object>();
            errResp.put("error", "No valid data rows found in the file");
            errResp.put("skipped", result.getSkippedCount());
            errResp.put("errors", result.getErrors());
            resp.setStatus(400);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(mapToJson(errResp));
            return;
        }

        // Backup before replacing
        dao.backupClaims();
        dao.deleteAllClaims();
        dao.bulkInsertClaims(result.getClaims());

        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("message", "Import successful");
        resMap.put("inserted", result.getClaims().size());
        resMap.put("skipped", result.getSkippedCount());
        resMap.put("errors", result.getErrors());

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(mapToJson(resMap));
    }

    // =================================================================
    // ACTION: ROLLBACK FROM BACKUP
    // =================================================================
    private void handleRollback(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        if (!dao.hasBackup()) {
            sendError(resp, 400, "No backup available to restore");
            return;
        }

        dao.restoreFromBackup();
        dao.clearBackup();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"message\":\"Database restored from backup\"}");
    }

    // =================================================================
    // ACTION: BACKUP STATUS
    // =================================================================
    private void handleBackupStatus(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        boolean hasBackup = dao.hasBackup();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"has_backup\":" + hasBackup + "}");
    }

    // =================================================================
    // ACTION: EMPLOYEE LOOKUP
    // =================================================================
    private void handleLookupEmployee(HttpServletRequest req, HttpServletResponse resp, ClaimsDAO dao)
            throws Exception {
        String staffNumber = req.getParameter("staff_number");
        if (staffNumber == null || staffNumber.trim().isEmpty()) {
            sendError(resp, 400, "staff_number parameter is required");
            return;
        }

        Map<String, String> emp = dao.lookupEmployee(staffNumber.trim());
        if (emp == null) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"found\":false}");
            return;
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("found", true);
        result.put("staffNumber", emp.get("staffNumber"));
        result.put("firstName", emp.get("firstName"));
        result.put("lastName", emp.get("lastName"));
        String fullName = ((emp.get("firstName") != null ? emp.get("firstName") : "") + " "
            + (emp.get("lastName") != null ? emp.get("lastName") : "")).trim();
        result.put("name", fullName);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(mapToJson(result));
    }

    // =================================================================
    // HELPER: Extract claim data from request parameters
    // =================================================================
    private Claim extractClaim(HttpServletRequest req) {
        Claim c = new Claim();
        c.setSerialNumber(req.getParameter("serial_number"));
        c.setEmployeeStatus(req.getParameter("serving_retired"));
        c.setStaffNumber(req.getParameter("staff_number"));
        c.setEmployeeName(req.getParameter("name"));
        c.setMeetingNumber(req.getParameter("meeting_number"));
        c.setMeetingDate(req.getParameter("meeting_date"));
        c.setApprovalStatus(req.getParameter("approval_status"));
        c.setFinalStatus(req.getParameter("final_status"));
        c.setUnpaidReason(req.getParameter("unpaid_reason"));

        String claimed = req.getParameter("claimed_amount");
        if (claimed != null && !claimed.trim().isEmpty()) {
            try { c.setClaimedAmount(Double.parseDouble(claimed.trim())); }
            catch (NumberFormatException e) { c.setClaimedAmount(0); }
        }

        String passed = req.getParameter("passed_amount");
        if (passed != null && !passed.trim().isEmpty()) {
            try { c.setPassedAmount(Double.parseDouble(passed.trim())); }
            catch (NumberFormatException e) { c.setPassedAmount(null); }
        }

        return c;
    }

    // =================================================================
    // HELPER: Determine action from request
    // =================================================================
    private String getAction(HttpServletRequest req) {
        String action = req.getParameter("action");
        if (action != null && !action.isEmpty()) {
            return action;
        }
        // Try path info (e.g. /claims/list)
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            return pathInfo.substring(1); // remove leading /
        }
        return "";
    }

    private int getIntParam(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        if (val == null || val.trim().isEmpty()) return -1;
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void sendError(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"error\":\"" + escapeJson(message) + "\"}");
    }

    // =================================================================
    // JSON HELPERS (hand-built, no external library needed)
    // =================================================================
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String claimToJson(Claim c) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(c.getClaimId());
        sb.append(",\"serial_number\":\"").append(escapeJson(c.getSerialNumber())).append("\"");
        sb.append(",\"serving_retired\":\"").append(escapeJson(c.getEmployeeStatus())).append("\"");
        sb.append(",\"staff_number\":\"").append(escapeJson(c.getStaffNumber())).append("\"");
        sb.append(",\"name\":\"").append(escapeJson(c.getEmployeeName())).append("\"");
        sb.append(",\"claimed_amount\":").append(c.getClaimedAmount());
        sb.append(",\"meeting_number\":\"").append(escapeJson(c.getMeetingNumber())).append("\"");
        sb.append(",\"meeting_date\":\"").append(escapeJson(c.getMeetingDate())).append("\"");
        sb.append(",\"approval_status\":\"").append(escapeJson(c.getApprovalStatus())).append("\"");
        if (c.getPassedAmount() != null) {
            sb.append(",\"passed_amount\":").append(c.getPassedAmount());
        } else {
            sb.append(",\"passed_amount\":null");
        }
        sb.append(",\"final_status\":\"").append(escapeJson(c.getFinalStatus())).append("\"");
        sb.append(",\"unpaid_reason\":\"").append(escapeJson(c.getUnpaidReason())).append("\"");
        sb.append(",\"created_at\":\"").append(escapeJson(c.getCreatedAt())).append("\"");
        sb.append(",\"updated_at\":\"").append(escapeJson(c.getUpdatedAt())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String claimsToJson(List<Claim> claims) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < claims.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(claimToJson(claims.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object val = entry.getValue();
            if (val == null) {
                sb.append("null");
            } else if (val instanceof Number) {
                sb.append(val);
            } else if (val instanceof Boolean) {
                sb.append(val);
            } else if (val instanceof List) {
                sb.append(listToJson((List<?>) val));
            } else {
                sb.append("\"").append(escapeJson(val.toString())).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            Object item = list.get(i);
            if (item instanceof String) {
                sb.append("\"").append(escapeJson((String) item)).append("\"");
            } else {
                sb.append("\"").append(escapeJson(String.valueOf(item))).append("\"");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
