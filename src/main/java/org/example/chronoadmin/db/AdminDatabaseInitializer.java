package org.example.chronoadmin.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AdminDatabaseInitializer {

    private static String dbUrl = null;

    private static String getDbUrl() {
        if (dbUrl == null) {
            // Use user's AppData directory for database storage
            String userHome = System.getProperty("user.home");
            String appDataDir = userHome + File.separator + "AppData" + File.separator + "Local" + File.separator + "AdminChronoPos";

            // Create directory if it doesn't exist
            File dir = new File(appDataDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("Created database directory: " + created);
            }

            dbUrl = "jdbc:h2:" + appDataDir + File.separator + "admindb";
            System.out.println("Database URL: " + dbUrl);
        }
        return dbUrl;
    }

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(getDbUrl(), "admin", "admin123");
             Statement stmt = conn.createStatement()) {

            // Create scratch cards table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS scratch_cards (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    scratch_code VARCHAR(12) UNIQUE NOT NULL,
                    sales_person_id VARCHAR(50) NOT NULL,
                    sales_person_name VARCHAR(100) NOT NULL,
                    embedded_password TEXT NOT NULL,
                    territory VARCHAR(100),
                    is_used BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    used_at TIMESTAMP NULL
                )
            """);

            // Create licenses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS licenses (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    license_key TEXT UNIQUE NOT NULL,
                    scratch_code VARCHAR(12) NOT NULL,
                    sales_person_id VARCHAR(50) NOT NULL,
                    customer_details TEXT NOT NULL,
                    system_fingerprint VARCHAR(255) NOT NULL,
                    is_active BOOLEAN DEFAULT TRUE,
                    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP NOT NULL,
                    admin_signature TEXT NOT NULL
                )
            """);

            // Migration: Update existing license_key column if it exists as VARCHAR
            try {
                stmt.execute("ALTER TABLE licenses ALTER COLUMN license_key TEXT");
                System.out.println("Updated license_key column to TEXT type");
            } catch (Exception e) {
                // Column might already be TEXT or table might not exist yet - this is fine
            }

            // Create sales person requests table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sales_requests (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    scratch_code VARCHAR(12) NOT NULL,
                    sales_person_key TEXT NOT NULL,
                    customer_details TEXT NOT NULL,
                    system_fingerprint VARCHAR(255) NOT NULL,
                    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    is_processed BOOLEAN DEFAULT FALSE,
                    license_generated BOOLEAN DEFAULT FALSE
                )
            """);

            System.out.println("Admin database initialized successfully");

        } catch (Exception e) {
            System.err.println("Error initializing admin database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
