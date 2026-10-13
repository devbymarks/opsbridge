# OpsBridge — Ops Command

Central demonstrativa de incidentes e diagnóstico de TI, pensada para simular rotinas de suporte N1/N2: abertura e acompanhamento de incidentes, checklist de troubleshooting, evidências, responsáveis, histórico e relatório técnico.

> **Estado honesto do pacote:** inclui frontend, API, banco, testes automatizados de serviço e testes de integração HTTP usando H2. O código foi revisado e o JavaScript pode ser validado localmente. Este ambiente de geração não possui Docker nem Maven e não permite baixar dependências, portanto a suíte Java ainda precisa ser executada no computador de teste. Não use o MVP em produção nem exponha publicamente: autenticação e autorização ainda não existem.

## Funcionalidades

- Dashboard com contadores de incidentes e fila pesquisável/filtrável.
- Cadastro com categoria, prioridade, descrição e solicitante.
- Checklist inicial sugerido conforme categoria (rede/DNS, banco de dados ou aplicação).
- Alteração de status e atribuição de responsável.
- Adição de verificações e atualização de resultado/evidências.
- Linha do tempo das ações.
- Registro de resolução e relatório TXT para escalonamento.
- Validação de campos e respostas de erro para incidentes inexistentes.
- Tema Ops Command: interface escura, azul elétrico, layout responsivo.

## Stack

- Java 21 + Spring Boot 3.5
- Spring Web, Spring Data JPA e Bean Validation
- PostgreSQL 16
- HTML, CSS e JavaScript sem framework
- Docker Compose
- Maven
- H2 para testes de integração automatizados

## Opção A — executar tudo com Docker (recomendado)

Requisitos: Docker Desktop recente no Windows ou Docker Engine + Docker Compose v2 no Linux/macOS. O Docker constrói a aplicação Java e executa os testes Maven durante a construção da imagem, sem exigir Java ou Maven instalados na máquina.

1. Extraia o ZIP.
2. Abra um terminal na pasta `opsbridge-completo`.
3. Opcional: copie `.env.example` para `.env` e ajuste as portas/credenciais locais.
4. Execute:

   ```bash
   docker compose up --build
   ```

5. Aguarde os serviços ficarem saudáveis e abra **http://localhost:8080**.
6. Para verificar o estado:

   ```bash
   docker compose ps
   docker compose logs -f app
   ```

7. Para parar sem apagar os dados:

   ```bash
   docker compose down
   ```

Para apagar também o volume de banco e todos os dados locais, use `docker compose down -v` (ação destrutiva).

### Windows — atalho

Também está incluído `iniciar-windows.bat`. Dê dois cliques ou execute pelo terminal na raiz do projeto. Ele verifica se o Docker está disponível e inicia a composição com build.

## Opção B — executar com Java/Maven local

Requisitos: JDK 21, Maven 3.9+ e Docker Compose v2.

```bash
docker compose up -d postgres
mvn test
mvn spring-boot:run
```

Acesse http://localhost:8080. Para encerrar a aplicação, pressione `Ctrl+C`; o banco pode ser parado com `docker compose stop postgres`.

## Testes automatizados

Os testes cobrem a criação do incidente e o checklist sugerido, além de um fluxo HTTP completo em banco H2: listar, detalhar, mudar status, atribuir responsável, atualizar evidência, adicionar etapa, resolver, exportar relatório, validar 404 e rejeitar payload inválido.

Execute na raiz do projeto:

```bash
mvn test
```

A imagem Docker executa `mvn test package` antes de montar a imagem final. Se algum teste falhar, o build falha em vez de entregar silenciosamente uma imagem com teste vermelho.

## Smoke test contra a aplicação em execução

Com o sistema iniciado em `http://localhost:8080`, em outro terminal execute:

```bash
python scripts/smoke-test.py
```

Se a aplicação estiver em outro endereço, defina `OPSBRIDGE_URL` antes de executar. Exemplo PowerShell:

```powershell
$env:OPSBRIDGE_URL = "http://localhost:8080"
python .\scripts\smoke-test.py
```

O smoke test cria um incidente marcado `[SMOKE TEST]` e mantém o registro no banco para inspeção. Ele imprime cada etapa e retorna código diferente de zero se uma verificação falhar.

## API REST

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/incidents` | Lista incidentes |
| `POST` | `/api/incidents` | Cria incidente |
| `GET` | `/api/incidents/{id}` | Consulta detalhe e histórico |
| `PATCH` | `/api/incidents/{id}/status` | Atualiza status |
| `PATCH` | `/api/incidents/{id}/assignee` | Atribui responsável |
| `POST` | `/api/incidents/{id}/checks` | Adiciona etapa de diagnóstico |
| `PATCH` | `/api/incidents/{id}/checks/{checkId}` | Atualiza resultado/evidência |
| `POST` | `/api/incidents/{id}/resolve` | Registra resolução |
| `GET` | `/api/incidents/{id}/report` | Exporta relatório em texto |

Valores válidos de status: `OPEN`, `IN_PROGRESS`, `WAITING_FOR_USER`, `ESCALATED`, `RESOLVED`, `CLOSED`.

Prioridades: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

Resultados de diagnóstico: `PENDING`, `PASSED`, `FAILED`, `INCONCLUSIVE`, `NOT_APPLICABLE`.

## Estrutura principal

```text
opsbridge-completo/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── iniciar-windows.bat
├── pom.xml
├── scripts/smoke-test.py
├── src/main/java/br/devbymarks/opsbridge/
│   ├── OpsBridgeApplication.java
│   └── incident/                  # entidades, DTOs, serviços e endpoints
├── src/main/resources/
│   ├── application.yml
│   └── static/                    # index.html, styles.css, app.js
└── src/test/
    ├── java/                      # testes de serviço e integração HTTP
    └── resources/application-test.yml
```

## Segurança e limitações conhecidas

- É um MVP de portfólio; não há login, controle de acesso, CSRF para uma futura autenticação ou gestão de perfis.
- Credenciais padrão e `.env.example` servem apenas para desenvolvimento local.
- `ddl-auto: update` é usado para facilitar o ambiente de demonstração. Para produção, adote Flyway/Liquibase e migrações revisadas.
- Use dados fictícios. Não coloque senhas, tokens, dados pessoais ou informações confidenciais nas evidências.
- Não exponha a porta do serviço para a internet sem autenticação, autorização, gestão de segredos, HTTPS, logs e política de backup.

## Objetivo profissional

Demonstrar troubleshooting estruturado, API REST, persistência relacional, validação, histórico de auditoria funcional, documentação e comunicação entre suporte N1/N2.
