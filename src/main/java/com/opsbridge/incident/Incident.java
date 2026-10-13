package com.opsbridge.incident;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidents")
public class Incident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private IncidentPriority priority;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(nullable = false, length = 100)
    private String reporter;

    @Column(length = 100)
    private String assignedTo;

    @Column(length = 2000)
    private String resolution;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiagnosticCheck> checks = new ArrayList<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<IncidentActivity> activities = new ArrayList<>();

    protected Incident() {}

    public Incident(String title, String description, String category, IncidentPriority priority, String reporter) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.reporter = reporter;
        this.status = IncidentStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        addActivity("INCIDENT_CREATED", "Incidente aberto por " + reporter);
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    public void addCheck(DiagnosticCheck check) { checks.add(check); check.setIncident(this); }
    public void addActivity(String type, String details) { activities.add(new IncidentActivity(this, type, details)); }
    public void changeStatus(IncidentStatus newStatus) {
        IncidentStatus old = this.status;
        this.status = newStatus;
        addActivity("STATUS_CHANGED", "Status alterado de " + old + " para " + newStatus);
    }
    public void assignTo(String assignee) {
        this.assignedTo = assignee;
        addActivity("ASSIGNED", "Responsável definido: " + assignee);
    }
    public void resolve(String resolution) {
        this.resolution = resolution;
        changeStatus(IncidentStatus.RESOLVED);
        addActivity("RESOLUTION", "Resolução registrada: " + resolution);
    }

    public Long getId(){return id;}
    public String getTitle(){return title;}
    public String getDescription(){return description;}
    public String getCategory(){return category;}
    public IncidentPriority getPriority(){return priority;}
    public IncidentStatus getStatus(){return status;}
    public String getReporter(){return reporter;}
    public String getAssignedTo(){return assignedTo;}
    public String getResolution(){return resolution;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public List<DiagnosticCheck> getChecks(){return checks;}
    public List<IncidentActivity> getActivities(){return activities;}
}
