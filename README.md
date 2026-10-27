# FantesDay API

API REST do **FantesDay**, um app de rotina e saúde com gamificação: o usuário registra tarefas, atividades físicas, refeições e treinos de musculação, e ganha XP e níveis conforme avança.

O catálogo de alimentos e de exercícios já vem pronto — a API importa a **TACO** (Tabela Brasileira de Composição de Alimentos) e o **free-exercise-db** na primeira execução, e busca produtos industrializados no **Open Food Facts** quando necessário.

## Sumário
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Como rodar](#como-rodar)
- [Configuração](#configuração)
- [Autenticação](#autenticação)
- [Catálogos de alimentos e exercícios](#catálogos-de-alimentos-e-exercícios)
- [Rank de força, PRs e streak](#rank-de-força-prs-e-streak)
- [Endpoints](#endpoints)
- [Erros](#erros)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Limitações conhecidas](#limitações-conhecidas)
- [Créditos dos dados](#créditos-dos-dados)

## Funcionalidades
- **Cadastro e login com JWT**: todas as rotas (exceto cadastro e login) exigem token, e cada usuário só acessa os próprios dados.
- **Usuários** com senha criptografada (BCrypt), e-mail único, peso corporal e sexo.
- **Progresso**: XP total, nível (a cada 100 XP sobe um nível) e categoria. Treinos e tarefas concluídas somam XP automaticamente.
- **Metas diárias** de calorias, proteína, carboidrato e gordura, com o consumo do dia comparado à meta.
- **Rank de força no estilo [Liftoff](https://liftoffrank.com/)**: cada série estima o 1RM e gera uma nota de 1 a 1000 relativa ao peso corporal e sexo, com 9 ranks (Wood → Olympian) por exercício, por grupo muscular e geral.
- **Recordes pessoais (PRs)** detectados ao registrar a série, e histórico do 1RM estimado por exercício.
- **Streak de treinos**: dias de descanso contam, mas 3 dias seguidos sem treinar quebram a sequência.
- **Rankings** entre usuários: geral (nota de força) e semanal (volume levantado).
- **Tarefas** diárias ou recorrentes; concluir dá 10 XP e desmarcar remove.
- **Atividades** físicas (tipo, duração, calorias gastas).
- **Refeições** com nome (ex: "Café da manhã"), montadas com alimentos do catálogo; cada item calcula calorias, proteína, carboidrato e gordura pela quantidade, e a refeição tem o total nutricional.
- **Treinos** com exercícios e séries individuais (repetições e carga de cada série).
- **Catálogo de alimentos**: ~600 alimentos brasileiros da TACO + produtos industrializados do Open Food Facts (por código de barras ou nome) + alimentos cadastrados pelo usuário.
- **Catálogo de exercícios**: ~870 exercícios com grupo muscular em português, equipamento, nível, instruções e imagem.

## Tecnologias
| | |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, RestClient, Actuator) |
| Banco de dados | PostgreSQL (Supabase) |
| ORM | Hibernate (`ddl-auto=update` — o schema é criado/atualizado automaticamente) |
| Segurança | Spring Security 7 + OAuth2 Resource Server (JWT HS256), BCrypt para senhas |
| Outros | Lombok, Jackson 3, Maven Wrapper |

## Como rodar

### Pré-requisitos
- JDK 21
- Um banco PostgreSQL (o projeto usa o Supabase, mas qualquer Postgres funciona)

### Passo a passo
1. Copie o arquivo de exemplo de variáveis e preencha com os dados do seu banco:
   ```bash
   cp .env.example .env
   ```
2. Suba a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
   No Windows (PowerShell/CMD): `mvnw.cmd spring-boot:run`
3. A API fica disponível em `http://localhost:8080`.

> Na **primeira execução** em um banco vazio, a importação dos catálogos leva alguns minutos (cerca de 4 min num Supabase remoto), porque os ~1.470 registros são gravados um a um. Nas execuções seguintes ela é pulada.

## Configuração

### Variáveis de ambiente (`.env`)
| Variável | Descrição |
|---|---|
| `SUPABASE_DB_URL` | URL JDBC do banco, ex: `jdbc:postgresql://host:5432/postgres?sslmode=require` |
| `SUPABASE_DB_USER` | Usuário do banco |
| `SUPABASE_DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave para assinar os tokens, com **no mínimo 32 caracteres**. Sem ela, a API gera uma chave aleatória a cada inicialização e os tokens deixam de valer quando ela reinicia. **Obrigatória em produção.** |
| `CORS_ALLOWED_ORIGINS` | Origens do frontend liberadas no CORS, separadas por vírgula (padrão: `*`) |
| `OFF_CONTACT` | E-mail ou site enviado no `User-Agent` do Open Food Facts (a API deles pede uma forma de contato) |

Para gerar um `JWT_SECRET`:
```bash
openssl rand -base64 48
```

O `.env` é carregado automaticamente pelo `application.properties` e está no `.gitignore` — nunca faça commit dele.

### Propriedades (`application.properties`)
| Propriedade | Padrão | Descrição |
|---|---|---|
| `app.jwt.secret` | `${JWT_SECRET}` | Chave de assinatura dos tokens |
| `app.jwt.expiration` | `24h` | Validade do token |
| `app.cors.allowed-origins` | `${CORS_ALLOWED_ORIGINS:*}` | Origens liberadas no CORS |
| `app.xp.task-completed` | `10` | XP ganho ao concluir uma tarefa |
| `app.seed.enabled` | `true` | Importa TACO e free-exercise-db na inicialização (se ainda não estiverem no banco) |
| `app.openfoodfacts.base-url` | `https://world.openfoodfacts.org` | Endereço da API do Open Food Facts |
| `app.openfoodfacts.user-agent` | `FantesDay/0.0.1 (${OFF_CONTACT})` | Identificação enviada ao Open Food Facts |

## Autenticação
1. O usuário cria a conta em `POST /api/auth/register` ou entra em `POST /api/auth/login`. Os dois devolvem:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "tokenType": "Bearer",
     "expiresAt": "2026-09-27T21:26:26Z",
     "user": { "id": 6, "name": "Usuario Teste", "email": "teste@fantesday.test", "...": "..." }
   }
   ```
2. As demais requisições enviam o token no header:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```
3. O token vale 24 horas (`app.jwt.expiration`). Depois disso a API responde `401` e o usuário precisa entrar de novo.

**Regras de acesso:**
- Nas rotas com `{userId}`, ele precisa ser o id do dono do token. Senão, a resposta é `403`.
- Tarefas, refeições, treinos, séries e atividades acessados pelo próprio id também só respondem ao dono.
- O catálogo (alimentos e exercícios) é compartilhado: qualquer usuário logado lê e cadastra, mas itens importados (TACO, Open Food Facts, free-exercise-db) não podem ser alterados nem excluídos.
- Os rankings mostram nome e nota de todos os usuários.

## Catálogos de alimentos e exercícios
A API usa uma abordagem **híbrida**: os dados ficam no banco local, e fontes externas só são consultadas quando preciso.

| Fonte | Conteúdo | Como entra no banco |
|---|---|---|
| **TACO** (NEPA/Unicamp, 4ª ed.) | 597 alimentos brasileiros com calorias e macros por 100g | Importada na inicialização a partir de `src/main/resources/seed/taco.json` |
| **free-exercise-db** | 876 exercícios com músculos, equipamento, instruções e imagem | Importado na inicialização a partir de `src/main/resources/seed/exercises.json` |
| **Open Food Facts** | Produtos industrializados (código de barras) | Salvo no banco na primeira consulta por código de barras (cache) |
| Cadastro manual | Alimentos e exercícios criados pelo usuário | `POST /api/foods` e `POST /api/exercises` |

Cada registro guarda sua origem no campo `source` (`TACO`, `OPEN_FOOD_FACTS`, `FREE_EXERCISE_DB` ou `CUSTOM`).
A importação é feita pelo [CatalogSeeder](src/main/java/com/vhfantes/fantesday/seed/CatalogSeeder.java) e é idempotente: se a fonte já tiver registros, nada é importado de novo.

Os grupos musculares são traduzidos para português (`Peito`, `Costas`, `Dorsais`, `Ombros`, `Bíceps`, `Tríceps`, `Quadríceps`, `Posteriores de coxa`, `Glúteos`, `Panturrilhas`, `Abdômen`, ...). Nomes e instruções dos exercícios continuam em inglês.

## Rank de força, PRs e streak
Inspirado no [Liftoff](https://liftoffrank.com/). A fórmula exata do Liftoff não é pública; esta é uma implementação própria com a mesma ideia.

### Como a nota é calculada
1. **1RM estimado** a cada série, pela fórmula de Epley: `carga × (1 + reps / 30)`. Séries com mais de 20 repetições não estimam 1RM.
2. **Razão de força**: o melhor 1RM do exercício dividido pelo peso corporal do usuário.
   - Exercícios com halteres usam o peso de **um** halter.
   - Barra fixa, barra supinada e paralelas usam **peso corporal + carga extra**.
3. **Nota de 1 a 1000**: a razão é comparada com os padrões de força do exercício ([strength-standards.json](src/main/resources/seed/strength-standards.json)), que têm 5 níveis por sexo:

   | Nível | Iniciante | Novato | Intermediário | Avançado | Elite | 1,5× Elite |
   |---|---|---|---|---|---|---|
   | Nota | 100 | 250 | 400 | 600 | 800 | 1000 |

   Entre os níveis a nota é interpolada linearmente.
4. **Grupo muscular e geral**: as notas dos exercícios formam a nota de cada grupo muscular, e as dos grupos formam a nota geral. A média é ponderada pela própria nota, então as notas mais altas pesam mais.

### Ranks
Cada rank cobre 100 pontos, e o **LP** (lift points) é quanto o usuário já avançou dentro do rank:

| Rank | Wood | Bronze | Silver | Gold | Platinum | Diamond | Champion | Titan | Olympian |
|---|---|---|---|---|---|---|---|---|---|
| Nota | 0–99 | 100–199 | 200–299 | 300–399 | 400–499 | 500–599 | 600–699 | 700–799 | 800–1000 |

Só os **46 exercícios** da lista curada têm rank (supino, agachamento, levantamento terra, desenvolvimento, remadas, barra fixa, roscas, tríceps, etc.). Veja com `GET /api/exercises/ranked`. Para calcular o rank, o usuário precisa ter `bodyweightKg` e `sex` (`MALE`/`FEMALE`) preenchidos.

### Recordes (PRs)
Ao registrar uma série, ela é marcada como `personalRecord: true` se o 1RM estimado for maior que o de todas as outras séries do usuário naquele exercício. A resposta já traz o rank atualizado do exercício.

### Streak
- Cada dia com treino mantém o streak, e os dias de descanso entre treinos também contam.
- **3 dias seguidos sem treinar** quebram a sequência (mesma regra do Liftoff).
- `expiresOn` é o último dia para treinar sem perder o streak.

### Rankings
- **Geral** (`/api/leaderboard`): usuários ordenados pela nota de força geral.
- **Semanal** (`/api/leaderboard/weekly`): volume levantado (soma de reps × carga) na semana atual, de segunda a domingo.

O XP e o nível continuam existindo e medem a constância; o rank de força mede quão forte o usuário é.

## Endpoints
Todas as rotas começam com `/api` e exigem `Authorization: Bearer <token>`, exceto cadastro e login. Atualizações usam `PATCH` e alteram apenas os campos enviados no corpo.

### Autenticação
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Cria a conta e devolve o token (**público**) |
| `POST` | `/api/auth/login` | Troca e-mail e senha por um token (**público**) |
| `GET` | `/api/auth/me` | Usuário dono do token |

### Usuários e progresso
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users/{id}` | Busca usuário |
| `PATCH` | `/api/users/{id}` | Atualiza usuário |
| `DELETE` | `/api/users/{id}` | Exclui usuário |
| `GET` | `/api/users/{userId}/progress` | XP, nível e categoria |
| `POST` | `/api/users/{userId}/progress/xp` | Soma XP (`{"xp": 50}`; negativo subtrai) |
| `PATCH` | `/api/users/{userId}/progress/category` | Altera a categoria |

### Tarefas e atividades
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users/{userId}/tasks?date=yyyy-MM-dd` | Lista tarefas (filtro por dia opcional) |
| `POST` | `/api/users/{userId}/tasks` | Cria tarefa |
| `GET` / `PATCH` / `DELETE` | `/api/tasks/{id}` | Busca, atualiza ou exclui |
| `PATCH` | `/api/tasks/{id}/complete` | Marca como concluída e soma 10 XP |
| `PATCH` | `/api/tasks/{id}/uncomplete` | Desmarca e remove os 10 XP |
| `GET` | `/api/users/{userId}/activities` | Lista atividades |
| `POST` | `/api/users/{userId}/activities` | Cria atividade |
| `GET` / `PATCH` / `DELETE` | `/api/activities/{id}` | Busca, atualiza ou exclui |

O XP das tarefas só muda quando o estado muda: concluir duas vezes seguidas não soma em dobro. Excluir uma tarefa concluída remove o XP que ela deu.

### Alimentos
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/foods?name=arroz` | Lista ou pesquisa alimentos no banco |
| `POST` | `/api/foods` | Cadastra alimento próprio |
| `GET` / `PATCH` / `DELETE` | `/api/foods/{id}` | Busca, atualiza ou exclui |
| `GET` | `/api/foods/external?q=biscoito` | Pesquisa no Open Food Facts (não salva) |
| `POST` | `/api/foods/barcode/{codigo}` | Devolve o produto do banco ou importa do Open Food Facts |

### Refeições
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users/{userId}/meals?date=yyyy-MM-dd` | Lista refeições (filtro por dia opcional) |
| `POST` | `/api/users/{userId}/meals` | Cria refeição (`{"name": "Café da manhã", "date": "2026-09-26"}`); sem nome vira "Refeição N", e sem data vira hoje |
| `GET` / `PATCH` / `DELETE` | `/api/meals/{id}` | Busca, atualiza ou exclui (com os itens) |
| `GET` | `/api/meals/{id}/items` | Itens da refeição |
| `POST` | `/api/meals/{id}/items` | Adiciona alimento (`{"foodId": 3, "quantity": 150, "unit": "g"}`) |
| `GET` | `/api/meals/{id}/nutrition` | Total de calorias, proteína, carboidrato e gordura |
| `PATCH` / `DELETE` | `/api/meal-items/{id}` | Atualiza ou remove um item |

Os valores nutricionais são calculados como `quantidade × valor por 100g ÷ 100`, considerando a quantidade em gramas (ou ml).

### Metas
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users/{userId}/goals` | Metas diárias (campos `null` enquanto não forem definidos) |
| `PATCH` | `/api/users/{userId}/goals` | Cria ou altera metas (`{"dailyCalories": 2200, "proteinG": 150, "carbsG": 250, "fatG": 70}`) |
| `GET` | `/api/users/{userId}/nutrition/daily?date=yyyy-MM-dd` | Consumo do dia vs. meta (data padrão: hoje) |

Exemplo de `/nutrition/daily`:
```json
{
  "date": "2026-09-26",
  "consumed": { "calories": 442, "protein": 8.93, "carbs": 94.84, "fat": 1.96 },
  "goal": { "dailyCalories": 2200, "proteinG": 150.00, "carbsG": 250.00, "fatG": 70.00, "...": "..." },
  "remaining": { "calories": 1758, "protein": 141.07, "carbs": 155.16, "fat": 68.04 },
  "caloriesPercent": 20
}
```
`remaining` fica negativo quando o usuário passa da meta.

### Exercícios e treinos
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/exercises?name=press` ou `?muscleGroup=Peito` | Lista, pesquisa por nome ou filtra por grupo muscular |
| `POST` | `/api/exercises` | Cadastra exercício próprio |
| `GET` / `PATCH` / `DELETE` | `/api/exercises/{id}` | Busca, atualiza ou exclui |
| `GET` | `/api/users/{userId}/workouts` | Lista treinos |
| `POST` | `/api/users/{userId}/workouts` | Cria treino (o `xpEarned` é somado ao progresso) |
| `GET` / `PATCH` / `DELETE` | `/api/workouts/{id}` | Busca, atualiza ou exclui (ajustando o XP) |
| `GET` | `/api/workouts/{id}/exercises` | Exercícios do treino |
| `POST` | `/api/workouts/{id}/exercises` | Adiciona exercício (`{"exerciseId": 44}`) |
| `PATCH` / `DELETE` | `/api/workout-exercises/{id}` | Troca o exercício ou remove (com as séries) |
| `GET` | `/api/workout-exercises/{id}/sets` | Séries do exercício |
| `POST` | `/api/workout-exercises/{id}/sets` | Registra série (`{"reps": 8, "weightKg": 70}`); devolve 1RM estimado, PR e rank |
| `PATCH` / `DELETE` | `/api/workout-sets/{id}` | Atualiza ou remove uma série |

### Rank, streak e rankings
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/exercises/ranked` | Exercícios que têm rank de força |
| `GET` | `/api/users/{userId}/strength` | Rank geral, por grupo muscular e por exercício |
| `GET` | `/api/users/{userId}/exercises/{exerciseId}/history` | Melhor 1RM estimado e volume por dia |
| `GET` | `/api/users/{userId}/streak` | Streak atual, maior streak e prazo para treinar |
| `GET` | `/api/leaderboard?limit=50` | Ranking geral pela nota de força |
| `GET` | `/api/leaderboard/weekly?limit=50` | Ranking da semana pelo volume levantado |

### Exemplo
Criar a conta (a resposta traz o `token`):
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
{
  "status": 404,
  "title": "Not Found",
  "detail": "Usuario nao encontrado: 99",
  "instance": "/api/users/99"
}
```
| Status | Quando |
|---|---|
| `400` | Regra de negócio violada (ex: e-mail já cadastrado), JSON inválido ou parâmetro com tipo errado |
| `401` | Token ausente, inválido ou expirado, ou e-mail/senha errados no login |
| `403` | Tentativa de acessar dados de outro usuário, ou de alterar item importado do catálogo |
| `404` | Registro não encontrado |

## Testes

### Unitários
```bash
./mvnw test -Dtest=MuscleGroupTranslatorTest,OpenFoodFactsClientTest,MealServiceCalculationTest,StrengthCalculatorTest,StrengthStandardsTest,StreakServiceTest,TokenServiceTest
```
Não precisam de banco. Cobrem:
- a tradução de músculos;
- o client do Open Food Facts (com `MockRestServiceServer`);
- o cálculo nutricional;
- o cálculo do rank (1RM, nota, LP) e a consistência dos padrões de força;
- as regras do streak;
- a geração e validação do JWT (assinatura, expiração, tamanho da chave).
O `FantesdayApplicationTests` sobe o contexto completo e precisa de um banco configurado no `.env`.

### Manuais (Insomnia)
A pasta [`insomnia/`](insomnia/) tem uma coleção pronta com todas as requisições:
1. No Insomnia: **Import → From File** → `insomnia/FantesDay.insomnia.json`.
2. Rode **Autenticação → Cadastrar** (ou **Login**) e cole o `token` e o id do usuário nas variáveis `token` e `user_id` do ambiente.
3. Ajuste as outras variáveis (`meal_id`, `workout_id`, ...) com os ids retornados.

Os corpos JSON de cada requisição também estão em `insomnia/bodies/`. Veja o [README da pasta](insomnia/README.md).

## Estrutura do projeto
```
src/main/java/com/vhfantes/fantesday
├── client/        # Client HTTP do Open Food Facts e DTOs da resposta
├── config/        # Segurança (JWT, CORS), BCrypt, RestClient e log de conexão com o banco
├── controller/    # Endpoints REST
├── dto/           # Records de request/response (XxxRequestDTO / XxxResponseDTO)
├── entity/        # Entidades JPA
├── exception/     # Exceções de negócio e handler global de erros
├── repository/    # Repositórios Spring Data JPA
├── security/      # Geração do token e usuário da requisição atual
├── seed/          # Importação da TACO e do free-exercise-db
├── service/       # Regras de negócio
└── strength/      # Cálculo do rank de força (1RM, nota, ranks) e padrões de força
src/main/resources
├── application.properties
└── seed/          # taco.json, exercises.json e strength-standards.json
```

### Modelo de dados
- `User` 1—1 `Progress` e 1—1 `NutritionGoal`
- `User` 1—N `Task`, `Activity`, `Meal`, `Workout`
- `Meal` 1—N `MealItem` N—1 `Food`
- `Workout` 1—N `WorkoutExercise` N—1 `Exercise`
- `WorkoutExercise` 1—N `WorkoutSet` (cada série, com o 1RM estimado e a marca de recorde)
- `Food` e `Exercise` são catálogos globais (não pertencem a um usuário)

## Limitações conhecidas
- **Sem renovação ou revogação de token**: não há refresh token nem logout no servidor. O logout é apagar o token no frontend, e um token vazado vale até expirar (24 h).
- **Itens cadastrados no catálogo não têm dono**: um alimento ou exercício criado por um usuário pode ser editado ou excluído por qualquer outro usuário logado.
- **Excluir um usuário com dados** (tarefas, refeições, treinos ou atividades) falha, porque esses registros não são apagados junto. Apague-os antes.
- **Busca no Open Food Facts**: a busca por nome deles tem limite de requisições (~10/min) e às vezes responde 503. Nesses casos `/api/foods/external` devolve lista vazia.
- **Schema gerenciado pelo Hibernate** (`ddl-auto=update`): não há migrações versionadas (Flyway/Liquibase).
- **Unidades**: o cálculo nutricional considera a quantidade em gramas/ml.
- **Padrões de força aproximados**: os valores de `strength-standards.json` são estimativas baseadas em tabelas públicas de força, não os do Liftoff. Ajuste à vontade; o `StrengthStandardsTest` valida o arquivo.
- **Rank calculado na hora**: usa o peso corporal atual. Se o usuário mudar de peso, os ranks mudam junto. Não há temporadas (o Liftoff reinicia a cada 4 meses).
- **Ranking semanal por volume**: em exercícios com peso corporal, só a carga extra entra no volume.

## Créditos dos dados
- **TACO** — Tabela Brasileira de Composição de Alimentos, 4ª edição, NEPA/UNICAMP. Dados processados a partir do repositório [brolesi/taco](https://github.com/brolesi/taco) (MIT).
- **free-exercise-db** — [yuhonas/free-exercise-db](https://github.com/yuhonas/free-exercise-db) (Unlicense / domínio público). As imagens são carregadas direto do repositório no GitHub.
- **Open Food Facts** — [openfoodfacts.org](https://world.openfoodfacts.org), dados sob a licença [ODbL](https://opendatacommons.org/licenses/odbl/1-0/).
