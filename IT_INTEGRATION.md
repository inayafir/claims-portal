# IT_INTEGRATION.md - For CIG Team

## Overview

This document describes what the CIG (IT) team must do to integrate the Claims Portal module into the existing Sandesh intranet application.

## Section A: What You Receive

The intern provides a self-contained module with:

| Category | Files |
|---|---|
| Java source | `Claim.java`, `ConnectionProvider.java`, `ClaimsDAO.java`, `ClaimsServlet.java`, `DateUtil.java`, `ExcelUtil.java` |
| Web assets | `index.jsp`, `style.css`, `script.js` |
| Database | `schema.sql` (table definitions + SQL queries) |
| Documentation | `README.md`, `CODE_MAP.md`, `IT_INTEGRATION.md`, `LIBRARY_GUIDE.md`, `OFFICE_HANDOFF.md` |
| Library info | `lib/README.txt` (list of required JARs) |

## Section B: What You Must Provide

1. **Database Connection** - Implement `ConnectionProvider` using existing Sandesh mechanism
2. **Table Creation** - Create `CHSS_CLAIMS` and `CHSS_CLAIMS_BACKUP` tables
3. **Employee Table Names** - Replace placeholder names in `ClaimsDAO.java`
4. **Apache POI JARs** - Add 8 JARs to `WEB-INF/lib/`
5. **URL Mapping** - Ensure `/claims` URL is accessible

## Section C: What You Must Change

### 1. ConnectionProvider.java

**File:** `src/com/ursc/sandesh/claims/ConnectionProvider.java`

This is an interface. Create a concrete class implementing it:

```java
package com.ursc.sandesh.claims;

import java.sql.Connection;
import java.sql.SQLException;

public class SandeshConnectionProvider implements ConnectionProvider {
    @Override
    public Connection getConnection() throws SQLException {
        // Use your existing Sandesh database connection mechanism
        // Example:
        // return DataSourceManager.getConnection();
        // or
        // return (Connection) initialContext.lookup("java:comp/env/jdbc/SandeshDB");
        throw new UnsupportedOperationException("Replace with your connection method");
    }
}
```

### 2. ClaimsServlet.java - getProvider() method

**File:** `src/com/ursc/sandesh/claims/ClaimsServlet.java`

Find the `getProvider()` method (around line 55) and replace the body:

```java
private ConnectionProvider getProvider() {
    // Option 1: Store in ServletContext during app startup
    return (ConnectionProvider) getServletContext().getAttribute("connectionProvider");

    // Option 2: Instantiate directly
    // return new SandeshConnectionProvider();

    // Option 3: Use your existing factory
    // return YourExistingFactory.getConnectionProvider();
}
```

### 3. ClaimsDAO.java - Table/Column Names

**File:** `src/com/ursc/sandesh/claims/ClaimsDAO.java`

Find the placeholder constants (around line 20) and replace:

```java
// BEFORE (placeholders):
private static final String TABLE_EMPLOYEE  = "EMPLOYEE_MASTER";
private static final String COL_EMP_STAFF   = "STAFF_NUMBER_COL";
private static final String COL_EMP_FIRST   = "FIRST_NAME_COL";
private static final String COL_EMP_LAST    = "LAST_NAME_COL";

// AFTER (your actual names):
private static final String TABLE_EMPLOYEE  = "YOUR_ACTUAL_TABLE";
private static final String COL_EMP_STAFF   = "YOUR_STAFF_COLUMN";
private static final String COL_EMP_FIRST   = "YOUR_FIRST_NAME_COLUMN";
private static final String COL_EMP_LAST    = "YOUR_LAST_NAME_COLUMN";
```

Also verify the `CHSS_CLAIMS` and `CHSS_CLAIMS_BACKUP` table names match your schema.

### 4. database/schema.sql - Review and Execute

- Review all column names and types
- Create the `CHSS_CLAIMS` table
- Create the `CHSS_CLAIMS_BACKUP` table
- Verify the employee table query works with your actual table

## Section D: What the Application Expects

### Database Tables

**CHSS_CLAIMS** (application-owned):
```sql
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
    UPDATED_AT            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**CHSS_CLAIMS_BACKUP** (application-owned, same structure as CHSS_CLAIMS).

**EMPLOYEE_MASTER** (organisational, read-only):
- Must have a staff number column
- Must have a first name column
- Must have a last name column
- Application only runs SELECT queries against this table

### ConnectionProvider Contract

```java
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
```

- Must return a valid, open JDBC Connection
- Connection must have permissions for: SELECT, INSERT, UPDATE, DELETE on `CHSS_CLAIMS` and `CHSS_CLAIMS_BACKUP`
- Connection must have SELECT permission on the employee table
- Caller will close the Connection after use

## Section E: Verification Checklist

After integration, verify:

- [ ] `CHSS_CLAIMS` table exists with all columns
- [ ] `CHSS_CLAIMS_BACKUP` table exists with all columns
- [ ] `ConnectionProvider` is implemented and returning valid Connections
- [ ] `ClaimsServlet.getProvider()` retrieves the ConnectionProvider
- [ ] Employee table placeholders are replaced in `ClaimsDAO.java`
- [ ] Apache POI JARs are in `WEB-INF/lib/`
- [ ] `/claims` URL is accessible in browser
- [ ] Main page loads without errors
- [ ] Can add a claim via the form
- [ ] Can edit a claim
- [ ] Can delete a claim
- [ ] Dashboard stats update correctly
- [ ] Export downloads an Excel file
- [ ] Import uploads an Excel file and replaces data
- [ ] Rollback restores previous data
- [ ] Employee lookup returns name when entering staff number

## Section F: Files You Should NOT Modify

| File | Reason |
|---|---|
| `index.jsp` | Frontend, no changes needed |
| `style.css` | Styling, no changes needed |
| `script.js` | Client logic, no changes needed |
| `Claim.java` | Model class, no changes needed |
| `DateUtil.java` | Utility, no changes needed |
| `ExcelUtil.java` | Excel logic, no changes needed |
| `schema.sql` | Reference only, no changes needed |

Only modify:
- `ConnectionProvider.java` (or create implementing class)
- `ClaimsServlet.java` (getProvider method)
- `ClaimsDAO.java` (table/column names)
