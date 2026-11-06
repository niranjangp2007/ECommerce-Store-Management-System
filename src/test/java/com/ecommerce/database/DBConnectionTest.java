package com.ecommerce.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JDBC DBConnection Configuration & Resilience Verification")
public class DBConnectionTest {

    @Test
    @DisplayName("1. DBConnection resolves URL and User cleanly")
    void testConfigurationResolution() {
        String url = DBConnection.getDatabaseUrl();
        assertNotNull(url, "Database URL must not be null.");
        assertTrue(url.startsWith("jdbc:mysql://"), "Database URL must be a valid MySQL JDBC URL.");

        String user = DBConnection.getDatabaseUser();
        assertNotNull(user, "Database user must not be null.");
        assertFalse(user.trim().isEmpty(), "Database user must not be empty.");
    }

    @Test
    @DisplayName("2. diagnoseConnection() returns structured sanitized report")
    void testDiagnoseConnection() {
        DBConnection.ConnectionDiagnostic diagnostic = DBConnection.diagnoseConnection();
        assertNotNull(diagnostic, "Diagnostic result must not be null.");
        assertNotNull(diagnostic.getCategory(), "Diagnostic category must not be null.");
        assertNotNull(diagnostic.getMessage(), "Diagnostic message must not be null.");

        System.out.println("=================================================");
        System.out.println(diagnostic);
        System.out.println("=================================================");

        // Verify that under no circumstances is a password printed in the message
        assertFalse(diagnostic.getMessage().contains("password="), "Password parameter must not appear in message.");
        assertFalse(diagnostic.toString().contains("password="), "Password parameter must not appear in string representation.");
    }
}
