package com.opsbridge.incident;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {
    @Mock IncidentRepository incidents;
    @Mock DiagnosticCheckRepository checks;
    @InjectMocks IncidentService service;

    @Test
    void shouldCreateIncidentAndSuggestChecksForDatabaseCategory() {
        CreateIncidentRequest request = new CreateIncidentRequest(
            " Banco indisponível ", " Falha de conexão ", "DATABASE", IncidentPriority.HIGH, "Suporte N1"
        );
        when(incidents.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incident result = service.create(request);

        assertEquals("Banco indisponível", result.getTitle());
        assertEquals(IncidentStatus.OPEN, result.getStatus());
        assertEquals(3, result.getChecks().size());
        assertTrue(result.getActivities().stream().anyMatch(a -> a.getType().equals("INCIDENT_CREATED")));
        verify(incidents).save(any(Incident.class));
    }

    @Test
    void shouldThrowWhenIncidentDoesNotExist() {
        when(incidents.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IncidentNotFoundException.class, () -> service.findById(99L));
    }
}
