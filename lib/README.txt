Claims Portal - Required JARs
================================

Apache POI JARs (required for Excel import/export):

1. poi-ooxml-4.1.2.jar    - High-level Excel operations
2. poi-4.1.2.jar          - Core POI classes
3. poi-ooxml-lite-4.1.2.jar - Lightweight OOXML support
4. commons-codec-1.13.jar - Encoding utilities
5. commons-collections4-4.4.jar - Collection utilities
6. commons-math3-3.6.1.jar - Math utilities
7. xmlbeans-3.1.0.jar     - XML schema binding
8. curvesapi-1.06.jar     - Bezier curve math

All JARs go into: WebContent/WEB-INF/lib/

DO NOT COPY these (Tomcat provides them):
- servlet-api.jar
- jsp-api.jar
- jstl.jar

Download from: https://mvnrepository.com/artifact/org.apache.poi
