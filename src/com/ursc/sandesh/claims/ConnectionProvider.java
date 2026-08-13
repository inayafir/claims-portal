package com.ursc.sandesh.claims;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for obtaining a database connection.
 *
 * IT (CIG team) MUST implement this interface and make it available
 * to the application through their existing Sandesh connection mechanism.
 *
 * The simplest implementation for IT:
 *
 *   public class SandeshConnectionProvider implements ConnectionProvider {
 *       public Connection getConnection() throws SQLException {
 *           // Return a Connection using your existing Sandesh database mechanism
 *           return yourExistingConnectionMethod();
 *       }
 *   }
 *
 * The application will call getConnection() whenever it needs database access.
 * IT must ensure the returned Connection is valid and has appropriate permissions
 * for the CHSS_CLAIMS and CHSS_CLAIMS_BACKUP tables.
 */
public interface ConnectionProvider {

    /**
     * Returns a JDBC Connection to the database.
     *
     * @return a valid, open Connection
     * @throws SQLException if a connection cannot be obtained
     */
    Connection getConnection() throws SQLException;
}
