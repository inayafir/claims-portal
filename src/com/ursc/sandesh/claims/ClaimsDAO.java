package com.ursc.sandesh.claims;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for all CHSS_CLAIMS database operations.
 *
 * Table names used:
 *   - CHSS_CLAIMS        (application-owned, created by this app)
 *   - CHSS_CLAIMS_BACKUP (application-owned, temporary backup)
 *   - EMPLOYEE_MASTER    (organisational, read-only, placeholder - IT replaces)
 *
 * All SQL uses PreparedStatement for safety.
 * IT must verify table/column names match their actual schema.
 */
public class ClaimsDAO {

    // =================================================================
    // PLACEHOLDER TABLE/COLUMN NAMES
    // IT (CIG team) MUST replace these with actual names.
    // =================================================================
    private static final String TABLE_CLAIMS        = "CHSS_CLAIMS";
    private static final String TABLE_BACKUP        = "CHSS_CLAIMS_BACKUP";
    private static final String TABLE_EMPLOYEE      = "EMPLOYEE_MASTER";      // placeholder
    private static final String COL_EMP_STAFF       = "STAFF_NUMBER_COL";     // placeholder
    private static final String COL_EMP_FIRST       = "FIRST_NAME_COL";       // placeholder
    private static final String COL_EMP_LAST        = "LAST_NAME_COL";        // placeholder

    private final ConnectionProvider provider;

    public ClaimsDAO(ConnectionProvider provider) {
        this.provider = provider;
    }

    // =================================================================
    // GET CLAIMS (with filters)
    // =================================================================
    public List<Claim> getClaims(String search, String statusFilter,
                                  String employeeFilter, String dateFrom,
                                  String dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CLAIM_ID, SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, ")
           .append("EMPLOYEE_NAME, CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, ")
           .append("APPROVAL_STATUS, PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON, ")
           .append("CREATED_AT, UPDATED_AT FROM ")
           .append(TABLE_CLAIMS).append(" WHERE 1=1");

        List<Object> params = new ArrayList<Object>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND STAFF_NUMBER LIKE ?");
            params.add("%" + search.trim() + "%");
        }

        if (statusFilter != null && !statusFilter.equals("All")) {
            if ("Paid".equals(statusFilter)) {
                sql.append(" AND FINAL_STATUS = ?");
                params.add("Paid");
            } else if ("NIL".equals(statusFilter)) {
                sql.append(" AND FINAL_STATUS = ? AND UNPAID_REASON = ?");
                params.add("Unpaid");
                params.add("NIL");
            } else {
                sql.append(" AND FINAL_STATUS = ? AND UNPAID_REASON = ?");
                params.add("Unpaid");
                params.add(statusFilter);
            }
        }

        if (employeeFilter != null && !employeeFilter.equals("All")) {
            sql.append(" AND EMPLOYEE_STATUS = ?");
            params.add(employeeFilter);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND MEETING_DATE >= ?");
            params.add(dateFrom.trim());
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND MEETING_DATE <= ?");
            params.add(dateTo.trim());
        }

        sql.append(" ORDER BY CLAIM_ID DESC");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            return mapClaims(rs);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // GET SINGLE CLAIM
    // =================================================================
    public Claim getClaimById(int claimId) throws SQLException {
        String sql = "SELECT CLAIM_ID, SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, "
            + "EMPLOYEE_NAME, CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, "
            + "APPROVAL_STATUS, PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON, "
            + "CREATED_AT, UPDATED_AT FROM " + TABLE_CLAIMS + " WHERE CLAIM_ID = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, claimId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapClaim(rs);
            }
            return null;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // INSERT CLAIM
    // =================================================================
    public int insertClaim(Claim c) throws SQLException {
        String sql = "INSERT INTO " + TABLE_CLAIMS
            + " (SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME, "
            + "CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS, "
            + "PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getSerialNumber());
            ps.setString(2, c.getEmployeeStatus());
            ps.setString(3, c.getStaffNumber());
            ps.setString(4, c.getEmployeeName());
            ps.setDouble(5, c.getClaimedAmount());
            ps.setString(6, nvl(c.getMeetingNumber()));
            ps.setString(7, nvl(c.getMeetingDate()));
            ps.setString(8, c.getApprovalStatus());
            if (c.getPassedAmount() != null) {
                ps.setDouble(9, c.getPassedAmount());
            } else {
                ps.setNull(9, java.sql.Types.DOUBLE);
            }
            ps.setString(10, nvl(c.getFinalStatus(), "Unpaid"));
            ps.setString(11, c.getUnpaidReason());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // UPDATE CLAIM
    // =================================================================
    public boolean updateClaim(int claimId, Claim c) throws SQLException {
        String sql = "UPDATE " + TABLE_CLAIMS + " SET "
            + "SERIAL_NUMBER=?, EMPLOYEE_STATUS=?, STAFF_NUMBER=?, EMPLOYEE_NAME=?, "
            + "CLAIMED_AMOUNT=?, MEETING_NUMBER=?, MEETING_DATE=?, "
            + "APPROVAL_STATUS=?, PASSED_AMOUNT=?, FINAL_STATUS=?, "
            + "UNPAID_REASON=?, UPDATED_AT=NOW() WHERE CLAIM_ID=?";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, c.getSerialNumber());
            ps.setString(2, c.getEmployeeStatus());
            ps.setString(3, c.getStaffNumber());
            ps.setString(4, c.getEmployeeName());
            ps.setDouble(5, c.getClaimedAmount());
            ps.setString(6, nvl(c.getMeetingNumber()));
            ps.setString(7, nvl(c.getMeetingDate()));
            ps.setString(8, c.getApprovalStatus());
            if (c.getPassedAmount() != null) {
                ps.setDouble(9, c.getPassedAmount());
            } else {
                ps.setNull(9, java.sql.Types.DOUBLE);
            }
            ps.setString(10, nvl(c.getFinalStatus(), "Unpaid"));
            ps.setString(11, c.getUnpaidReason());
            ps.setInt(12, claimId);
            return ps.executeUpdate() > 0;
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // DELETE CLAIM
    // =================================================================
    public boolean deleteClaim(int claimId) throws SQLException {
        String sql = "DELETE FROM " + TABLE_CLAIMS + " WHERE CLAIM_ID = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, claimId);
            return ps.executeUpdate() > 0;
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // DASHBOARD STATISTICS
    // =================================================================
    public Map<String, Integer> getDashboardStats() throws SQLException {
        String sql = "SELECT "
            + "COUNT(*) AS TOTAL_CLAIMS, "
            + "SUM(CASE WHEN FINAL_STATUS='Paid' THEN 1 ELSE 0 END) AS PAID_COUNT, "
            + "SUM(CASE WHEN FINAL_STATUS='Unpaid' AND UNPAID_REASON='CMO Clarification' THEN 1 ELSE 0 END) AS CMO_COUNT, "
            + "SUM(CASE WHEN FINAL_STATUS='Unpaid' AND UNPAID_REASON='Objection Exists' THEN 1 ELSE 0 END) AS OBJECTION_COUNT, "
            + "SUM(CASE WHEN FINAL_STATUS='Unpaid' AND UNPAID_REASON='High Value' THEN 1 ELSE 0 END) AS HIGH_VALUE_COUNT, "
            + "SUM(CASE WHEN FINAL_STATUS='Unpaid' AND UNPAID_REASON='NIL' THEN 1 ELSE 0 END) AS NIL_COUNT "
            + "FROM " + TABLE_CLAIMS;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            Map<String, Integer> stats = new HashMap<String, Integer>();
            if (rs.next()) {
                stats.put("total", rs.getInt("TOTAL_CLAIMS"));
                stats.put("paid", rs.getInt("PAID_COUNT"));
                stats.put("cmo_clarification", rs.getInt("CMO_COUNT"));
                stats.put("objection_exists", rs.getInt("OBJECTION_COUNT"));
                stats.put("high_value", rs.getInt("HIGH_VALUE_COUNT"));
                stats.put("nil", rs.getInt("NIL_COUNT"));
            }
            return stats;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // GET ALL CLAIMS FOR EXPORT
    // =================================================================
    public List<Claim> getAllClaimsForExport() throws SQLException {
        String sql = "SELECT SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME, "
            + "CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS, "
            + "PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON FROM "
            + TABLE_CLAIMS + " ORDER BY CLAIM_ID ASC";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            return mapClaims(rs);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // BACKUP / RESTORE / IMPORT
    // =================================================================
    public boolean hasBackup() throws SQLException {
        String sql = "SELECT COUNT(*) AS CNT FROM " + TABLE_BACKUP;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("CNT") > 0;
            }
            return false;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public void backupClaims() throws SQLException {
        Connection conn = null;
        PreparedStatement psClear = null;
        PreparedStatement psInsert = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            psClear = conn.prepareStatement("DELETE FROM " + TABLE_BACKUP);
            psClear.executeUpdate();

            String insertSql = "INSERT INTO " + TABLE_BACKUP
                + " (SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME, "
                + "CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS, "
                + "PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON) "
                + "SELECT SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME, "
                + "CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS, "
                + "PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON FROM " + TABLE_CLAIMS;
            psInsert = conn.prepareStatement(insertSql);
            psInsert.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            closeQuietly(psInsert);
            closeQuietly(psClear);
            closeQuietly(conn);
        }
    }

    public void deleteAllClaims() throws SQLException {
        String sql = "DELETE FROM " + TABLE_CLAIMS;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public void bulkInsertClaims(List<Claim> claims) throws SQLException {
        String sql = "INSERT INTO " + TABLE_CLAIMS
            + " (SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME, "
            + "CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS, "
            + "PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);

            for (Claim c : claims) {
                ps.setString(1, c.getSerialNumber());
                ps.setString(2, c.getEmployeeStatus());
                ps.setString(3, c.getStaffNumber());
                ps.setString(4, c.getEmployeeName());
                ps.setDouble(5, c.getClaimedAmount());
                ps.setString(6, nvl(c.getMeetingNumber()));
                ps.setString(7, nvl(c.getMeetingDate()));
                ps.setString(8, c.getApprovalStatus());
                if (c.getPassedAmount() != null) {
                    ps.setDouble(9, c.getPassedAmount());
                } else {
                    ps.setNull(9, java.sql.Types.DOUBLE);
                }
                ps.setString(10, nvl(c.getFinalStatus(), "Unpaid"));
                ps.setString(11, c.getUnpaidReason());
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public void clearBackup() throws SQLException {
        String sql = "DELETE FROM " + TABLE_BACKUP;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public boolean restoreFromBackup() throws SQLException {
        Connection conn = null;
        PreparedStatement psDel = null;
        PreparedStatement psIns = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            psDel = conn.prepareStatement("DELETE FROM " + TABLE_CLAIMS);
            psDel.executeUpdate();

            String insertSql = "INSERT INTO " + TABLE_CLAIMS
                + " (SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME, "
                + "CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS, "
                + "PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON) "
                + "SELECT SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME, "
                + "CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS, "
                + "PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON FROM " + TABLE_BACKUP;
            psIns = conn.prepareStatement(insertSql);
            int rows = psIns.executeUpdate();

            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            closeQuietly(psIns);
            closeQuietly(psDel);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // EMPLOYEE LOOKUP (reads from ORGANISATIONAL table)
    // =================================================================
    public Map<String, String> lookupEmployee(String staffNumber) throws SQLException {
        String sql = "SELECT " + COL_EMP_STAFF + ", " + COL_EMP_FIRST + ", " + COL_EMP_LAST
            + " FROM " + TABLE_EMPLOYEE + " WHERE " + COL_EMP_STAFF + " = ? LIMIT 1";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, staffNumber);
            rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, String> result = new HashMap<String, String>();
                result.put("staffNumber", rs.getString(COL_EMP_STAFF));
                result.put("firstName", rs.getString(COL_EMP_FIRST));
                result.put("lastName", rs.getString(COL_EMP_LAST));
                return result;
            }
            return null;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // =================================================================
    // HELPER: Map a ResultSet row to a Claim object
    // =================================================================
    private Claim mapClaim(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setClaimId(rs.getInt("CLAIM_ID"));
        c.setSerialNumber(rs.getString("SERIAL_NUMBER"));
        c.setEmployeeStatus(rs.getString("EMPLOYEE_STATUS"));
        c.setStaffNumber(rs.getString("STAFF_NUMBER"));
        c.setEmployeeName(rs.getString("EMPLOYEE_NAME"));
        c.setClaimedAmount(rs.getDouble("CLAIMED_AMOUNT"));
        c.setMeetingNumber(rs.getString("MEETING_NUMBER"));
        java.sql.Date md = rs.getDate("MEETING_DATE");
        c.setMeetingDate(md != null ? md.toString() : "");
        c.setApprovalStatus(rs.getString("APPROVAL_STATUS"));
        double pa = rs.getDouble("PASSED_AMOUNT");
        c.setPassedAmount(rs.wasNull() ? null : pa);
        c.setFinalStatus(rs.getString("FINAL_STATUS"));
        c.setUnpaidReason(rs.getString("UNPAID_REASON"));
        java.sql.Timestamp cat = rs.getTimestamp("CREATED_AT");
        c.setCreatedAt(cat != null ? cat.toString() : "");
        java.sql.Timestamp uat = rs.getTimestamp("UPDATED_AT");
        c.setUpdatedAt(uat != null ? uat.toString() : "");
        return c;
    }

    private List<Claim> mapClaims(ResultSet rs) throws SQLException {
        List<Claim> list = new ArrayList<Claim>();
        while (rs.next()) {
            list.add(mapClaim(rs));
        }
        return list;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String nvl(String s, String def) {
        return (s == null || s.isEmpty()) ? def : s;
    }

    private void closeQuietly(AutoCloseable c) {
        if (c != null) {
            try { c.close(); } catch (Exception ignored) {}
        }
    }
}
