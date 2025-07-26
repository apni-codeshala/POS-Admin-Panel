package org.example.chronoadmin.model;

import java.time.LocalDateTime;

public class SalesRequest {
    private Long id;
    private String scratchCode;
    private String salesPersonKey;
    private String customerDetails;
    private String systemFingerprint;
    private LocalDateTime receivedAt;
    private boolean isProcessed;
    private boolean licenseGenerated;
    private String salesPersonId;
    private String salesPersonName;
    private String territory;

    // Constructors
    public SalesRequest() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getScratchCode() { return scratchCode; }
    public void setScratchCode(String scratchCode) { this.scratchCode = scratchCode; }

    public String getSalesPersonKey() { return salesPersonKey; }
    public void setSalesPersonKey(String salesPersonKey) { this.salesPersonKey = salesPersonKey; }

    public String getCustomerDetails() { return customerDetails; }
    public void setCustomerDetails(String customerDetails) { this.customerDetails = customerDetails; }

    public String getSystemFingerprint() { return systemFingerprint; }
    public void setSystemFingerprint(String systemFingerprint) { this.systemFingerprint = systemFingerprint; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public boolean isProcessed() { return isProcessed; }
    public void setProcessed(boolean processed) { isProcessed = processed; }

    public boolean isLicenseGenerated() { return licenseGenerated; }
    public void setLicenseGenerated(boolean licenseGenerated) { this.licenseGenerated = licenseGenerated; }

    public String getSalesPersonId() { return salesPersonId; }
    public void setSalesPersonId(String salesPersonId) { this.salesPersonId = salesPersonId; }

    public String getSalesPersonName() { return salesPersonName; }
    public void setSalesPersonName(String salesPersonName) { this.salesPersonName = salesPersonName; }

    public String getTerritory() { return territory; }
    public void setTerritory(String territory) { this.territory = territory; }
}
