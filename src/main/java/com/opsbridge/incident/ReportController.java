package com.opsbridge.incident;

import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/incidents")
public class ReportController {
    private final IncidentService service;
    public ReportController(IncidentService service) { this.service = service; }

    @GetMapping(value="/{id}/report", produces="text/plain; charset=UTF-8")
    public String report(@PathVariable Long id) {
        Incident i = service.findById(id);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder b = new StringBuilder();
        b.append("OPSB​​RIDGE - RELATÓRIO DE ESCALONAMENTO\n".replace("​​",""));
        b.append("=====================================\n");
        b.append("Incidente: #").append(i.getId()).append(" - ").append(i.getTitle()).append("\n");
        b.append("Categoria: ").append(i.getCategory()).append("\n");
        b.append("Prioridade: ").append(i.getPriority()).append("\n");
        b.append("Status: ").append(i.getStatus()).append("\n");
        b.append("Solicitante: ").append(i.getReporter()).append("\n");
        b.append("Responsável: ").append(i.getAssignedTo() == null ? "Não atribuído" : i.getAssignedTo()).append("\n");
        b.append("Criado em: ").append(i.getCreatedAt().format(fmt)).append("\n\n");
        b.append("DESCRIÇÃO\n").append(i.getDescription()).append("\n\n");
        b.append("DIAGNÓSTICO EXECUTADO\n");
        if (i.getChecks().isEmpty()) b.append("- Nenhuma verificação registrada\n");
        for (DiagnosticCheck c : i.getChecks()) {
            b.append("- [").append(c.getStatus()).append("] ").append(c.getTitle()).append("\n");
            b.append("  Esperado: ").append(c.getExpectedResult()).append("\n");
            if (c.getNotes() != null && !c.getNotes().isBlank()) b.append("  Evidência: ").append(c.getNotes()).append("\n");
        }
        b.append("\nRESOLUÇÃO\n").append(i.getResolution() == null ? "Ainda não registrada" : i.getResolution()).append("\n\n");
        b.append("HISTÓRICO\n");
        for (IncidentActivity a : i.getActivities()) {
            b.append("- ").append(a.getCreatedAt().format(fmt)).append(" | ").append(a.getType()).append(" | ").append(a.getDetails()).append("\n");
        }
        b.append("\nAtenção: remova segredos e dados pessoais antes de compartilhar este relatório.\n");
        return b.toString();
    }
}
