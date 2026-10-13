package com.opsbridge.incident;
public class IncidentNotFoundException extends RuntimeException {
    public IncidentNotFoundException(Long id) { super("Incidente " + id + " não encontrado."); }
}
