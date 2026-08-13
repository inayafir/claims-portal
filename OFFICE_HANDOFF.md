# OFFICE_HANDOFF.md - Handoff Checklist

## Before Going to Office

### 1. Prepare USB/Transfer

- [ ] Copy entire `sandesh-integration/` folder to USB drive or shared location
- [ ] Verify all 17 files are present (see checklist below)
- [ ] Download all 8 Apache POI JARs (see LIBRARY_GUIDE.md)
- [ ] Place JARs in `lib/` folder on USB

### 2. File Checklist (17 files)

**Java source (6 files):**
- [ ] `src/com/ursc/sandesh/claims/Claim.java`
- [ ] `src/com/ursc/sandesh/claims/ConnectionProvider.java`
- [ ] `src/com/ursc/sandesh/claims/ClaimsDAO.java`
- [ ] `src/com/ursc/sandesh/claims/ClaimsServlet.java`
- [ ] `src/com/ursc/sandesh/claims/util/DateUtil.java`
- [ ] `src/com/ursc/sandesh/claims/util/ExcelUtil.java`

**Web assets (3 files):**
- [ ] `web/index.jsp`
- [ ] `web/css/style.css`
- [ ] `web/js/script.js`

**Database (1 file):**
- [ ] `database/schema.sql`

**Library info (1 file):**
- [ ] `lib/README.txt`

**Documentation (6 files):**
- [ ] `README.md`
- [ ] `CODE_MAP.md`
- [ ] `IT_INTEGRATION.md`
- [ ] `LIBRARY_GUIDE.md`
- [ ] `OFFICE_HANDOFF.md`
- [ ] `OFFICE_CHANGES.md`

## At the Office - Integration Steps

### Step 1: Open Sandesh Eclipse Project

1. Open Eclipse IDE
2. Open the existing Sandesh Dynamic Web Project
3. Locate the `src/` and `WebContent/` directories

### Step 2: Copy Java Files

1. Create package: `src/com/ursc/sandesh/claims/`
2. Copy 5 Java files into `src/com/ursc/sandesh/claims/`
3. Create package: `src/com/ursc/sandesh/claims/util/`
4. Copy 2 utility files into `src/com/ursc/sandesh/claims/util/`

### Step 3: Copy Web Files

1. Copy `index.jsp` to `WebContent/`
2. Create folder: `WebContent/css/`
3. Copy `style.css` to `WebContent/css/`
4. Create folder: `WebContent/js/`
5. Copy `script.js` to `WebContent/js/`

### Step 4: Copy Library JARs

1. Copy all 8 Apache POI JARs to `WebContent/WEB-INF/lib/`

### Step 5: Hand to CIG Team

Give CIG team these files:

1. `database/schema.sql` - For table creation
2. `IT_INTEGRATION.md` - Step-by-step integration guide
3. `CODE_MAP.md` - Wiring reference

### Step 6: CIG Must Do

1. Implement `ConnectionProvider` (see IT_INTEGRATION.md)
2. Update `ClaimsServlet.getProvider()` method
3. Replace employee table placeholders in `ClaimsDAO.java`
4. Create database tables from `schema.sql`
5. Deploy to Tomcat

## Verification After Integration

1. Navigate to `/claims` in browser
2. Main page loads with dashboard
3. Add a test claim
4. Edit the test claim
5. Delete the test claim
6. Export to Excel
7. Import from Excel
8. Rollback
9. Test employee lookup (if employee table is available)
