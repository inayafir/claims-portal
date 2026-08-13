package com.ursc.sandesh.claims.util;

import com.ursc.sandesh.claims.Claim;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utility class for Excel import and export.
 * Requires Apache POI library (poi-ooxml).
 *
 * JARs required: poi-ooxml, poi, poi-ooxml-lite, commons-codec,
 *                commons-collections4, commons-math3, xmlbeans, curvesapi
 *
 * See lib/README.txt for the complete list of required JARs.
 */
public final class ExcelUtil {

    // Header display names for export
    private static final String[] HEADERS = {
        "Serial Number", "Serving/Retired", "Staff Number", "Name",
        "Claimed Amount", "Meeting Number", "Meeting Date",
        "Approval Status", "Passed Amount", "Final Status", "Unpaid Reason"
    };

    // Field aliases for flexible import header mapping
    private static final Map<String, String[]> FIELD_ALIASES;
    static {
        FIELD_ALIASES = new HashMap<String, String[]>();
        FIELD_ALIASES.put("serial_number", new String[]{
            "serial", "serialnumber", "serial no", "serialno", "sno", "sl no", "slno"});
        FIELD_ALIASES.put("serving_retired", new String[]{
            "servingretired", "serving/retired", "emp status", "employeestatus",
            "employee status", "employeecategory", "category", "emp category"});
        FIELD_ALIASES.put("staff_number", new String[]{
            "staffnumber", "staff no", "staffno", "staffid", "employeeid",
            "employeeno", "employee no"});
        FIELD_ALIASES.put("name", new String[]{
            "name", "employeename", "employee name", "claimantname", "claimant"});
        FIELD_ALIASES.put("claimed_amount", new String[]{
            "claimedamount", "amountclaimed", "claimed amt", "claimamount",
            "claim amt", "claimamt", "amount"});
        FIELD_ALIASES.put("meeting_number", new String[]{
            "meetingnumber", "meeting no", "meetingno", "meeting"});
        FIELD_ALIASES.put("meeting_date", new String[]{
            "meetingdate", "date of meeting", "dateofmeeting", "meeting date",
            "mtg date", "mtgdate", "meeting dt", "date"});
        FIELD_ALIASES.put("approval_status", new String[]{
            "approvalstatus", "approval status", "approval", "approvaldecision"});
        FIELD_ALIASES.put("passed_amount", new String[]{
            "passedamount", "passed amt", "amountpassed", "sanctionedamount"});
        FIELD_ALIASES.put("final_status", new String[]{
            "finalstatus", "final status", "paidstatus", "paymentstatus",
            "paid/unpaid", "final", "paid"});
        FIELD_ALIASES.put("unpaid_reason", new String[]{
            "unpaidreason", "unpaid reason", "reason", "reasonforunpaid",
            "nonpaymentreason", "reason for non payment"});
    }

    private ExcelUtil() {}

    // =================================================================
    // EXPORT: Claims list -> XLSX Workbook
    // =================================================================
    public static Workbook exportToExcel(List<Claim> claims) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Claims");

        // Header style
        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        setThinBorder(headerStyle);

        // Status fill colors
        Map<String, CellStyle> colorMap = new HashMap<String, CellStyle>();
        colorMap.put("Paid", createFillStyle(wb, IndexedColors.GREEN));
        colorMap.put("Objection Exists", createFillStyle(wb, IndexedColors.LAVENDER));
        colorMap.put("CMO Clarification", createFillStyle(wb, IndexedColors.LIGHT_BLUE));
        colorMap.put("High Value", createFillStyle(wb, IndexedColors.YELLOW));
        colorMap.put("NIL", createFillStyle(wb, IndexedColors.ROSE));

        // Header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        for (int r = 0; r < claims.size(); r++) {
            Claim c = claims.get(r);
            Row row = sheet.createRow(r + 1);

            String displayStatus = c.getDisplayStatus();
            CellStyle fillStyle = colorMap.get(displayStatus);

            String[] values = {
                c.getSerialNumber(),
                c.getEmployeeStatus(),
                c.getStaffNumber(),
                c.getEmployeeName(),
                String.valueOf(c.getClaimedAmount()),
                c.getMeetingNumber() != null ? c.getMeetingNumber() : "",
                c.getMeetingDate() != null ? c.getMeetingDate() : "",
                c.getApprovalStatus(),
                c.getPassedAmount() != null ? String.valueOf(c.getPassedAmount()) : "",
                c.getFinalStatus(),
                c.getUnpaidReason() != null ? c.getUnpaidReason() : ""
            };

            for (int col = 0; col < values.length; col++) {
                Cell cell = row.createCell(col);
                cell.setCellValue(values[col]);
                setThinBorder(cell.getCellStyle());
                if (fillStyle != null) {
                    cell.setCellStyle(fillStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.setColumnWidth(i, 18 * 256);
        }

        return wb;
    }

    // =================================================================
    // IMPORT: XLSX InputStream -> list of Claim objects
    // =================================================================
    public static ImportResult importFromExcel(InputStream inputStream) throws IOException {
        ImportResult result = new ImportResult();
        List<Claim> claims = new ArrayList<Claim>();

        Workbook wb = new XSSFWorkbook(inputStream);
        Sheet sheet = wb.getSheetAt(0);

        // Find header row (first row with at least one non-empty cell)
        int dataStart = 0;
        Row headerRow = null;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && hasNonEmptyCell(row)) {
                headerRow = row;
                dataStart = i + 1;
                break;
            }
        }

        if (headerRow == null) {
            wb.close();
            result.addError("The file appears to be empty");
            return result;
        }

        // Build field-to-column mapping using aliases
        Map<String, Integer> mapping = buildMapping(headerRow);

        // Check required fields
        String[] required = {"staff_number", "name", "claimed_amount"};
        List<String> missing = new ArrayList<String>();
        for (String field : required) {
            if (!mapping.containsKey(field)) {
                missing.add(field);
            }
        }
        if (!missing.isEmpty()) {
            wb.close();
            result.addError("Missing required column(s): " + joinStrings(missing));
            return result;
        }

        // Parse data rows
        for (int i = dataStart; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || !hasNonEmptyCell(row)) {
                continue;
            }

            Map<String, String> values = new HashMap<String, String>();
            for (Map.Entry<String, Integer> entry : mapping.entrySet()) {
                Integer colIdx = entry.getValue();
                if (colIdx < row.getLastCellNum()) {
                    Cell cell = row.getCell(colIdx);
                    values.put(entry.getKey(), getCellValue(cell));
                }
            }

            String staffNumber = cleanText(values.get("staff_number"));
            String name = cleanText(values.get("name"));
            Double claimedAmount = DateUtil.parseAmount(values.get("claimed_amount"));

            if (staffNumber == null || staffNumber.isEmpty() ||
                name == null || name.isEmpty()) {
                result.addError("Row " + (i + 1) + ": missing staff number or name");
                result.incrementSkipped();
                continue;
            }
            if (claimedAmount == null) {
                result.addError("Row " + (i + 1) + ": invalid claimed amount");
                result.incrementSkipped();
                continue;
            }

            Claim c = new Claim();
            c.setSerialNumber(cleanText(values.get("serial_number")));
            if (c.getSerialNumber() == null || c.getSerialNumber().isEmpty()) {
                c.setSerialNumber("IMP-" + (i + 1));
            }
            c.setEmployeeStatus(parseEmployeeStatus(values.get("serving_retired")));
            c.setStaffNumber(staffNumber);
            c.setEmployeeName(name);
            c.setClaimedAmount(claimedAmount);
            c.setMeetingNumber(cleanText(values.get("meeting_number")));
            c.setMeetingDate(DateUtil.parse(values.get("meeting_date")));
            c.setApprovalStatus(parseApprovalStatus(values.get("approval_status")));
            c.setPassedAmount(DateUtil.parseAmount(values.get("passed_amount")));

            String finalStatus = parseFinalStatus(values.get("final_status"));
            c.setFinalStatus(finalStatus);

            if (!"Paid".equals(finalStatus)) {
                c.setUnpaidReason(parseUnpaidReason(values.get("unpaid_reason")));
            }

            claims.add(c);
        }

        wb.close();
        result.setClaims(claims);
        return result;
    }

    // =================================================================
    // HELPER: Build field-to-column-index mapping from header row
    // =================================================================
    private static Map<String, Integer> buildMapping(Row headerRow) {
        Map<String, Integer> normalisedHeaders = new HashMap<String, Integer>();
        for (int i = 0; i <= headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String norm = normalise(cell.getStringCellValue());
                if (!norm.isEmpty() && !normalisedHeaders.containsKey(norm)) {
                    normalisedHeaders.put(norm, i);
                }
            }
        }

        Map<String, Integer> mapping = new HashMap<String, Integer>();
        for (Map.Entry<String, String[]> entry : FIELD_ALIASES.entrySet()) {
            String field = entry.getKey();
            for (String alias : entry.getValue()) {
                String normAlias = normalise(alias);
                if (normalisedHeaders.containsKey(normAlias)) {
                    mapping.put(field, normalisedHeaders.get(normAlias));
                    break;
                }
            }
        }
        return mapping;
    }

    private static String normalise(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static String cleanText(String s) {
        return s != null ? s.trim() : "";
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default:      return "";
        }
    }

    private static boolean hasNonEmptyCell(Row row) {
        for (int i = 0; i <= row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && getCellValue(cell).length() > 0) {
                return true;
            }
        }
        return false;
    }

    // =================================================================
    // FIELD PARSING (matches original Python logic)
    // =================================================================
    private static String parseEmployeeStatus(String value) {
        String s = cleanText(value).toLowerCase();
        if (s.contains("cisf")) return "CISF";
        if (s.contains("retir")) return "Retired";
        return "Serving";
    }

    private static String parseApprovalStatus(String value) {
        String s = cleanText(value).toLowerCase();
        if (s.contains("not")) return "Not Approved";
        return "Approved";
    }

    private static String parseFinalStatus(String value) {
        String s = cleanText(value).toLowerCase();
        if (s.contains("unpaid")) return "Unpaid";
        if (s.contains("paid")) return "Paid";
        return "Unpaid";
    }

    private static String parseUnpaidReason(String value) {
        String s = cleanText(value);
        if (s.isEmpty()) return null;
        if (s.equals("CMO Clarification") || s.equals("Objection Exists") ||
            s.equals("High Value") || s.equals("NIL")) {
            return s;
        }
        String sl = s.toLowerCase();
        if (sl.contains("cmo") || sl.contains("clarif")) return "CMO Clarification";
        if (sl.contains("object")) return "Objection Exists";
        if (sl.contains("high")) return "High Value";
        if (sl.equals("nil")) return "NIL";
        return null;
    }

    // =================================================================
    // CELL STYLE HELPERS
    // =================================================================
    private static void setThinBorder(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private static CellStyle createFillStyle(Workbook wb, IndexedColors color) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setThinBorder(style);
        return style;
    }

    private static String joinStrings(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    // =================================================================
    // ImportResult: holds parsed claims and any errors
    // =================================================================
    public static class ImportResult {
        private List<Claim> claims = new ArrayList<Claim>();
        private List<String> errors = new ArrayList<String>();
        private int skippedCount = 0;

        public List<Claim> getClaims() { return claims; }
        public void setClaims(List<Claim> claims) { this.claims = claims; }
        public List<String> getErrors() { return errors; }
        public void addError(String err) { errors.add(err); }
        public boolean hasErrors() { return !errors.isEmpty(); }
        public int getSkippedCount() { return skippedCount; }
        public void incrementSkipped() { skippedCount++; }
    }
}
