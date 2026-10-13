# Roteiros de diagnóstico — exemplos de laboratório

Use estes roteiros em ambientes próprios ou autorizados. Não execute testes contra sistemas de terceiros sem permissão.

## 1. Falha de DNS

1. Confirmar o nome do sistema e o erro informado pelo usuário.
2. Verificar a resolução do nome (`nslookup nome-do-host`).
3. Comparar o endereço retornado com o endereço esperado do ambiente de laboratório.
4. Se necessário, testar conectividade ao host aprovado.
5. Registrar comando, horário, resultado e conclusão sem incluir dados sensíveis.

## 2. Aplicação indisponível

1. Registrar URL, horário de início e quantidade de usuários afetados.
2. Verificar se o host responde na rede autorizada.
3. Validar a porta do serviço com ferramenta apropriada ao sistema operacional.
4. Conferir logs de aplicação no intervalo do incidente.
5. Verificar dependências, como banco de dados e serviços internos.
6. Escalonar com evidências e testes já realizados.

## 3. Erro de conexão com banco

1. Confirmar ambiente, nome do serviço e horário do erro.
2. Verificar disponibilidade do serviço de banco no ambiente autorizado.
3. Validar host, porta e conectividade.
4. Confirmar que a aplicação aponta para o ambiente correto.
5. Testar permissões com uma conta autorizada; nunca incluir senha no chamado.
6. Registrar a mensagem de erro sanitizada e encaminhar ao DBA se necessário.

## Boas práticas de evidência

- Anotar o que foi testado, resultado esperado e resultado observado.
- Não registrar senhas, tokens, strings de conexão completas ou dados pessoais.
- Diferenciar fato observado de hipótese.
- Evitar ações destrutivas sem aprovação e plano de retorno.
