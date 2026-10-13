package com.opsbridge.incident;
public class CheckNotFoundException extends RuntimeException {
    public CheckNotFoundException(Long id) { super("Verificação " + id + " não encontrada neste incidente."); }
}
