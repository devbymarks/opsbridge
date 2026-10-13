package com.opsbridge.incident;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService service;
    public IncidentController(IncidentService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Incident create(@Valid @RequestBody CreateIncidentRequest request) { return service.create(request); }

    @GetMapping public List<Incident> findAll() { return service.findAll(); }

    @GetMapping("/{id}") public Incident findById(@PathVariable Long id) { return service.findById(id); }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return service.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}/assignee")
    public Incident assign(@PathVariable Long id, @Valid @RequestBody AssignRequest request) {
        return service.assign(id, request.assignedTo());
    }

    @PostMapping("/{id}/resolve")
    public Incident resolve(@PathVariable Long id, @Valid @RequestBody ResolveRequest request) {
        return service.resolve(id, request.resolution());
    }

    @PostMapping("/{id}/checks") @ResponseStatus(HttpStatus.CREATED)
    public DiagnosticCheck addCheck(@PathVariable Long id, @Valid @RequestBody CreateCheckRequest request) {
        return service.addCheck(id, request);
    }

    @PatchMapping("/{id}/checks/{checkId}")
    public DiagnosticCheck updateCheck(@PathVariable Long id, @PathVariable Long checkId,
                                       @Valid @RequestBody UpdateCheckRequest request) {
        return service.updateCheck(id, checkId, request);
    }
}
