#!/usr/bin/env python3
"""Teste ponta a ponta da API OpsBridge. Requer a aplicação já iniciada."""
import json
import os
import sys
import urllib.error
import urllib.request

BASE = os.environ.get("OPSBRIDGE_URL", "http://localhost:8080").rstrip("/")
checks_passed = 0

def request(path, method="GET", payload=None, expected=200):
    global checks_passed
    data = None if payload is None else json.dumps(payload, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(BASE + path, data=data, method=method,
                                 headers={"Content-Type": "application/json; charset=utf-8"})
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            status, raw, content_type = response.status, response.read(), response.headers.get("Content-Type", "")
    except urllib.error.HTTPError as exc:
        status, raw, content_type = exc.code, exc.read(), exc.headers.get("Content-Type", "")
    if status != expected:
        raise RuntimeError(f"{method} {path}: esperado HTTP {expected}, recebido {status}: {raw.decode('utf-8', 'replace')}")
    checks_passed += 1
    print(f"[OK] {method} {path} -> HTTP {status}")
    text = raw.decode("utf-8", "replace")
    if "json" in content_type:
        return json.loads(text)
    return text

def ensure(condition, message):
    global checks_passed
    if not condition:
        raise RuntimeError(message)
    checks_passed += 1
    print(f"[OK] {message}")

try:
    listing = request("/api/incidents")
    ensure(isinstance(listing, list), "GET da lista retorna um array JSON")

    incident = request("/api/incidents", "POST", {
        "title": "[SMOKE TEST] Banco indisponível",
        "description": "Teste automatizado local do ciclo de vida do incidente.",
        "category": "DATABASE", "priority": "HIGH", "reporter": "Smoke Test"
    }, 201)
    incident_id = incident["id"]
    ensure(incident.get("status") == "OPEN", "Incidente novo inicia como OPEN")
    ensure(len(incident.get("checks", [])) >= 3, "Checklist inicial é criado para banco de dados")

    detail = request(f"/api/incidents/{incident_id}")
    ensure(detail["id"] == incident_id, "Detalhe corresponde ao incidente criado")
    request(f"/api/incidents/{incident_id}/status", "PATCH", {"status": "IN_PROGRESS"})
    request(f"/api/incidents/{incident_id}/assignee", "PATCH", {"assignedTo": "Técnico Smoke Test"})

    check_id = detail["checks"][0]["id"]
    updated_check = request(f"/api/incidents/{incident_id}/checks/{check_id}", "PATCH", {
        "status": "FAILED", "notes": "Evidência fictícia criada pelo teste automatizado."
    })
    ensure(updated_check.get("status") == "FAILED", "Resultado do checklist persiste")

    extra = request(f"/api/incidents/{incident_id}/checks", "POST", {
        "title": "Validar porta de banco", "expectedResult": "Porta acessível no ambiente de teste"
    }, 201)
    ensure(extra.get("title") == "Validar porta de banco", "Nova etapa de diagnóstico é criada")

    resolved = request(f"/api/incidents/{incident_id}/resolve", "POST", {
        "resolution": "Resolução fictícia registrada pelo smoke test."
    })
    ensure(resolved.get("status") == "RESOLVED", "Resolução muda o status para RESOLVED")

    report = request(f"/api/incidents/{incident_id}/report")
    ensure("RELATÓRIO DE ESCALONAMENTO" in report and str(incident_id) in report,
           "Relatório TXT contém o incidente correto")
    request("/api/incidents/987654321", expected=404)
    print(f"\nSUCESSO: {checks_passed} verificações passaram. Incidente de teste criado: #{incident_id}.")
    print("Nota: o incidente [SMOKE TEST] permanece no banco; exclua-o manualmente se quiser limpar os dados.")
except Exception as exc:
    print(f"\nFALHA NO TESTE: {exc}", file=sys.stderr)
    sys.exit(1)
