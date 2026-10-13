package com.opsbridge.incident;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnostic_checks")
public class DiagnosticCheck {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, length = 1000)
    private String expectedResult;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private CheckStatus status = CheckStatus.PENDING;
    @Column(length = 2000)
    private String notes;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected DiagnosticCheck() {}
    public DiagnosticCheck(String title, String expectedResult) {
        this.title = title; this.expectedResult = expectedResult;
    }
    void setIncident(Incident incident) { this.incident = incident; }
    public void update(CheckStatus status, String notes) {
        this.status = status; this.notes = notes; this.updatedAt = LocalDateTime.now();
        incident.addActivity("DIAGNOSTIC_UPDATED", "Verificação '" + title + "' marcada como " + status);
    }
    public Long getId(){return id;}
    public String getTitle(){return title;}
    public String getExpectedResult(){return expectedResult;}
    public CheckStatus getStatus(){return status;}
    public String getNotes(){return notes;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
}
