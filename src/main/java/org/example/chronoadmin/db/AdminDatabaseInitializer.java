package org.example.chronoadmin.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AdminDatabaseInitializer {

    private static final String DB_URL = "jdbc:h2:./admin_data/admindb";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL, "admin", "admin123");
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
