# CODE_MAP.md - Complete Wiring Diagram

## User Flow

```
Browser (http://server/claims)
    ↓ GET /claims (no action)
ClaimsServlet.doGet()
    ↓ forwards to
index.jsp (main page loads)
    ↓ on page load, JavaScript calls:
    ↓ GET /claims?action=dashboard
    ↓ GET /claims?action=list
ClaimsServlet.doGet()
    ↓ queries database via ClaimsDAO
    ↓ returns JSON
ClaimsServlet → JSON response → JavaScript renders table + dashboard
```

## JSP → Servlet Mapping

| JSP Element | Servlet Action | HTTP Method |
|---|---|---|
| Page load (index.jsp) | `action=list` | GET |
| Page load (index.jsp) | `action=dashboard` | GET |
| Page load (index.jsp) | `action=backup-status` | GET |
| Search input `oninput` | `action=list` (with search param) | GET |
| Status filter `onchange` | `action=list` (with status param) | GET |
| Employee filter `onchange` | `action=list` (with employee param) | GET |
| Date From `onchange` | `action=list` (with date_from param) | GET |
| Date To `onchange` | `action=list` (with date_to param) | GET |
| Clear Filters button | `action=list` (no params) | GET |
| + Add Claim button | Opens modal (client-side only) | - |
| Modal form submit | `action=save` | POST |
| Staff Number `onblur` | `action=lookup-employee` | GET |
| Edit button | `action=get` (then opens modal) | GET |
| Delete button | `action=delete` | POST |
| Export Excel button | `action=export` | GET |
| Import Excel button | `action=import` | POST |
| Rollback button | `action=rollback` | POST |

## Servlet → Database Mapping

| Servlet Action | ClaimsDAO Method | SQL Table |
|---|---|---|
| `list` | `getClaims()` | CHSS_CLAIMS |
| `get` | `getClaimById()` | CHSS_CLAIMS |
| `dashboard` | `getDashboardStats()` | CHSS_CLAIMS |
| `save` (new) | `insertClaim()` | CHSS_CLAIMS |
| `save` (update) | `updateClaim()` | CHSS_CLAIMS |
| `delete` | `deleteClaim()` | CHSS_CLAIMS |
| `export` | `getAllClaimsForExport()` | CHSS_CLAIMS |
| `import` | `backupClaims()` + `deleteAllClaims()` + `bulkInsertClaims()` | CHSS_CLAIMS + CHSS_CLAIMS_BACKUP |
| `rollback` | `restoreFromBackup()` + `clearBackup()` | CHSS_CLAIMS + CHSS_CLAIMS_BACKUP |
| `backup-status` | `hasBackup()` | CHSS_CLAIMS_BACKUP |
| `lookup-employee` | `lookupEmployee()` | EMPLOYEE_MASTER (placeholder) |

## JavaScript Function → Servlet Action

| JavaScript Function | Servlet Action | Trigger |
|---|---|---|
| `loadDashboard()` | `action=dashboard` | Page load, after list |
| `loadClaims()` | `action=list` | Page load, filter change, after save/delete |
| `handleSubmit()` | `action=save` | Modal form submit |
| `editClaim(id)` | `action=get` | Edit button click |
| `deleteClaim(id)` | `action=delete` | Delete button click |
| `exportExcel()` | `action=export` | Export button click |
| `handleImport()` | `action=import` | File input change |
| `rollback()` | `action=rollback` | Rollback button click |
| `checkBackup()` | `action=backup-status` | Page load |
| `lookupEmployee()` | `action=lookup-employee` | Staff number blur |

## Complete Data Flow: Add Claim

```
1. User clicks "+ Add Claim"
   → openModal() (client-side)

2. User fills form, clicks Save
   → handleSubmit(event)

3. handleSubmit() builds form data
   → POST /claims?action=save
   → ClaimsServlet.doPost()

4. ClaimsServlet extracts parameters
   → new ClaimsDAO(getProvider())
   → dao.insertClaim(claim)

5. ClaimsDAO executes INSERT
   → PreparedStatement on CHSS_CLAIMS table
   → Returns generated ID

6. ClaimsServlet returns JSON
   → {"message":"Claim created successfully","id":N}

7. JavaScript receives response
   → closeModal()
   → loadClaims() (refreshes table)
```

## Complete Data Flow: Import Excel

```
1. User clicks "Import Excel"
   → File input dialog opens

2. User selects .xlsx file
   → handleImport(input)

3. JavaScript confirms, sends FormData
   → POST /claims?action=import
   → ClaimsServlet.doPost()

4. ClaimsServlet reads uploaded file
   → ExcelUtil.importFromExcel(inputStream)
   → Returns ImportResult (list of Claims + errors)

5. ClaimsServlet backs up current data
   → dao.backupClaims() → CHSS_CLAIMS_BACKUP

6. ClaimsServlet replaces data
   → dao.deleteAllClaims() → DELETE FROM CHSS_CLAIMS
   → dao.bulkInsertClaims(claims) → INSERT INTO CHSS_CLAIMS

7. ClaimsServlet returns JSON
   → {"message":"Import successful","inserted":N,"skipped":N}

8. JavaScript shows result, refreshes table
   → loadClaims()
   → checkBackup()
```
