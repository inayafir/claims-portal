# LIBRARY_GUIDE.md - JAR Dependencies

## Overview

This module requires Apache POI for Excel import/export. All other dependencies (Servlet API, JSP API) are provided by Tomcat.

## Required JARs (copy to WEB-INF/lib/)

### 1. poi-ooxml-4.1.2.jar
- **Purpose:** High-level Excel file operations (Workbook, Sheet, Row, Cell)
- **Required by:** `ExcelUtil.java` (both import and export)
- **Size:** ~4.5 MB
- **Must copy:** Yes

### 2. poi-4.1.2.jar
- **Purpose:** Core POI interfaces and classes (CellType, CellStyle, Font, etc.)
- **Required by:** `ExcelUtil.java`
- **Size:** ~2.8 MB
- **Must copy:** Yes

### 3. poi-ooxml-lite-4.1.2.jar
- **Purpose:** Lightweight OOXML implementation for poi-ooxml
- **Required by:** poi-ooxml-4.1.2.jar (transitive dependency)
- **Size:** ~1.2 MB
- **Must copy:** Yes

### 4. commons-codec-1.13.jar
- **Purpose:** Encoding/decoding utilities (Base64, Hex, etc.)
- **Required by:** poi-4.1.2.jar (transitive dependency)
- **Size:** ~280 KB
- **Must copy:** Yes

### 5. commons-collections4-4.4.jar
- **Purpose:** Collection utilities (bags, bidirectional maps, etc.)
- **Required by:** poi-4.1.2.jar (transitive dependency)
- **Size:** ~590 KB
- **Must copy:** Yes

### 6. commons-math3-3.6.1.jar
- **Purpose:** Mathematical/statistical functions
- **Required by:** poi-4.1.2.jar (transitive dependency)
- **Size:** ~2.2 MB
- **Must copy:** Yes

### 7. xmlbeans-3.1.0.jar
- **Purpose:** XML schema Java binding (reads OOXML format)
- **Required by:** poi-ooxml-4.1.2.jar (transitive dependency)
- **Size:** ~1.7 MB
- **Must copy:** Yes

### 8. curvesapi-1.06.jar
- **Purpose:** Bezier curve math (used for chart rendering in OOXML)
- **Required by:** poi-ooxml-4.1.2.jar (transitive dependency)
- **Size:** ~40 KB
- **Must copy:** Yes

## JARs That Should NOT Be Copied

| JAR | Reason |
|---|---|
| servlet-api.jar | Provided by Tomcat |
| jsp-api.jar | Provided by Tomcat |
| jstl.jar | Not used by this module |
| mysql-connector-java.jar | IT provides the database driver |
| Any Spring JAR | Not used (plain Servlet) |
| Any Hibernate JAR | Not used (plain JDBC) |

## Download Source

All JARs can be downloaded from Maven Central:
- https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml/4.1.2
- Download the "jar" for each artifact

## If POI JARs Already Exist in Sandesh

Check if Apache POI is already in the project's `WEB-INF/lib/`. If version 4.x is present, you may not need to copy these JARs. If an older version (3.x) is present, you may need to upgrade.

## How to Verify

After copying JARs, check that these imports compile in `ExcelUtil.java`:
```java
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
```

If compilation fails, a required JAR is missing.
