# OpsBridge — Direção visual Ops Command

O frontend foi redesenhado para uma direção visual de central de operações, com:

- navegação lateral persistente e área de trabalho definida;
- cabeçalho contextual com ambiente e data;
- área de abertura com fluxo visual de intake, diagnóstico e resolução;
- indicadores operacionais com acentos de cor por significado;
- fila de incidentes com busca, filtros, prioridade e status;
- painéis auxiliares para fluxo de atendimento e guia de prioridade;
- formulários de criação, controle de incidente, checklist e linha do tempo;
- estados de foco, hover, carregamento, erro e adaptação para telas pequenas.

## Arquivos alterados

- `src/main/resources/static/index.html`: composição visual da área principal e ilustração do fluxo de trabalho.
- `src/main/resources/static/styles.css`: sistema visual, tipografia, superfícies, tabela, formulários, painéis e breakpoints responsivos.

O `app.js` foi mantido para preservar IDs, endpoints e lógica de integração existentes.

## Observação

A verificação realizada foi estática: sintaxe JavaScript, parsing do HTML, ausência de IDs duplicados e conferência dos seletores estáticos usados pelo JavaScript. O navegador integrado ao backend e o PostgreSQL precisam ser testados separadamente com a aplicação em execução.
