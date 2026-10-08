# EVFINDER

Backend de um app para encontrar estações de recarga de carros elétricos e recompensar check-ins com tokens (Charge-to-Earn).
O código fica em `EVFINDER.back/`: Spring Boot 4.1, Java 21, PostgreSQL 17 (Docker), API do Open Charge Map e Solana (simulada).

**Idioma:** tudo em português: conversa com o usuário, comentários, mensagens de erro da API, nomes de testes, commits, PRs e README.

## Comandos

Rode a partir de `EVFINDER.back/`. No Windows, use `.\mvnw.cmd` no lugar de `./mvnw`.

| Comando | Para quê |
|---|---|
| `docker compose up -d` | Sobe o PostgreSQL |
| `./mvnw spring-boot:run` | Roda a API em `http://localhost:8080` |
| `./mvnw verify` | Compila e roda os testes (não precisam de banco nem de chave). O CI roda o mesmo comando |

O arquivo `.env` (fora do git, modelo em `.env.example`) precisa de:
- `OCM_API_KEY`: obrigatória. Sem ela, a aplicação não inicia.
- `DB_PORT`: opcional, padrão 5432. Use outra porta se já houver um PostgreSQL instalado ocupando a 5432.

## Arquitetura e padrões

- **Camadas:** `controller → service → repository`. As regras de negócio ficam nos services, não nos controllers.
- **DTOs:** são `record` com Bean Validation. As mensagens são curtas e em português, como `"é obrigatório"` e `"deve estar entre -90 e 90"`.
- **Erros:** o `GlobalExceptionHandler` devolve sempre `{status, error, message, timestamp}`.
  - `ResourceNotFoundException` vira 404.
  - Violação de regra de negócio vira `ResponseStatusException` com o status adequado: 400, 409 ou 422.
- **Configuração de regras:** fica em `application.properties` e é lida por records `@ConfigurationProperties`. Exemplo: `RewardProperties`, com as propriedades `rewards.*`.
- **Banco:** usa `ddl-auto=update`, sem migrations. O Hibernate cria e atualiza as tabelas.
- **Check-in** (`CheckInService`):
  - A estação precisa existir no banco local.
  - O usuário precisa estar a no máximo 200 m da estação (Haversine).
  - A mesma carteira faz 1 check-in por estação a cada 24 h.
  - A estação fica travada com `findLockedById` (`SELECT ... FOR UPDATE`) para evitar check-ins duplicados simultâneos.
  - O `TokenRewardService` ainda simula a Solana e devolve um hash falso.
- **Testes:**
  - Controllers são testados com `MockMvcBuilders.standaloneSetup` e mocks.
  - Services são testados com Mockito puro.
  - Os nomes dos testes ficam em português, como `retorna400QuandoFaltamCamposObrigatorios`.
  - Não há testes com banco (nem H2 nem Testcontainers). Queries novas devem ser validadas de ponta a ponta com o PostgreSQL do Docker, e os dados de teste devem ser apagados depois.

## Fluxo de trabalho

- **Branches:** uma por mudança, no formato `tipo/descricao-em-portugues`, como `feat/check-in-recompensas`.
- **Commits:** no formato `TIPO - Descrição no presente`. Os tipos são FEAT, FIX, CHORE, DOCS e CI. O corpo traz bullets explicando o porquê da mudança.
- **PRs:** em português, com as seções Resumo, Mudanças, Como testar e Fora deste PR / Limitações. Avise no PR quando o contrato da API mudar para o frontend.
- **Merge:** use merge commit, nunca squash: `gh pr merge <n> --merge --delete-branch`.
- **README:** atualize junto sempre que mudar a API, a configuração ou a forma de rodar o projeto.
- **Processo java no Windows:** ao rodar a aplicação em background, o processo java continua vivo depois que o `mvnw` é parado. Encerre-o pelo processo que estiver na porta 8080.

## Estado atual

Atualize esta seção ao concluir uma etapa.

**Já feito** (PRs #1 a #4, outubro de 2026):
- Chave da API no `.env`.
- Tratamento de erros com os status HTTP corretos.
- Bean Validation nos DTOs.
- CI no GitHub Actions.
- `docker-compose.yml`.
- Check-in com regras de negócio e histórico.

**Próximos passos candidatos**, em ordem de prioridade sugerida:
1. **Autenticação da carteira.** Hoje qualquer pessoa pode informar qualquer endereço e acumular tokens. A ideia é o usuário provar que é dono da carteira assinando uma mensagem.
2. **Check-in em estações do Open Charge Map.** Elas aparecem nas recomendações, mas o `OcmStationDto` ainda não expõe o ID da estação.
3. **Emissão real de tokens na Solana.** Ela deve sair da transação do banco: salvar o check-in como pendente e emitir depois.

**Limitações conhecidas:**
- A posição do usuário no check-in vem do cliente e pode ser falsificada.
- O CORS está aberto para qualquer origem (`WebConfig`).
