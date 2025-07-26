package org.example.chronoadmin.service;

import org.example.chronoadmin.model.AdminScratchCard;
import org.example.chronoadmin.model.AdminLicense;
import org.example.chronoadmin.model.SalesRequest;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminLicenseService {

    private static final String DB_URL = "jdbc:h2:./admin_data/admindb";

    public static List<AdminScratchCard> generateScratchCards(String salesPersonId, String salesPersonName,
                                                             String territory, int quantity) throws Exception {
        List<AdminScratchCard> cards = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, "admin", "admin123")) {
            String sql = "INSERT INTO scratch_cards (scratch_code, sales_person_id, sales_person_name, embedded_password, territory) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (int i = 0; i < quantity; i++) {
                String scratchCode = AdminCryptographicService.generateScratchCode();
                String embeddedPassword = AdminCryptographicService.createScratchCardEmbeddedPassword(
                    scratchCode, salesPersonId, salesPersonName, territory
                );

                stmt.setString(1, scratchCode);
                stmt.setString(2, salesPersonId);
                stmt.setString(3, salesPersonName);
                stmt.setString(4, embeddedPassword);
                stmt.setString(5, territory);
                stmt.executeUpdate();

                AdminScratchCard card = new AdminScratchCard();
                card.setScratchCode(scratchCode);
                card.setSalesPersonId(salesPersonId);
                card.setSalesPersonName(salesPersonName);
                card.setEmbeddedPassword(embeddedPassword);
                card.setTerritory(territory);
                card.setCreatedAt(LocalDateTime.now());

                cards.add(card);
            }
        }

        return cards;
    }

    public static List<AdminScratchCard> getAllScratchCards() throws Exception {
        List<AdminScratchCard> cards = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, "admin", "admin123")) {
            String sql = "SELECT * FROM scratch_cards ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AdminScratchCard card = new AdminScratchCard();
                card.setId(rs.getLong("id"));
                card.setScratchCode(rs.getString("scratch_code"));
                card.setSalesPersonId(rs.getString("sales_person_id"));
                card.setSalesPersonName(rs.getString("sales_person_name"));
                card.setEmbeddedPassword(rs.getString("embedded_password"));
                card.setTerritory(rs.getString("territory"));
                card.setUsed(rs.getBoolean("is_used"));
                card.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                if (rs.getTimestamp("used_at") != null) {
                    card.setUsedAt(rs.getTimestamp("used_at").toLocalDateTime());
                }
                cards.add(card);
            }
        }

        return cards;
    }

    public static void processSalesPersonKey(String salesPersonKey) throws Exception {
        Map<String, String> salesData = AdminCryptographicService.decryptSalesPersonKey(salesPersonKey);

        try (Connection conn = DriverManager.getConnection(DB_URL, "admin", "admin123")) {
            String sql = "INSERT INTO sales_requests (scratch_code, sales_person_key, customer_details, system_fingerprint) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            String customerDetails = String.format("Name: %s, Email: %s, Phone: %s, Address: %s",
                salesData.get("customerName"), salesData.get("customerEmail"),
                salesData.get("customerPhone"), salesData.get("customerAddress"));

            stmt.setString(1, salesData.get("scratchCode"));
            stmt.setString(2, salesPersonKey);
            stmt.setString(3, customerDetails);
            stmt.setString(4, salesData.get("systemFingerprint"));
            stmt.executeUpdate();

            // Mark scratch card as used
            String updateSql = "UPDATE scratch_cards SET is_used = TRUE, used_at = CURRENT_TIMESTAMP WHERE scratch_code = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, salesData.get("scratchCode"));
            updateStmt.executeUpdate();
        }
    }

    public static List<SalesRequest> getPendingSalesRequests() throws Exception {
        List<SalesRequest> requests = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, "admin", "admin123")) {
            String sql = """
                SELECT sr.*, sc.sales_person_id, sc.sales_person_name, sc.territory 
                FROM sales_requests sr 
                JOIN scratch_cards sc ON sr.scratch_code = sc.scratch_code 
                WHERE sr.is_processed = FALSE 
                ORDER BY sr.received_at DESC
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SalesRequest request = new SalesRequest();
                request.setId(rs.getLong("id"));
                request.setScratchCode(rs.getString("scratch_code"));
                request.setSalesPersonKey(rs.getString("sales_person_key"));
                request.setCustomerDetails(rs.getString("customer_details"));
                request.setSystemFingerprint(rs.getString("system_fingerprint"));
                request.setReceivedAt(rs.getTimestamp("received_at").toLocalDateTime());
                request.setSalesPersonId(rs.getString("sales_person_id"));
                request.setSalesPersonName(rs.getString("sales_person_name"));
                request.setTerritory(rs.getString("territory"));
                requests.add(request);
            }
        }

        return requests;
    }

    public static String generateLicense(SalesRequest request) throws Exception {
        String licenseKey = AdminCryptographicService.generateLicenseKey(
            request.getScratchCode(),
            request.getSalesPersonId(),
            request.getCustomerDetails(),
            request.getSystemFingerprint()
        );

        try (Connection conn = DriverManager.getConnection(DB_URL, "admin", "admin123")) {
            // Insert license record
            String sql = "INSERT INTO licenses (license_key, scratch_code, sales_person_id, customer_details, system_fingerprint, admin_signature, expires_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, licenseKey);
            stmt.setString(2, request.getScratchCode());
            stmt.setString(3, request.getSalesPersonId());
            stmt.setString(4, request.getCustomerDetails());
            stmt.setString(5, request.getSystemFingerprint());
            stmt.setString(6, "ADMIN_SIGNED");
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now().plusYears(1)));
            stmt.executeUpdate();

            // Mark request as processed
            String updateSql = "UPDATE sales_requests SET is_processed = TRUE, license_generated = TRUE WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setLong(1, request.getId());
            updateStmt.executeUpdate();
        }

        return licenseKey;
    }

    public static List<AdminLicense> getAllLicenses() throws Exception {
        List<AdminLicense> licenses = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, "admin", "admin123")) {
            String sql = "SELECT * FROM licenses ORDER BY issued_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AdminLicense license = new AdminLicense();
                license.setId(rs.getLong("id"));
                license.setLicenseKey(rs.getString("license_key"));
                license.setScratchCode(rs.getString("scratch_code"));
                license.setSalesPersonId(rs.getString("sales_person_id"));
                license.setCustomerDetails(rs.getString("customer_details"));
                license.setSystemFingerprint(rs.getString("system_fingerprint"));
                license.setActive(rs.getBoolean("is_active"));
                license.setIssuedAt(rs.getTimestamp("issued_at").toLocalDateTime());
                license.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                license.setAdminSignature(rs.getString("admin_signature"));
                licenses.add(license);
            }
        }

        return licenses;
    }
}
