# FantesDay API

API REST do **FantesDay**, um app de rotina e saúde com gamificação: o usuário registra tarefas, atividades físicas, refeições, água, peso e treinos de musculação, e ganha XP, níveis e ranks de força conforme avança.

O catálogo de alimentos e de exercícios já vem pronto — a API importa a **TACO** (Tabela Brasileira de Composição de Alimentos) e o **free-exercise-db** na primeira execução, e busca produtos industrializados no **Open Food Facts** quando necessário.

## Sumário
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Como rodar](#como-rodar)
- [Configuração](#configuração)
- [Banco de dados e migrações](#banco-de-dados-e-migrações)
- [Autenticação](#autenticação)
- [Catálogos de alimentos e exercícios](#catálogos-de-alimentos-e-exercícios)
- [Tarefas recorrentes e XP](#tarefas-recorrentes-e-xp)
- [Calorias das atividades](#calorias-das-atividades)
- [Foto da refeição (Gemini)](#foto-da-refeição-gemini)
- [Rank de força, PRs e streak](#rank-de-força-prs-e-streak)
- [Rotinas e gerador de treino](#rotinas-e-gerador-de-treino)
- [Endpoints](#endpoints)
- [Erros](#erros)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Limitações conhecidas](#limitações-conhecidas)
- [Créditos dos dados](#créditos-dos-dados)

## Funcionalidades
- **Conta**:
  - cadastro e login com JWT + refresh token (que também serve para o login por Face ID);
  - troca de senha, que exige a senha atual;
  - recuperação de senha por código;
  - foto de perfil;
  - exclusão da conta com todos os dados.
- **Tela inicial em uma chamada** (`/dashboard`): perfil, XP e nível, streak, tarefas do dia, calorias e macros, saldo de calorias (meta + exercício − consumido), água, atividades, treinos da semana, TMB/IMC, peso e rank de força.
- **Metabolismo e metas**: altura, data de nascimento e nível de atividade no perfil; cálculo de TMB (Mifflin-St Jeor), gasto diário, IMC e sugestão de meta de calorias e proteína para perder, manter ou ganhar peso.
- **Tarefas avulsas e recorrentes** (ex: "beber água" de segunda a sexta), com conclusão por dia e 10 XP por conclusão.
- **Atividades físicas** com XP pela duração, e importação em lote de apps como Apple Saúde e Google Fit, sem duplicar.
  - **calorias calculadas** pelo MET da atividade × peso × horas quando o app não informa o gasto;
  - **registro por texto livre** ("joguei vôlei por 2 horas"), com o Gemini completando atividades fora do catálogo.
- **Alimentação**:
  - refeições com nome;
  - calorias e macros calculados por item, em gramas ou **ml** para líquidos (convertidos pela densidade do alimento);
  - metas diárias de calorias, macros e água, com o consumo do dia comparado à meta;
  - **foto da refeição**: o Gemini identifica os alimentos e estima a porção, as calorias e os macros, sugerindo o alimento do catálogo para registrar.
- **Água** registrada por copo/garrafa e **peso corporal** com histórico. O peso mais recente atualiza o perfil e o rank.
- **Treinos** com séries individuais, 1RM estimado e recordes pessoais (PRs).
- **Rotinas de treino** reutilizáveis, um **gerador automático** (objetivo, dias por semana, nível e equipamentos) e "iniciar treino a partir da rotina".
- **Rank de força no estilo [Liftoff](https://liftoffrank.com/)**: nota de 1 a 1000 relativa ao peso corporal e sexo, com 9 ranks (Bronze → Olympian) por exercício, por grupo muscular e geral.
- **Streak de treinos** e **rankings** entre usuários: geral pela força e semanal pelo volume.
- **Catálogo**:
  - ~600 alimentos da TACO, produtos do Open Food Facts e alimentos cadastrados pelos usuários;
  - ~1.200 exercícios **em português** (free-exercise-db traduzido + wger), com busca pelo nome em português ou em inglês;
  - listas paginadas.

## Tecnologias
| | |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, RestClient, Actuator) |
| Banco de dados | PostgreSQL (Supabase) |
| Migrações | Flyway (`src/main/resources/db/migration`) |
| ORM | Hibernate (`ddl-auto=validate`: só confere se as entidades batem com o banco) |
| Segurança | Spring Security 7 + OAuth2 Resource Server (JWT HS256), BCrypt para senhas |
| Testes | JUnit 5, Mockito, MockMvc, Spring Security Test |
| Outros | Lombok, Jackson 3, Maven Wrapper, Docker |

## Como rodar

### Com o seu banco (Supabase ou outro Postgres)
1. Copie o arquivo de exemplo de variáveis e preencha com os dados do seu banco e um `JWT_SECRET`:
   ```bash
   cp .env.example .env
   ```
2. Suba a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
   No Windows (PowerShell/CMD): `mvnw.cmd spring-boot:run`
3. A API fica disponível em `http://localhost:8080`.

### Com Docker (API + Postgres local, sem Supabase)
```bash
docker compose up --build
```
O `docker-compose.yml` sobe um Postgres 17 e a API na porta 8080. Para publicar, use o `Dockerfile` com as variáveis de ambiente da tabela abaixo.

> Na **primeira execução** em um banco vazio, o Flyway cria as tabelas e a API importa os catálogos. A importação leva alguns minutos (cerca de 4 min num Supabase remoto), porque os ~1.470 registros são gravados um a um. Nas execuções seguintes ela é pulada.

### No Render (com o Supabase como banco)
1. **New → Web Service**, runtime **Docker**, health check em `/actuator/health`.
2. Variáveis: `SUPABASE_DB_URL` (use o **Session pooler**, porta 5432, com `?sslmode=require` — a conexão direta do Supabase é IPv6 e o Render não alcança), `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`, `JWT_SECRET` e, se o frontend for web, `CORS_ALLOWED_ORIGINS`. A porta vem da variável `PORT`, que o Render define.
3. **Keep-alive:** o plano gratuito desliga o serviço após ~15 min sem requisições. A API chama a própria URL pública (`RENDER_EXTERNAL_URL`, definida pelo Render) em `/actuator/health` a cada 45 s (`app.keep-alive.interval`), o que conta como tráfego e mantém o serviço acordado. Localmente o job fica desligado.

## Configuração

### Variáveis de ambiente (`.env`)
| Variável | Descrição |
|---|---|
| `SUPABASE_DB_URL` | URL JDBC do banco, ex: `jdbc:postgresql://host:5432/postgres?sslmode=require` |
| `SUPABASE_DB_USER` | Usuário do banco |
| `SUPABASE_DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave para assinar os tokens, com **no mínimo 32 caracteres**. Sem ela, a API gera uma chave aleatória a cada inicialização e todos são deslogados quando ela reinicia. **Obrigatória em produção.** |
| `CORS_ALLOWED_ORIGINS` | Origens do frontend liberadas no CORS, separadas por vírgula (padrão: `*`) |
| `APP_TIMEZONE` | Fuso usado para "hoje" e para as horas gravadas (padrão: `America/Sao_Paulo`) |
| `DB_POOL_SIZE` | Conexões com o banco por instância da API (padrão: `5`; o pooler gratuito do Supabase aceita 15 no total) |
| `KEEP_ALIVE_URL` | URL pública da API para o keep-alive (no Render não precisa: usa `RENDER_EXTERNAL_URL`, que o Render já define) |
| `OFF_CONTACT` | E-mail ou site enviado no `User-Agent` do Open Food Facts (a API deles pede uma forma de contato) |
| `GEMINI_API_KEY` | Chave da API do Gemini ([Google AI Studio](https://aistudio.google.com/apikey)). Sem ela, a análise de foto da refeição fica desligada e as atividades em texto só usam o catálogo local. |
| `GEMINI_MODEL` | Modelo do Gemini (padrão: `gemini-3.5-flash`) |

Para gerar um `JWT_SECRET`:
```bash
openssl rand -base64 48
```

O `.env` é carregado automaticamente pelo `application.properties` e está no `.gitignore` — nunca faça commit dele.
Se você roda a API pelo VS Code (`launch.json` com `envFile`), **pare e inicie de novo** a sessão depois de mudar o `.env`: o DevTools recarrega o código, mas não as variáveis.

### Propriedades (`application.properties`)
| Propriedade | Padrão | Descrição |
|---|---|---|
| `app.jwt.expiration` | `1h` | Validade do token de acesso (JWT) |
| `app.jwt.refresh-expiration` | `30d` | Validade do refresh token |
| `app.password-reset.code-expiration` | `15m` | Validade do código de recuperação de senha |
| `app.password-reset.max-attempts` | `5` | Tentativas por código |
| `app.cors.allowed-origins` | `${CORS_ALLOWED_ORIGINS:*}` | Origens liberadas no CORS |
| `app.xp.task-completed` | `10` | XP por tarefa concluída |
| `app.xp.activity-minutes-per-xp` | `2` | Atividades: 1 XP a cada N minutos |
| `app.xp.activity-max` | `60` | Atividades: XP máximo por atividade |
| `app.pagination.default-size` / `max-size` | `20` / `100` | Paginação de alimentos e exercícios |
| `app.seed.enabled` | `true` | Importa TACO e free-exercise-db na inicialização (se ainda não estiverem no banco) |
| `app.openfoodfacts.base-url` | `https://world.openfoodfacts.org` | Endereço da API do Open Food Facts |
| `app.gemini.model` | `${GEMINI_MODEL:gemini-3.5-flash}` | Modelo do Gemini usado na foto da refeição e nas atividades em texto |
| `spring.servlet.multipart.max-file-size` | `10MB` | Tamanho máximo de upload (a foto de perfil continua limitada a 2 MB) |

## Banco de dados e migrações
O schema é versionado com **Flyway** em [`src/main/resources/db/migration`](src/main/resources/db/migration):

| Versão | O que faz |
|---|---|
| `V1__baseline.sql` | Schema base (o que o Hibernate criava antes do Flyway) |
| `V2__recurring_tasks_auth_routines_tracking.sql` | Conclusões de tarefas, XP e importação de atividades, dono dos itens do catálogo, meta de água, refresh tokens, códigos de recuperação, rotinas, água, peso e foto |
| `V3__body_metrics_liquids_exercises_pt.sql` | Altura, nascimento e nível de atividade; líquidos (ml e densidade) com os da TACO marcados; nome em inglês e idioma das instruções dos exercícios; fonte `WGER` |
| `V4__fill_missing_taco_values.sql` | Valores que a TACO não traz para leite integral, leite desnatado e sal |
| `V5__shift_utc_timestamps_to_sao_paulo.sql` | Traz os horários gravados em UTC para o fuso de São Paulo |
| `V6__activity_calories_estimate.sql` | MET da atividade e indicação de calorias estimadas pela API |

- **Banco novo:** o Flyway roda V1 e V2.
- **Banco que já existia antes do Flyway:** com `baseline-on-migrate`, a V1 é marcada como aplicada e só as seguintes rodam.
- **O Hibernate não altera mais o schema** (`ddl-auto=validate`): se uma entidade não bater com o banco, a API não sobe.
- **Para mudar o schema**, crie um arquivo novo, por exemplo `V3__descricao.sql`, sem editar os já aplicados.

## Autenticação
1. O usuário cria a conta em `POST /api/auth/register` ou entra em `POST /api/auth/login`. Os dois devolvem:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "tokenType": "Bearer",
     "expiresAt": "2026-10-27T03:28:57Z",
     "refreshToken": "q8Xc...",
     "refreshTokenExpiresAt": "2026-11-26T02:28:57Z",
     "user": { "id": 6, "name": "Usuario Teste", "photoUrl": null, "...": "..." }
   }
   ```
2. As demais requisições enviam o token no header `Authorization: Bearer <token>`.
3. O `token` vale **1 hora**. Quando a API responder `401`, o app chama `POST /api/auth/refresh` com o `refreshToken` e recebe um par novo, sem pedir a senha.
4. **Rotação:** cada refresh token só pode ser usado uma vez. Se um token já usado aparecer de novo (sinal de roubo), todas as sessões do usuário são encerradas.
5. **Logout:** `POST /api/auth/logout` com o `refreshToken` invalida a sessão daquele aparelho.

**Face ID:** a biometria acontece no celular. O app guarda o `refreshToken` no armazenamento seguro (Keychain/Keystore) protegido por biometria e, depois do Face ID, chama `/api/auth/refresh`. O campo `faceIdEnabled` do usuário só registra a preferência.

**Senha:**
- **Troca:** `POST /api/auth/change-password` exige a senha atual. Ela encerra as outras sessões e devolve tokens novos. O `PATCH /api/users/{id}` não aceita mais `password`.
- **Esqueci a senha:** `POST /api/auth/forgot-password` gera um código de 6 dígitos (válido por 15 min, 5 tentativas) e `POST /api/auth/reset-password` define a senha nova.
  > ⚠️ **Por enquanto o código não é enviado por e-mail: ele aparece no log da API** (`Codigo de recuperacao de senha para v***@exemplo.com: 123456`). Para produção, é preciso integrar um serviço de e-mail em `PasswordResetService.requestCode`.
- A senha precisa ter de 8 a 72 caracteres.

**Regras de acesso:**
- Nas rotas com `{userId}`, ele precisa ser o id do dono do token; senão a resposta é `403`. Tarefas, refeições, treinos, séries, rotinas, atividades, água e peso acessados pelo próprio id também só respondem ao dono.
- O catálogo é compartilhado: qualquer usuário logado lê e cadastra. Itens importados (TACO, Open Food Facts, free-exercise-db) não podem ser alterados, e itens cadastrados só podem ser alterados ou excluídos por quem os criou.
- A foto de perfil e os rankings (nome e nota) são visíveis para qualquer usuário logado.

## Catálogos de alimentos e exercícios
A API usa uma abordagem **híbrida**: os dados ficam no banco local, e fontes externas só são consultadas quando preciso.

| Fonte | Conteúdo | Como entra no banco |
|---|---|---|
| **TACO** (NEPA/Unicamp, 4ª ed.) | 597 alimentos brasileiros com calorias e macros por 100g | Importada na inicialização a partir de `seed/taco.json` |
| **free-exercise-db** (traduzido) + **wger** | 1.213 exercícios em português: 876 do free-exercise-db (nome traduzido; descrição em português quando existe no wger) e 337 do wger | Sincronizado na inicialização a partir de `seed/exercises-pt.json` |
| **Open Food Facts** | Produtos industrializados (código de barras) | Salvo no banco na primeira consulta por código de barras (cache) |
| Cadastro manual | Alimentos e exercícios criados pelo usuário | `POST /api/foods` e `POST /api/exercises` (com `createdById`) |

As listas `GET /api/foods` e `GET /api/exercises` são **paginadas** (`?page=0&size=20`, máximo 100):
```json
{ "content": [ ... ], "page": 0, "size": 20, "totalElements": 597, "totalPages": 30, "last": false }
```
Um alimento ou exercício em uso (em refeições, treinos ou rotinas) não pode ser excluído.

**Líquidos em ml:** alimentos com `liquid: true` (sucos, leites, óleos, bebidas, mel...) têm `defaultUnit: "ml"` e `densityGPerMl`. Ao adicionar um item com `"unit": "ml"`, a quantidade vira gramas pela densidade antes do cálculo (ex.: 15 ml de azeite × 0,91 = 13,65 g). As unidades aceitas são `g` e `ml`; qualquer alimento aceita `ml` (densidade 1 quando não informada). Produtos do Open Food Facts vendidos em ml/L são marcados como líquidos automaticamente.

**Exercícios:** `name` vem em português e `nameEn` traz o nome original; a busca (`?name=`) procura nos dois. `instructionsLanguage` indica se as instruções estão em português (`pt`, 416 exercícios) ou ainda em inglês (`en`). Equipamentos e categorias estão em português (`Barra`, `Halteres`, `Polia`, `Máquina`, `Peso corporal`...; `Força`, `Alongamento`, `Cardio`...). Bancos que já tinham o catálogo antigo são atualizados na inicialização sem perder treinos e histórico.

> A **TBCA** (USP, versão 7.3 de 2025) é mais nova que a TACO, mas os termos de uso proíbem reproduzir a base e exigem autorização dos coordenadores para uso comercial; por isso ela não foi incluída.

## Tarefas recorrentes e XP
- **Tarefa avulsa** (sem `weekDays`): com `date`, vale naquele dia. Sem data, aparece todo dia até ser concluída.
- **Tarefa recorrente** (`"weekDays": "SEG,QUA,SEX"`): repete nesses dias da semana, a partir de `date` (se houver). Aceita SEG, TER, QUA, QUI, SEX, SAB, DOM ou MON…SUN.
- **Conclusão por dia:** cada conclusão é registrada por dia. `GET /api/users/{id}/tasks/today` (ou `?date=`) traz as tarefas do dia com `completed` daquele dia.
- **Concluir e desmarcar:** `PATCH /api/tasks/{id}/complete` (ou `?date=` para um dia passado) dá 10 XP, e `/uncomplete` remove. Repetir a mesma ação não muda o XP, e datas futuras são recusadas.
- **Excluir:** excluir uma tarefa avulsa concluída desconta o XP. Numa recorrente, o XP dos dias já feitos fica.

**XP de outras fontes:** treinos dão o `xpEarned` informado, e atividades dão 1 XP a cada 2 minutos (máximo 60 por atividade). Editar a duração ou excluir a atividade ajusta o XP.

## Calorias das atividades
- **Fórmula do MET:** `kcal = MET × peso (kg) × horas`, com o MET do [Compendium of Physical Activities](https://pacompendium.com/). Ex: vôlei recreativo (MET 4,0), 70 kg, 2 h → 560 kcal. Sem peso no perfil, usa 70 kg.
- **Quando calcula:** se a atividade chega sem `caloriesBurned` (criação ou importação). Calorias informadas pelo app (ex: Apple Saúde) são mantidas.
- **Reconhecimento:** `activityType` é comparado com ~40 atividades e seus sinônimos em português (`GET /api/activities/catalog`), sem diferenciar acento ou maiúscula. Atividade fora do catálogo fica sem estimativa.
- **Resposta:** `met` e `caloriesEstimated: true` indicam que o valor foi calculado. Ao mudar o tipo ou a duração, calorias estimadas são recalculadas.
- **Texto livre** (`POST /api/users/{userId}/activities/text` com `{"text": "joguei vôlei por 2 horas"}`):
  - a atividade e a duração são lidas localmente ("2 horas", "1h30", "uma hora e meia", "45 min", "meia hora"...);
  - se faltar algo (atividade fora do catálogo ou duração), o **Gemini** completa e estima o MET;
  - sem duração no texto nem na resposta da IA, a API pede para informar.

## Foto da refeição (Gemini)
`POST /api/meals/analyze-photo` (multipart: `file` com JPG, PNG, WebP ou HEIC até 10 MB, e `description` opcional, ex: "arroz integral e frango"):
- O Gemini identifica cada alimento e estima a porção em gramas, as calorias e os macros (com base na TACO), com `confidence` ALTA, MEDIA ou BAIXA.
- `totals` é a soma dos itens. Nada é salvo.
- Cada item traz `foodId`/`foodName` do alimento mais parecido no catálogo, quando houver. Para registrar, use `POST /api/meals/{mealId}/items` com `{"foodId": foodId, "quantity": portionGrams, "unit": "g"}`.
- É uma **estimativa**: porções vistas por foto podem errar bastante (óleo, molhos e recheios não aparecem). Mostre os valores para o usuário conferir antes de salvar.

## Rank de força, PRs e streak
Inspirado no [Liftoff](https://liftoffrank.com/). A fórmula exata do Liftoff não é pública; esta é uma implementação própria com a mesma ideia.

1. **1RM estimado** a cada série pela fórmula de Epley: `carga × (1 + reps / 30)`. Séries com mais de 20 repetições não estimam 1RM.
2. **Razão de força**: o melhor 1RM do exercício dividido pelo peso corporal.
   - Halteres usam o peso de **um** halter.
   - Barra fixa, barra supinada e paralelas usam **peso corporal + carga extra**.
3. **Nota de 1 a 1000** comparando a razão com os padrões do exercício ([strength-standards.json](src/main/resources/seed/strength-standards.json)):

   | Nível | Iniciante | Novato | Intermediário | Avançado | Elite | 1,5× Elite |
   |---|---|---|---|---|---|---|
   | Nota | 100 | 250 | 400 | 600 | 800 | 1000 |

4. **Grupo muscular e geral**: as notas dos exercícios formam a de cada grupo muscular, e as dos grupos formam a geral, numa média ponderada pela própria nota.

| Rank | Bronze | Silver | Gold | Platinum | Diamond | Master | Grandmaster | Challenger | Olympian |
|---|---|---|---|---|---|---|---|---|---|
| Nota | 0–99 | 100–199 | 200–299 | 300–399 | 400–499 | 500–599 | 600–699 | 700–799 | 800–1000 |

Outras regras:
- **Exercícios com rank:** só os **46 exercícios** da lista curada (`GET /api/exercises/ranked`).
- **Dados necessários:** o usuário precisa ter `bodyweightKg` e `sex` (`MALE`/`FEMALE`).
- **Recorde (PR):** uma série é `personalRecord: true` quando bate o melhor 1RM do usuário naquele exercício.
- **Streak:** dias de descanso contam, mas **3 dias seguidos sem treinar** quebram a sequência. `expiresOn` é o último dia para treinar sem perder.
- **Rankings:** o geral é pela nota de força; o semanal, pelo volume (reps × carga) de segunda a domingo.

## Rotinas e gerador de treino
- **Rotina:** um modelo de treino ("Treino A - Peito e Tríceps") com exercícios em ordem e metas de séries, repetições e descanso.
- **Iniciar treino:** `POST /api/routines/{id}/start` cria um treino com os exercícios da rotina, e as séries são registradas como em qualquer treino.
- **Gerador:** `POST /api/users/{id}/routines/generate`:
  ```json
  { "goal": "HYPERTROPHY", "daysPerWeek": 3, "level": "INTERMEDIATE", "equipment": ["BARBELL", "DUMBBELL", "MACHINE", "CABLE", "BODYWEIGHT"], "save": true }
  ```
  - **Divisão:** 1–2 dias, Full Body; 3 dias, Full Body (iniciante) ou Push/Pull/Legs; 4 dias, Superiores/Inferiores; 5 dias, PPL + Superiores/Inferiores; 6 dias, PPL duas vezes.
  - **Exercícios por dia:** 4 (iniciante), 5 (intermediário) ou 6 (avançado). Os exercícios com rank têm preferência, completados pelo resto do catálogo, filtrados pelos equipamentos. Dias repetidos variam os exercícios.
  - **Séries e repetições:** força, 4×3-6 com 180 s; hipertrofia, 3×8-12 com 90 s; resistência, 3×15-20 com 45 s. Iniciante faz −1 série e avançado +1.
  - `save: false` só devolve a sugestão, sem salvar.

## Endpoints
Todas as rotas começam com `/api` e exigem `Authorization: Bearer <token>`, exceto as marcadas como **público**. Atualizações usam `PATCH` e alteram só os campos enviados.

### Autenticação
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Cria a conta e devolve os tokens (**público**) |
| `POST` | `/api/auth/login` | E-mail e senha → tokens (**público**) |
| `POST` | `/api/auth/refresh` | `{"refreshToken"}` → tokens novos (**público**) |
| `POST` | `/api/auth/logout` | `{"refreshToken"}` → invalida a sessão (**público**) |
| `POST` | `/api/auth/forgot-password` | `{"email"}` → gera o código de recuperação (**público**) |
| `POST` | `/api/auth/reset-password` | `{"email", "code", "newPassword"}` (**público**) |
| `POST` | `/api/auth/change-password` | `{"currentPassword", "newPassword"}` → tokens novos |
| `GET` | `/api/auth/me` | Usuário dono do token |

### Usuário, perfil e tela inicial
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users/{id}/dashboard?date=` | Tudo da tela inicial em uma chamada (veja [Funcionalidades](#funcionalidades)) |
| `GET` | `/api/users/{id}/body-metrics` | TMB, gasto diário (TDEE), IMC, idade e metas sugeridas; `missingFields` diz o que falta no perfil |
| `POST` | `/api/users/{id}/goals/apply-suggestion` | Grava a meta sugerida (`{"goal": "LOSE" \| "MAINTAIN" \| "GAIN"}`) |
| `GET` / `PATCH` | `/api/users/{id}` | Busca ou atualiza o perfil (o peso entra no histórico) |
| `DELETE` | `/api/users/{id}` | Exclui a conta e **todos** os dados |
| `PUT` | `/api/users/{id}/photo` | Envia a foto (multipart, campo `file`, JPG/PNG/WebP até 2 MB) |
| `GET` / `DELETE` | `/api/users/{id}/photo` | Mostra ou remove a foto (o `photoUrl` vem no usuário) |
| `GET` | `/api/users/{userId}/progress` | XP, nível e categoria |
| `POST` | `/api/users/{userId}/progress/xp` | Soma XP (`{"xp": 50}`; negativo subtrai) |
| `PATCH` | `/api/users/{userId}/progress/category` | Altera a categoria |

### Tarefas e atividades
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users/{userId}/tasks` | Todas as tarefas |
| `GET` | `/api/users/{userId}/tasks/today` ou `?date=yyyy-MM-dd` | Tarefas do dia, com `completed` daquele dia |
| `POST` | `/api/users/{userId}/tasks` | Cria tarefa (`weekDays` opcional, ex: `"SEG,QUA,SEX"`) |
| `GET` / `PATCH` / `DELETE` | `/api/tasks/{id}` | Busca, atualiza ou exclui |
| `PATCH` | `/api/tasks/{id}/complete?date=` | Conclui no dia (padrão: hoje) e dá 10 XP |
| `PATCH` | `/api/tasks/{id}/uncomplete?date=` | Desmarca e remove o XP |
| `GET` / `POST` | `/api/users/{userId}/activities` | Lista ou cria atividade (+XP pela duração) |
| `POST` | `/api/users/{userId}/activities/text` | Cria a partir de texto livre (`{"text": "joguei vôlei por 2 horas"}`), com calorias pelo MET |
| `GET` | `/api/activities/catalog` | Atividades reconhecidas e o MET de cada uma |
| `POST` | `/api/users/{userId}/activities/import` | Importa um lote (`source` + `activities[]` com `externalId`) |
| `GET` / `PATCH` / `DELETE` | `/api/activities/{id}` | Busca, atualiza ou exclui (ajustando o XP) |

### Alimentação, metas, água e peso
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/foods?name=&page=&size=` | Lista ou pesquisa alimentos (paginado) |
| `POST` | `/api/foods` | Cadastra alimento próprio |
| `GET` / `PATCH` / `DELETE` | `/api/foods/{id}` | Busca, atualiza ou exclui (só quem cadastrou) |
| `GET` | `/api/foods/external?q=biscoito` | Pesquisa no Open Food Facts (não salva) |
| `POST` | `/api/foods/barcode/{codigo}` | Devolve o produto do banco ou importa do Open Food Facts |
| `GET` / `POST` | `/api/users/{userId}/meals?date=` | Lista ou cria refeição (sem nome vira "Refeição N") |
| `GET` / `PATCH` / `DELETE` | `/api/meals/{id}` | Busca, atualiza ou exclui (com os itens) |
| `GET` / `POST` | `/api/meals/{id}/items` | Itens da refeição / adiciona (`{"foodId": 3, "quantity": 150, "unit": "g"}` ou `"unit": "ml"`) |
| `GET` | `/api/meals/{id}/nutrition` | Total da refeição |
| `POST` | `/api/meals/analyze-photo` | Estima alimentos, porções e calorias pela foto (multipart `file` + `description` opcional) |
| `PATCH` / `DELETE` | `/api/meal-items/{id}` | Atualiza ou remove um item |
| `GET` / `PATCH` | `/api/users/{userId}/goals` | Metas (`dailyCalories`, `proteinG`, `carbsG`, `fatG`, `dailyWaterMl`) |
| `GET` | `/api/users/{userId}/nutrition/daily?date=` | Consumo do dia vs. meta |
| `POST` | `/api/users/{userId}/water` | Registra água (`{"amountMl": 250}`) e devolve o total do dia |
| `GET` | `/api/users/{userId}/water?date=` | Água do dia vs. meta |
| `DELETE` | `/api/water/{id}` | Remove um registro de água |
| `POST` | `/api/users/{userId}/weights` | Registra o peso do dia (`{"weightKg": 79.5, "date": opcional}`) |
| `GET` | `/api/users/{userId}/weights?from=&to=` | Histórico (padrão: 90 dias) e variação |
| `DELETE` | `/api/weights/{id}` | Remove um registro de peso |

### Exercícios, rotinas e treinos
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/exercises?name=&muscleGroup=&page=&size=` | Lista ou filtra exercícios (paginado; nome em português ou inglês) |
| `GET` | `/api/exercises/ranked` | Exercícios que têm rank de força |
| `POST` | `/api/exercises` | Cadastra exercício próprio |
| `GET` / `PATCH` / `DELETE` | `/api/exercises/{id}` | Busca, atualiza ou exclui (só quem cadastrou) |
| `GET` / `POST` | `/api/users/{userId}/routines` | Lista ou cria rotina (com `exercises[]` opcional) |
| `POST` | `/api/users/{userId}/routines/generate` | Gera rotinas automaticamente |
| `GET` / `PATCH` / `DELETE` | `/api/routines/{id}` | Busca, renomeia ou exclui |
| `POST` | `/api/routines/{id}/exercises` | Adiciona exercício (`exerciseId`, `targetSets`, `targetReps`, `restSeconds`, `position`) |
| `PATCH` / `DELETE` | `/api/routine-exercises/{id}` | Altera metas/posição ou remove |
| `POST` | `/api/routines/{id}/start` | Cria um treino a partir da rotina |
| `GET` / `POST` | `/api/users/{userId}/workouts` | Lista ou cria treino |
| `GET` / `PATCH` / `DELETE` | `/api/workouts/{id}` | Busca, atualiza ou exclui (ajustando o XP) |
| `GET` / `POST` | `/api/workouts/{id}/exercises` | Exercícios do treino / adiciona (`{"exerciseId": 44}`) |
| `PATCH` / `DELETE` | `/api/workout-exercises/{id}` | Troca o exercício ou remove (com as séries) |
| `GET` / `POST` | `/api/workout-exercises/{id}/sets` | Séries / registra série (devolve 1RM, PR e rank) |
| `PATCH` / `DELETE` | `/api/workout-sets/{id}` | Atualiza ou remove uma série |

### Rank, streak e rankings
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users/{userId}/strength` | Rank geral, por grupo muscular e por exercício |
| `GET` | `/api/users/{userId}/exercises/{exerciseId}/history` | Melhor 1RM estimado e volume por dia |
| `GET` | `/api/users/{userId}/streak` | Streak atual, maior streak e prazo para treinar |
| `GET` | `/api/leaderboard?limit=50` | Ranking geral pela nota de força |
| `GET` | `/api/leaderboard/weekly?limit=50` | Ranking da semana pelo volume levantado |

### Exemplo
Criar a conta (a resposta traz o `token` e o `refreshToken`):
```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"name":"Usuario Teste","email":"teste@fantesday.test","password":"senha123","bodyweightKg":80,"sex":"MALE"}'
```
Usar o token:
```bash
curl http://localhost:8080/api/auth/me -H "Authorization: Bearer SEU_TOKEN"
```

## Erros
Os erros seguem o formato **ProblemDetail** (RFC 9457):
```json
{ "status": 404, "title": "Not Found", "detail": "Usuario nao encontrado: 99", "instance": "/api/users/99" }
```
| Status | Quando |
|---|---|
| `400` | Regra de negócio violada (ex: e-mail já cadastrado, item em uso), JSON inválido ou parâmetro com tipo errado |
| `401` | Token ausente, inválido ou expirado; e-mail/senha errados; refresh token inválido |
| `403` | Dados de outro usuário, ou alteração de item do catálogo que não é seu |
| `404` | Registro não encontrado |
| `413` | Arquivo maior que o limite de upload |

## Testes

### Automatizados
```bash
./mvnw test -Dtest='!FantesdayApplicationTests'
```
São 103 testes que não precisam de banco. Cobrem:
- **Cálculos:** rank de força (1RM, nota, LP), padrões de força, streak, tarefas recorrentes (dias da semana e agenda), gerador de rotinas, XP de atividades e cálculo nutricional.
- **Services com Mockito:** tarefas (XP por conclusão, sem duplicar, datas futuras), refresh token (rotação, reuso, expiração) e recuperação de senha (tentativas, expiração).
- **Segurança e erros:** JWT (assinatura, expiração, tamanho da chave) e camada web com MockMvc (401 sem token, 403 para outro usuário, 200 para o dono).
- **Atividades:** calorias pelo MET, reconhecimento da atividade e da duração no texto e recálculo ao editar.
- **IA:** client do Gemini (resposta em JSON, nova tentativa quando o modelo está sobrecarregado) e análise de foto com o Gemini simulado.
- **Outros:** client do Open Food Facts e validação do tipo de imagem pelo conteúdo.

O `FantesdayApplicationTests` sobe o contexto completo e precisa de um banco configurado no `.env`.

### Manuais (Insomnia)
A pasta [`insomnia/`](insomnia/) tem uma coleção com todas as requisições. Veja o [README da pasta](insomnia/README.md).

## Estrutura do projeto
```
src/main/java/com/vhfantes/fantesday
├── activity/      # Calorias pelo MET, atividades conhecidas e leitura da duração em texto
├── client/        # Clients HTTP do Open Food Facts e do Gemini, e DTOs das respostas
├── config/        # Segurança (JWT, CORS), BCrypt, RestClient, paginação e log de conexão
├── controller/    # Endpoints REST
├── dto/           # Records de request/response
├── entity/        # Entidades JPA
├── exception/     # Exceções e handler global de erros
├── repository/    # Repositórios Spring Data JPA
├── routine/       # Gerador de rotinas (regras, objetivo, nível, equipamentos)
├── security/      # JWT, refresh token, política de senha e usuário da requisição
├── seed/          # Importação da TACO e do free-exercise-db
├── service/       # Regras de negócio
├── strength/      # Rank de força (1RM, nota, ranks) e padrões de força
├── task/          # Agenda das tarefas recorrentes (dias da semana)
├── body/          # TMB, gasto diário e IMC
├── food/          # Unidades (g/ml) e conversão por densidade
└── text/          # Normalização de texto (sem acento, minúsculo)
src/main/resources
├── application.properties
├── db/migration/  # Migrações do Flyway
└── seed/          # taco.json, exercises-pt.json e strength-standards.json
```

### Modelo de dados
- `User` 1—1 `Progress`, `NutritionGoal` e `UserPhoto`
- `User` 1—N `Task`, `Activity`, `Meal`, `Workout`, `WorkoutRoutine`, `WaterEntry`, `WeightEntry`, `RefreshToken`, `PasswordResetCode`
- `Task` 1—N `TaskCompletion` (uma por dia)
- `Meal` 1—N `MealItem` N—1 `Food`
- `Workout` 1—N `WorkoutExercise` 1—N `WorkoutSet`; `WorkoutExercise` N—1 `Exercise`
- `WorkoutRoutine` 1—N `RoutineExercise` N—1 `Exercise`
- `Food` e `Exercise` são catálogos compartilhados (`createdBy` só nos cadastrados manualmente)

## Limitações conhecidas
- **Recuperação de senha sem e-mail**: o código aparece no log da API. Antes de publicar, integre um serviço de e-mail.
- **Importação de saúde**: a API recebe as atividades, mas a leitura do Apple Saúde/Google Fit é feita pelo app no celular.
- **Busca no Open Food Facts**: a busca por nome deles tem limite de requisições (~10/min) e às vezes responde 503. Nesses casos `/api/foods/external` devolve lista vazia.
- **Estimativas de IA**: a foto da refeição e as atividades fora do catálogo dependem do Gemini (alguns segundos por chamada, sujeito à cota da chave). As porções são aproximadas.
- **Calorias brutas**: o MET estima o gasto total no período, incluindo o que o corpo gastaria em repouso. Apps como o Apple Saúde informam só a energia ativa, que é um pouco menor.
- **Unidades**: só gramas e ml; medidas caseiras (colher, xícara, unidade) ainda não são aceitas.
- **Padrões de força aproximados**: estimativas baseadas em tabelas públicas de força, não os do Liftoff. Ajuste à vontade; o `StrengthStandardsTest` valida o arquivo.
- **Rank calculado na hora** com o peso atual; não há temporadas (o Liftoff reinicia a cada 4 meses).
- **Ranking semanal por volume**: em exercícios com peso corporal, só a carga extra entra no volume.
- **Instruções em inglês**: 797 exercícios do free-exercise-db têm nome em português, mas as instruções ainda em inglês (`instructionsLanguage: "en"`).
- **Keep-alive no Render**: o job só funciona enquanto a API está de pé; se o serviço chegar a dormir (ex.: deploy falhou), ele não se acorda sozinho. O plano gratuito tem 750 h/mês, suficiente para um serviço ligado o mês todo.

## Créditos dos dados
- **TACO** — Tabela Brasileira de Composição de Alimentos, 4ª edição, NEPA/UNICAMP. Dados processados a partir do repositório [brolesi/taco](https://github.com/brolesi/taco) (MIT).
- **free-exercise-db** — [yuhonas/free-exercise-db](https://github.com/yuhonas/free-exercise-db) (Unlicense / domínio público). As imagens são carregadas direto do repositório no GitHub.
- **wger** — [wger.de](https://wger.de), exercícios e descrições em português sob a licença [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) (alguns CC BY-SA 3.0 e CC0). Imagens carregadas do servidor do wger.
- **USDA FoodData Central** — [fdc.nal.usda.gov](https://fdc.nal.usda.gov) (domínio público): valores de leite integral e desnatado que faltam na TACO.
- **Open Food Facts** — [openfoodfacts.org](https://world.openfoodfacts.org), dados sob a licença [ODbL](https://opendatacommons.org/licenses/odbl/1-0/).
