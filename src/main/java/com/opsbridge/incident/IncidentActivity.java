package com.opsbridge.incident;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_activities")
public class IncidentActivity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;
    @Column(nullable = false, length = 40)
    private String type;
    @Column(nullable = false, length = 2000)
    private String details;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected IncidentActivity() {}
    public IncidentActivity(Incident incident, String type, String details) {
        this.incident = incident; this.type = type; this.details = details;
    }
    public Long getId(){return id;}
    public String getType(){return type;}
    public String getDetails(){return details;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
