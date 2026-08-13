# Claims Portal - Sandesh Integration Module

## What This Application Does

A web-based claims management portal for managing medical/reimbursement claims. Features:

- Dashboard with summary statistics (Total, Paid, CMO Clarification, Objection, High Value)
- Claims table with search, status filter, employee status filter, date range filter
- Add/Edit/Delete claims via modal form
- Employee name auto-fill by staff number lookup
- Excel import (flexible header mapping, 60+ column aliases)
- Excel export (styled with colour-coded rows)
- Backup before import, one-click rollback

## Files You Need to Copy

```
sandesh-integration/
├── src/com/ursc/sandesh/claims/
│   ├── Claim.java                  → Model class
│   ├── ConnectionProvider.java     → Interface for IT to implement
│   ├── ClaimsDAO.java              → All database queries
│   ├── ClaimsServlet.java          → Single servlet (all actions)
│   └── util/
│       ├── DateUtil.java           → Date parsing utility
│       └── ExcelUtil.java          → Excel import/export (needs Apache POI)
├── web/
│   ├── index.jsp                   → Main page
│   ├── css/style.css               → Styling
│   └── js/script.js                → Client-side JavaScript
├── database/
│   └── schema.sql                  → Table definitions (IT reviews/runs)
├── lib/
│   └── README.txt                  → List of required JARs
├── README.md                       → This file
├── CODE_MAP.md                     → Wiring diagram
├── IT_INTEGRATION.md               → For CIG team
├── LIBRARY_GUIDE.md                → Detailed JAR documentation
└── OFFICE_HANDOFF.md               → Handoff checklist
```

## Where Each File Goes in Eclipse

| Source File | Destination in Eclipse Project |
|---|---|
| `src/com/ursc/sandesh/claims/*.java` | `src/com/ursc/sandesh/claims/` (create package) |
| `web/index.jsp` | `WebContent/index.jsp` |
| `web/css/style.css` | `WebContent/css/style.css` |
| `web/js/script.js` | `WebContent/js/script.js` |
| `lib/*.jar` | `WebContent/WEB-INF/lib/` |

## Files IT Must Edit

1. **`ConnectionProvider.java`** - Implement `getConnection()` using existing Sandesh mechanism
2. **`ClaimsServlet.java`** - Update `getProvider()` to retrieve the ConnectionProvider
3. **`ClaimsDAO.java`** - Replace placeholder table/column names (`EMPLOYEE_MASTER`, `STAFF_NUMBER_COL`, etc.)
4. **`schema.sql`** - Review, create tables, replace employee table placeholders

## Database Tables Required

### Application-Owned (IT creates these)

| Table | Purpose |
|---|---|
| `CHSS_CLAIMS` | All claims data |
| `CHSS_CLAIMS_BACKUP` | Temporary backup before import |

### Existing Organisational (DO NOT CREATE)

| Table | Purpose |
|---|---|
| `EMPLOYEE_MASTER` (placeholder) | Employee name lookup by staff number |

## Required Columns

See `database/schema.sql` for complete column definitions with SQL types.

## Required JARs

| JAR | Required By |
|---|---|
| poi-ooxml-4.1.2.jar | ExcelUtil.java |
| poi-4.1.2.jar | ExcelUtil.java |
| poi-ooxml-lite-4.1.2.jar | poi-ooxml dependency |
| commons-codec-1.13.jar | poi dependency |
| commons-collections4-4.4.jar | poi dependency |
| commons-math3-3.6.1.jar | poi dependency |
| xmlbeans-3.1.0.jar | poi-ooxml dependency |
| curvesapi-1.06.jar | poi-ooxml dependency |

**Do NOT copy:** servlet-api.jar, jsp-api.jar, jstl.jar (Tomcat provides these).

## Servlet Actions

| Action | HTTP Method | What It Does |
|---|---|---|
| (none) | GET | Shows main page (index.jsp) |
| `list` | GET | Returns JSON array of claims |
| `get` | GET | Returns JSON for single claim |
| `dashboard` | GET | Returns JSON dashboard stats |
| `save` | POST | Creates or updates a claim |
| `delete` | POST | Deletes a claim |
| `export` | GET | Downloads Excel file |
| `import` | POST | Imports claims from Excel |
| `rollback` | POST | Restores from backup |
| `backup-status` | GET | Checks if backup exists |
| `lookup-employee` | GET | Returns employee name for staff number |

## How to Test

1. Copy all files into the Sandesh Eclipse project
2. IT implements ConnectionProvider
3. IT creates the CHSS_CLAIMS and CHSS_CLAIMS_BACKUP tables
4. IT replaces employee table placeholders in ClaimsDAO
5. Deploy to Tomcat
6. Navigate to `/claims` (or configured URL)
7. Test: Add claim, Edit, Delete, Export, Import, Rollback

## Common Integration Problems

| Problem | Likely Cause | Fix |
|---|---|---|
| Blank page | ConnectionProvider not implemented | IT must implement getProvider() |
| 404 on /claims | Servlet not registered | Check @WebServlet annotation, Tomcat deployment |
| Employee lookup fails | Placeholder names not replaced | IT must update ClaimsDAO table/column names |
| Import fails | Apache POI JARs missing | Copy all JARs to WEB-INF/lib |
| Excel export fails | poi-ooxml not found | Ensure poi-ooxml and dependencies are in lib |
