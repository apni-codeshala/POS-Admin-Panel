package org.example.chronoadmin.model;

import java.time.LocalDateTime;

public class AdminScratchCard {
    private Long id;
    private String scratchCode;
    private String salesPersonId;
    private String salesPersonName;
    private String embeddedPassword;
    private String territory;
    private boolean isUsed;
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;

    // Constructors
    public AdminScratchCard() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getScratchCode() { return scratchCode; }
    public void setScratchCode(String scratchCode) { this.scratchCode = scratchCode; }

    public String getSalesPersonId() { return salesPersonId; }
    public void setSalesPersonId(String salesPersonId) { this.salesPersonId = salesPersonId; }

    public String getSalesPersonName() { return salesPersonName; }
    public void setSalesPersonName(String salesPersonName) { this.salesPersonName = salesPersonName; }

    public String getEmbeddedPassword() { return embeddedPassword; }
    public void setEmbeddedPassword(String embeddedPassword) { this.embeddedPassword = embeddedPassword; }

    public String getTerritory() { return territory; }
    public void setTerritory(String territory) { this.territory = territory; }

    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
