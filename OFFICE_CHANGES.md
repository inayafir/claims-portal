# OFFICE_CHANGES.md - Intern Checklist

## What You Must NOT Modify

These files are complete and ready to copy. Do not change them:

- `Claim.java` - Model class, no changes needed
- `DateUtil.java` - Utility, no changes needed
- `ExcelUtil.java` - Excel logic, no changes needed
- `ClaimsDAO.java` - SQL queries, IT will modify table names
- `ClaimsServlet.java` - Servlet logic, IT will modify getProvider()
- `ConnectionProvider.java` - Interface, IT will implement
- `index.jsp` - Frontend, no changes needed
- `style.css` - Styling, no changes needed
- `script.js` - Client logic, no changes needed

## What You Must Bring to Office

1. USB drive with `sandesh-integration/` folder
2. 8 Apache POI JAR files (downloaded separately)
3. Printed copy of this checklist
4. Printed copy of IT_INTEGRATION.md (for CIG team)

## At the Office

1. Copy Java files into Eclipse project `src/` structure
2. Copy JSP/CSS/JS into `WebContent/` structure
3. Copy JARs into `WebContent/WEB-INF/lib/`
4. Give `schema.sql` and `IT_INTEGRATION.md` to CIG team
5. Ask CIG to implement ConnectionProvider
6. Ask CIG to create database tables
7. Ask CIG to replace employee table placeholders
8. Test the application after deployment

## Do NOT

- Do not try to run the application locally
- Do not install any software
- Do not modify any Java files (IT will do that)
- Do not create database connections (IT will do that)
- Do not add any authentication code
- Do not add any Spring/Maven dependencies
