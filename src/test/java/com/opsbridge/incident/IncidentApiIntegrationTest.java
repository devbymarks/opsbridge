package com.opsbridge.incident;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidentApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired IncidentRepository incidents;

    @BeforeEach
    void cleanDatabase() {
        incidents.deleteAll();
    }

    @Test
    void shouldSupportIncidentLifecycleThroughHttpApi() throws Exception {
        String created = mvc.perform(post("/api/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Banco indisponível","description":"Aplicação não conecta ao PostgreSQL", "category":"DATABASE", "priority":"HIGH", "reporter":"Suporte N1"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.checks.length()").value(3))
            .andExpect(jsonPath("$.activities[0].type").value("INCIDENT_CREATED"))
            .andReturn().getResponse().getContentAsString();

        long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
            .readTree(created).get("id").asLong();
        long checkId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
            .readTree(created).get("checks").get(0).get("id").asLong();

        mvc.perform(get("/api/incidents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(patch("/api/incidents/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mvc.perform(patch("/api/incidents/{id}/assignee", id)
                .contentType(MediaType.APPLICATION_JSON).content("{\"assignedTo\":\"Técnico N2\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.assignedTo").value("Técnico N2"));

        mvc.perform(patch("/api/incidents/{id}/checks/{checkId}", id, checkId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"FAILED\",\"notes\":\"Conexão recusada na porta do banco\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FAILED"))
            .andExpect(jsonPath("$.notes").value("Conexão recusada na porta do banco"));

        mvc.perform(post("/api/incidents/{id}/checks", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Validar firewall\",\"expectedResult\":\"Porta liberada\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Validar firewall"));

        mvc.perform(post("/api/incidents/{id}/resolve", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"resolution\":\"Serviço do banco reiniciado e conexão validada\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVED"))
            .andExpect(jsonPath("$.resolution").value("Serviço do banco reiniciado e conexão validada"));

        mvc.perform(get("/api/incidents/{id}/report", id))
            .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string(containsString("RELATÓRIO DE ESCALONAMENTO")))
            .andExpect(content().string(containsString("Serviço do banco reiniciado")));

        mvc.perform(get("/api/incidents/{id}", id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.activities.length()").value(7));
    }

    @Test
    void shouldReturnNotFoundForUnknownIncident() throws Exception {
        mvc.perform(get("/api/incidents/987654"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Incidente 987654 não encontrado."));
    }

    @Test
    void shouldRejectInvalidIncidentPayload() throws Exception {
        mvc.perform(post("/api/incidents").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"description\":\"\",\"category\":\"DATABASE\",\"priority\":\"HIGH\",\"reporter\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
