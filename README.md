<div align="center">

# ⚡ OpsBridge — Ops Command

**Central de incidentes, troubleshooting e diagnóstico operacional de TI.**

Uma aplicação demonstrativa para organizar rotinas de suporte N1/N2, acompanhar incidentes e registrar evidências técnicas em um único lugar.

</div>

\---

## 📌 Sobre o projeto

O **OpsBridge** simula uma central de operações de TI em que a equipe pode abrir incidentes, acompanhar seu andamento, executar checklists de diagnóstico, registrar evidências e gerar relatórios técnicos para resolução ou escalonamento.

O projeto foi desenvolvido como **MVP de portfólio**, com foco em backend Java, APIs REST, persistência relacional, testes automatizados e uma interface web responsiva com identidade visual escura no estilo **Ops Command**.

> \*\*Status do projeto:\*\* pacote demonstrativo. A aplicação inclui frontend, API, persistência, testes de serviço e testes de integração HTTP com H2. A suíte Java precisa ser executada no ambiente local de desenvolvimento. O MVP ainda não possui autenticação ou autorização e não deve ser exposto publicamente nem utilizado em produção.

## ✨ Funcionalidades

* 📊 Dashboard com contadores e fila de incidentes pesquisável e filtrável.
* 📝 Abertura de incidentes com categoria, prioridade, descrição e solicitante.
* 🧭 Sugestão de checklist inicial por categoria: rede/DNS, banco de dados ou aplicação.
* 👤 Atribuição de responsável e atualização de status.
* 🔎 Registro de verificações, resultados e evidências de troubleshooting.
* 🕒 Linha do tempo das atividades realizadas no incidente.
* ✅ Registro da resolução e geração de relatório técnico em TXT.
* 🛡️ Validação de dados e tratamento de erros, incluindo incidentes inexistentes.
* 🎨 Interface responsiva com tema escuro, detalhes em azul elétrico e identidade Ops Command.

## 🧰 Tecnologias

|Tecnologia|Utilização|
|-|-|
|Java 21|Linguagem principal do backend|
|Spring Boot 3.5|Estrutura da aplicação e API|
|Spring Web|Endpoints HTTP/REST|
|Spring Data JPA|Persistência e acesso a dados|
|Bean Validation|Validação dos dados de entrada|
|PostgreSQL 16|Banco de dados da aplicação|
|H2|Banco em memória para testes de integração|
|HTML, CSS e JavaScript|Interface web sem framework frontend|
|Maven|Gerenciamento de dependências e build|
|Docker Compose|Inicialização dos serviços em ambiente local|
|Python|Execução opcional do script de smoke test; não é necessário para executar a aplicação Java|

## 🚀 Como executar com Docker

Esta é a opção recomendada. Você precisa do **Docker Desktop** no Windows ou do **Docker Engine com Docker Compose v2** no Linux/macOS. O build da imagem compila a aplicação e executa os testes Maven durante a construção.

1. Baixe ou clone este repositório.
2. Abra um terminal na pasta `opsbridge-completo`.
3. Opcionalmente, copie `.env.example` para `.env` e ajuste as configurações locais.
4. Execute:

```bash
   docker compose up --build
   ```

5. Quando os serviços estiverem iniciados, acesse **http://localhost:8080**.
6. Para consultar o estado dos serviços e os logs:

```bash
   docker compose ps
   docker compose logs -f app
   ```

7. Para encerrar os serviços sem remover os dados persistidos:

```bash
   docker compose down
   ```

> \*\*Atenção:\*\* `docker compose down -v` também remove o volume do banco e apaga os dados locais persistidos.

### 🪟 Atalho para Windows

O projeto inclui o arquivo `iniciar-windows.bat`. Execute-o a partir da pasta raiz para verificar a disponibilidade do Docker e iniciar os serviços com build.

## ☕ Como executar com Java e Maven

Requisitos:

* JDK 21
* Maven 3.9 ou superior
* Docker Compose v2 para iniciar o PostgreSQL configurado no projeto

Na raiz do projeto, execute:

```bash
docker compose up -d postgres
mvn test
mvn spring-boot:run
```

A aplicação ficará disponível em **http://localhost:8080**. Para encerrá-la, pressione `Ctrl+C`. Para parar o banco, execute:

```bash
docker compose stop postgres
```

## 🧪 Testes automatizados

Os testes automatizados cobrem a criação de incidentes e a sugestão de checklist, além de um fluxo de integração HTTP utilizando H2. Entre os cenários previstos estão:

* Listagem e consulta de incidentes.
* Criação de incidente e checklist inicial.
* Alteração de status e atribuição de responsável.
* Inclusão de etapas de diagnóstico e atualização de evidências.
* Resolução e exportação do relatório técnico.
* Retorno HTTP 404 para recurso inexistente.
* Rejeição de dados de entrada inválidos.

Execute na raiz do projeto:

```bash
mvn test
```

O Dockerfile executa `mvn test package` antes de montar a imagem final. Se os testes falharem, o build deverá falhar em vez de gerar uma imagem final como se a validação tivesse sido concluída com sucesso.

## 🔍 Smoke test da aplicação em execução

O script `scripts/smoke-test.py` é uma ferramenta **opcional** para verificar o funcionamento da API depois que a aplicação estiver iniciada. Ele não faz parte do runtime Java e não é necessário para abrir o sistema.

Com a aplicação rodando em `http://localhost:8080`, abra outro terminal na raiz do projeto e execute:

```bash
python scripts/smoke-test.py
```

Se a aplicação estiver em outro endereço, defina `OPSBRIDGE\_URL`.

**PowerShell (Windows):**

```powershell
$env:OPSBRIDGE\_URL = "http://localhost:8080"
python .\\scripts\\smoke-test.py
```

O script cria um incidente identificado com `\[SMOKE TEST]` e mantém esse registro no banco para inspeção. Ao final, informa as etapas verificadas e retorna um código diferente de zero se uma validação falhar. Por isso, execute-o preferencialmente em um ambiente de desenvolvimento com dados de teste.

## 🔌 API REST

|Método|Endpoint|Descrição|
|-|-|-|
|`GET`|`/api/incidents`|Lista incidentes|
|`POST`|`/api/incidents`|Cria um incidente|
|`GET`|`/api/incidents/{id}`|Consulta detalhes e histórico|
|`PATCH`|`/api/incidents/{id}/status`|Atualiza o status|
|`PATCH`|`/api/incidents/{id}/assignee`|Atribui um responsável|
|`POST`|`/api/incidents/{id}/checks`|Adiciona uma etapa de diagnóstico|
|`PATCH`|`/api/incidents/{id}/checks/{checkId}`|Atualiza resultado e evidência|
|`POST`|`/api/incidents/{id}/resolve`|Registra a resolução|
|`GET`|`/api/incidents/{id}/report`|Exporta o relatório em texto|

### Valores aceitos

**Status:** `OPEN`, `IN\_PROGRESS`, `WAITING\_FOR\_USER`, `ESCALATED`, `RESOLVED`, `CLOSED`.

**Prioridades:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

**Resultados de diagnóstico:** `PENDING`, `PASSED`, `FAILED`, `INCONCLUSIVE`, `NOT\_APPLICABLE`.

## 📁 Estrutura do projeto

```text
opsbridge-completo/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── iniciar-windows.bat
├── pom.xml
├── scripts/
│   └── smoke-test.py
├── docs/
│   ├── redesign-visual.md
│   └── roteiros-diagnostico.md
└── src/
    ├── main/
    │   ├── java/com/opsbridge/
    │   │   ├── OpsBridgeApplication.java
    │   │   └── incident/          # entidades, DTOs, serviços e endpoints
    │   └── resources/
    │       ├── application.yml
    │       └── static/            # index.html, styles.css e app.js
    └── test/
        ├── java/com/opsbridge/incident/ # testes de serviço e integração HTTP
        └── resources/application-test.yml
```

## 🔐 Segurança e limitações

* Este projeto é um MVP de portfólio e **não possui login, autenticação ou autorização**.
* As configurações de `.env.example` são destinadas apenas ao desenvolvimento local; não utilize credenciais de produção.
* A configuração `ddl-auto: update` facilita a demonstração. Em um ambiente de produção, utilize migrações controladas, por exemplo com Flyway ou Liquibase.
* Utilize somente dados fictícios. Não registre senhas, tokens, dados pessoais ou informações confidenciais nas evidências.
* Não exponha a aplicação à internet antes de implementar autenticação, autorização, HTTPS, gestão segura de segredos, logging e política de backup.

## 🎯 Objetivo profissional

O OpsBridge demonstra conhecimentos e práticas relacionados a:

* Desenvolvimento backend com Java e Spring Boot.
* Criação e organização de APIs REST.
* Persistência relacional com PostgreSQL e JPA.
* Validação de dados e tratamento de exceções.
* Testes unitários e de integração.
* Troubleshooting estruturado e documentação técnica.
* Registro de evidências, histórico operacional e comunicação entre suporte N1/N2.

## 👨‍💻 Autor

**Matheus Marks**

Projeto desenvolvido com foco em diagnóstico de incidentes, troubleshooting e organização de operações de TI. O OpsBridge simula uma central para registrar incidentes, acompanhar seu andamento, atribuir responsáveis e documentar verificações técnicas.

A plataforma reúne histórico de atividades, evidências de diagnóstico e relatórios de resolução em um fluxo estruturado, contribuindo para a padronização dos procedimentos de suporte e para uma comunicação mais clara entre equipes técnicas.



\---

## ⭐ Apoie o projeto

Se este projeto foi útil para você, considere deixar uma ⭐ no repositório. 

