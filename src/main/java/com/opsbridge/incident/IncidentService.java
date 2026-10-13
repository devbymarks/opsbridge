package com.opsbridge.incident;

import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class IncidentService {
    private final IncidentRepository incidents;
    private final DiagnosticCheckRepository checks;

    public IncidentService(IncidentRepository incidents, DiagnosticCheckRepository checks) {
        this.incidents = incidents; this.checks = checks;
    }

    @Transactional
    public Incident create(CreateIncidentRequest request) {
        Incident incident = new Incident(request.title().trim(), request.description().trim(),
            request.category().trim().toUpperCase(), request.priority(), request.reporter().trim());
        addSuggestedChecks(incident, request.category());
        incidents.save(incident);
        return prepareForResponse(incident);
    }

    private void addSuggestedChecks(Incident incident, String category) {
        String c = category.toUpperCase();
        if (c.contains("REDE") || c.contains("NETWORK") || c.contains("DNS")) {
            incident.addCheck(new DiagnosticCheck("Validar conectividade", "Confirmar se o destino responde e registrar o resultado."));
            incident.addCheck(new DiagnosticCheck("Validar resolução DNS", "Confirmar se o nome resolve para o endereço esperado."));
            incident.addCheck(new DiagnosticCheck("Validar porta/serviço", "Confirmar conectividade na porta utilizada pelo serviço."));
        } else if (c.contains("BANCO") || c.contains("DATABASE") || c.contains("SQL")) {
            incident.addCheck(new DiagnosticCheck("Validar disponibilidade do banco", "Confirmar que o serviço do banco está ativo."));
            incident.addCheck(new DiagnosticCheck("Validar conectividade", "Confirmar host, porta e rota de rede."));
            incident.addCheck(new DiagnosticCheck("Validar credenciais e permissões", "Verificar acesso usando credencial autorizada, sem registrar segredos."));
        } else {
            incident.addCheck(new DiagnosticCheck("Reproduzir o problema", "Registrar os passos para reproduzir o erro."));
            incident.addCheck(new DiagnosticCheck("Verificar logs da aplicação", "Registrar mensagens relevantes sem incluir segredos ou dados pessoais."));
            incident.addCheck(new DiagnosticCheck("Validar dependências", "Verificar disponibilidade das dependências envolvidas."));
        }
    }

    @Transactional(readOnly = true)
    public List<Incident> findAll() {
        List<Incident> result = incidents.findAllByOrderByCreatedAtDesc();
        result.forEach(this::initializeResponseCollections);
        return result;
    }

    @Transactional(readOnly = true)
    public Incident findById(Long id) {
        Incident incident = incidents.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
        initializeResponseCollections(incident);
        return incident;
    }

    private Incident prepareForResponse(Incident incident) {
        initializeResponseCollections(incident);
        return incident;
    }

    private void initializeResponseCollections(Incident incident) {
        // O JSON é serializado depois que a transação do service termina.
        // Inicializar as coleções aqui evita LazyInitializationException na API.
        Hibernate.initialize(incident.getChecks());
        Hibernate.initialize(incident.getActivities());
    }

    @Transactional
    public Incident updateStatus(Long id, IncidentStatus status) {
        Incident incident = findById(id); incident.changeStatus(status); incidents.save(incident); return prepareForResponse(incident);
    }

    @Transactional
    public Incident assign(Long id, String assignee) {
        Incident incident = findById(id); incident.assignTo(assignee.trim()); incidents.save(incident); return prepareForResponse(incident);
    }

    @Transactional
    public Incident resolve(Long id, String resolution) {
        Incident incident = findById(id); incident.resolve(resolution.trim()); incidents.save(incident); return prepareForResponse(incident);
    }

    @Transactional
    public DiagnosticCheck addCheck(Long id, CreateCheckRequest request) {
        Incident incident = findById(id);
        DiagnosticCheck check = new DiagnosticCheck(request.title().trim(), request.expectedResult().trim());
        incident.addCheck(check);
        incident.addActivity("DIAGNOSTIC_ADDED", "Verificação adicionada: " + request.title().trim());
        incidents.save(incident);
        return check;
    }

    @Transactional
    public DiagnosticCheck updateCheck(Long incidentId, Long checkId, UpdateCheckRequest request) {
        Incident incident = findById(incidentId);
        DiagnosticCheck check = checks.findByIdAndIncidentId(checkId, incidentId)
            .orElseThrow(() -> new CheckNotFoundException(checkId));
        check.update(request.status(), request.notes());
        return checks.save(check);
    }
}
