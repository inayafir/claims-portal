-- =================================================================
-- CLAIMS PORTAL - DATABASE SCHEMA
-- For integration into Sandesh intranet portal
-- =================================================================
-- This file is documentation/instructions for IT (CIG team).
-- Do NOT auto-execute. IT must review and run these statements.
-- =================================================================


-- =================================================================
-- SECTION 1: APPLICATION-OWNED TABLES
-- These tables are created and managed by this application.
-- =================================================================

-- -----------------------------------------------------------------
-- CHSS_CLAIMS
-- Stores all medical/reimbursement claims submitted by employees.
-- -----------------------------------------------------------------
CREATE TABLE CHSS_CLAIMS (
    CLAIM_ID              INT PRIMARY KEY AUTO_INCREMENT,
    SERIAL_NUMBER         VARCHAR(50)   NOT NULL,
    EMPLOYEE_STATUS       VARCHAR(20)   NOT NULL,
    STAFF_NUMBER          VARCHAR(50)   NOT NULL,
    EMPLOYEE_NAME         VARCHAR(200)  NOT NULL,
    CLAIMED_AMOUNT        DECIMAL(12,2) NOT NULL,
    MEETING_NUMBER        VARCHAR(50)   DEFAULT '',
    MEETING_DATE          DATE          DEFAULT NULL,
    APPROVAL_STATUS       VARCHAR(20)   NOT NULL,
    PASSED_AMOUNT         DECIMAL(12,2) DEFAULT NULL,
    FINAL_STATUS          VARCHAR(20)   NOT NULL DEFAULT 'Unpaid',
    UNPAID_REASON         VARCHAR(50)   DEFAULT NULL,
    CREATED_AT            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Column descriptions:
    -- CLAIM_ID              : Unique auto-increment primary key
    -- SERIAL_NUMBER         : Serial/bill number assigned by the office (e.g., "CHSS/2025/001")
    -- EMPLOYEE_STATUS       : "Serving", "Retired", or "CISF"
    -- STAFF_NUMBER          : Employee's staff number (e.g., "IS012345")
    -- EMPLOYEE_NAME         : Full name of the employee
    -- CLAIMED_AMOUNT        : Amount claimed by the employee
    -- MEETING_NUMBER        : Committee meeting reference number (optional)
    -- MEETING_DATE          : Date of the committee meeting
    -- APPROVAL_STATUS       : "Approved" or "Not Approved"
    -- PASSED_AMOUNT         : Amount passed/sanctioned by the committee (nullable)
    -- FINAL_STATUS          : "Paid" or "Unpaid"
    -- UNPAID_REASON         : If Unpaid: "CMO Clarification", "Objection Exists",
    --                         "High Value", or "NIL" (nullable)
    -- CREATED_AT            : Record creation timestamp
    -- UPDATED_AT            : Record last update timestamp

    INDEX IDX_CLAIMS_STAFF (STAFF_NUMBER),
    INDEX IDX_CLAIMS_FINAL_STATUS (FINAL_STATUS),
    INDEX IDX_CLAIMS_EMP_STATUS (EMPLOYEE_STATUS),
    INDEX IDX_CLAIMS_MEETING_DATE (MEETING_DATE)
);


-- -----------------------------------------------------------------
-- CHSS_CLAIMS_BACKUP
-- Temporary backup of claims data before Excel import.
-- The application creates this before import and can restore from it.
-- -----------------------------------------------------------------
CREATE TABLE CHSS_CLAIMS_BACKUP (
    CLAIM_ID              INT PRIMARY KEY AUTO_INCREMENT,
    SERIAL_NUMBER         VARCHAR(50)   NOT NULL,
    EMPLOYEE_STATUS       VARCHAR(20)   NOT NULL,
    STAFF_NUMBER          VARCHAR(50)   NOT NULL,
    EMPLOYEE_NAME         VARCHAR(200)  NOT NULL,
    CLAIMED_AMOUNT        DECIMAL(12,2) NOT NULL,
    MEETING_NUMBER        VARCHAR(50)   DEFAULT '',
    MEETING_DATE          DATE          DEFAULT NULL,
    APPROVAL_STATUS       VARCHAR(20)   NOT NULL,
    PASSED_AMOUNT         DECIMAL(12,2) DEFAULT NULL,
    FINAL_STATUS          VARCHAR(20)   NOT NULL DEFAULT 'Unpaid',
    UNPAID_REASON         VARCHAR(50)   DEFAULT NULL,
    CREATED_AT            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- =================================================================
-- SECTION 2: EXISTING ORGANISATIONAL TABLE — DO NOT CREATE
-- =================================================================
-- This table ALREADY EXISTS in the organisation's database.
-- The application reads from it for employee name lookup.
-- Do NOT create this table. Do NOT modify it.
-- IT (CIG team) must replace the placeholder names below
-- with the actual table and column names.
-- =================================================================

/*
-- Placeholder table: EMPLOYEE_MASTER
-- Placeholder columns: STAFF_NUMBER_COL, FIRST_NAME_COL, LAST_NAME_COL
-- The actual names will be provided by IT/CIG team.
--
-- Example query used by this application:
--
--   SELECT STAFF_NUMBER_COL, FIRST_NAME_COL, LAST_NAME_COL
--   FROM EMPLOYEE_MASTER
--   WHERE STAFF_NUMBER_COL = ?
--
-- IT must replace:
--   EMPLOYEE_MASTER    --> actual table name
--   STAFF_NUMBER_COL   --> actual staff number column name
--   FIRST_NAME_COL     --> actual first name column name
--   LAST_NAME_COL      --> actual last name column name
*/


-- =================================================================
-- SECTION 3: SQL QUERIES USED BY THIS APPLICATION
-- These are the queries the Java code executes.
-- IT must verify these work against the actual database schema.
-- =================================================================

-- Query 1: List claims with filters (ClaimsDAO.getClaims)
-- Used by: GET /claims?action=list
/*
SELECT CLAIM_ID, SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER,
       EMPLOYEE_NAME, CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE,
       APPROVAL_STATUS, PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON,
       CREATED_AT, UPDATED_AT
FROM CHSS_CLAIMS
WHERE 1=1
  AND STAFF_NUMBER LIKE ?
  AND FINAL_STATUS = ?
  AND UNPAID_REASON = ?
  AND EMPLOYEE_STATUS = ?
  AND MEETING_DATE >= ?
  AND MEETING_DATE <= ?
ORDER BY CLAIM_ID DESC
*/

-- Query 2: Get single claim (ClaimsDAO.getClaimById)
-- Used by: GET /claims?action=get&id=N
/*
SELECT CLAIM_ID, SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER,
       EMPLOYEE_NAME, CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE,
       APPROVAL_STATUS, PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON,
       CREATED_AT, UPDATED_AT
FROM CHSS_CLAIMS
WHERE CLAIM_ID = ?
*/

-- Query 3: Insert claim (ClaimsDAO.insertClaim)
-- Used by: POST /claims?action=save
/*
INSERT INTO CHSS_CLAIMS
    (SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME,
     CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS,
     PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
*/

-- Query 4: Update claim (ClaimsDAO.updateClaim)
-- Used by: POST /claims?action=save&id=N
/*
UPDATE CHSS_CLAIMS SET
    SERIAL_NUMBER=?, EMPLOYEE_STATUS=?, STAFF_NUMBER=?, EMPLOYEE_NAME=?,
    CLAIMED_AMOUNT=?, MEETING_NUMBER=?, MEETING_DATE=?,
    APPROVAL_STATUS=?, PASSED_AMOUNT=?, FINAL_STATUS=?,
    UNPAID_REASON=?, UPDATED_AT=NOW()
WHERE CLAIM_ID=?
*/

-- Query 5: Delete claim (ClaimsDAO.deleteClaim)
-- Used by: POST /claims?action=delete&id=N
/*
DELETE FROM CHSS_CLAIMS WHERE CLAIM_ID=?
*/

-- Query 6: Dashboard statistics (ClaimsDAO.getDashboardStats)
-- Used by: GET /claims?action=dashboard
/*
SELECT
    COUNT(*) AS TOTAL_CLAIMS,
    SUM(CASE WHEN FINAL_STATUS='Paid' THEN 1 ELSE 0 END) AS PAID_COUNT,
    SUM(CASE WHEN FINAL_STATUS='Unpaid' AND UNPAID_REASON='CMO Clarification' THEN 1 ELSE 0 END) AS CMO_COUNT,
    SUM(CASE WHEN FINAL_STATUS='Unpaid' AND UNPAID_REASON='Objection Exists' THEN 1 ELSE 0 END) AS OBJECTION_COUNT,
    SUM(CASE WHEN FINAL_STATUS='Unpaid' AND UNPAID_REASON='High Value' THEN 1 ELSE 0 END) AS HIGH_VALUE_COUNT,
    SUM(CASE WHEN FINAL_STATUS='Unpaid' AND UNPAID_REASON='NIL' THEN 1 ELSE 0 END) AS NIL_COUNT
FROM CHSS_CLAIMS
*/

-- Query 7: Get all claims for export (ClaimsDAO.getAllClaimsForExport)
-- Used by: GET /claims?action=export
/*
SELECT SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME,
       CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS,
       PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON
FROM CHSS_CLAIMS
ORDER BY CLAIM_ID ASC
*/

-- Query 8: Delete all claims before import (ClaimsDAO.deleteAllClaims)
-- Used by: POST /claims?action=import (before inserting new data)
/*
DELETE FROM CHSS_CLAIMS
*/

-- Query 9: Bulk insert claims (ClaimsDAO.bulkInsertClaims)
-- Used by: POST /claims?action=import
/*
INSERT INTO CHSS_CLAIMS
    (SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME,
     CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS,
     PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
*/

-- Query 10: Check if backup has data (ClaimsDAO.hasBackup)
-- Used by: GET /claims?action=backup-status
/*
SELECT COUNT(*) AS CNT FROM CHSS_CLAIMS_BACKUP
*/

-- Query 11: Clear backup (ClaimsDAO.clearBackup)
-- Used by: POST /claims?action=rollback (after restore)
/*
DELETE FROM CHSS_CLAIMS_BACKUP
*/

-- Query 12: Backup current claims (ClaimsDAO.backupClaims)
-- Used by: POST /claims?action=import (before replacing data)
/*
INSERT INTO CHSS_CLAIMS_BACKUP
    (SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME,
     CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS,
     PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON)
SELECT SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME,
       CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS,
       PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON
FROM CHSS_CLAIMS
*/

-- Query 13: Restore from backup (ClaimsDAO.restoreFromBackup)
-- Used by: POST /claims?action=rollback
/*
DELETE FROM CHSS_CLAIMS;
INSERT INTO CHSS_CLAIMS
    (SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME,
     CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS,
     PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON)
SELECT SERIAL_NUMBER, EMPLOYEE_STATUS, STAFF_NUMBER, EMPLOYEE_NAME,
       CLAIMED_AMOUNT, MEETING_NUMBER, MEETING_DATE, APPROVAL_STATUS,
       PASSED_AMOUNT, FINAL_STATUS, UNPAID_REASON
FROM CHSS_CLAIMS_BACKUP
*/

-- Query 14: Employee name lookup (ClaimsDAO.lookupEmployee)
-- Used by: GET /claims?action=lookup-employee&staff_number=N
-- NOTE: This queries the ORGANISATIONAL employee table (NOT owned by this app).
-- IT must replace EMPLOYEE_MASTER and column names with actual names.
/*
SELECT STAFF_NUMBER_COL, FIRST_NAME_COL, LAST_NAME_COL
FROM EMPLOYEE_MASTER
WHERE STAFF_NUMBER_COL = ?
LIMIT 1
*/
