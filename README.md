# FantesDay API

API REST do **FantesDay**, um app de rotina e saúde com gamificação: o usuário registra tarefas, atividades físicas, refeições e treinos de musculação, e ganha XP e níveis conforme avança.

O catálogo de alimentos e de exercícios já vem pronto — a API importa a **TACO** (Tabela Brasileira de Composição de Alimentos) e o **free-exercise-db** na primeira execução, e busca produtos industrializados no **Open Food Facts** quando necessário.

## Sumário
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Como rodar](#como-rodar)
- [Configuração](#configuração)
- [Catálogos de alimentos e exercícios](#catálogos-de-alimentos-e-exercícios)
- [Endpoints](#endpoints)
- [Erros](#erros)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Limitações conhecidas](#limitações-conhecidas)
- [Créditos dos dados](#créditos-dos-dados)

## Funcionalidades
- **Usuários** com senha criptografada (BCrypt) e e-mail único.
- **Progresso**: XP total, nível (a cada 100 XP sobe um nível) e categoria. Treinos somam XP automaticamente.
- **Tarefas** diárias ou recorrentes, com marcação de concluída.
- **Atividades** físicas (tipo, duração, calorias gastas).
- **Refeições** montadas com alimentos do catálogo; cada item calcula calorias, proteína, carboidrato e gordura pela quantidade, e a refeição tem o total nutricional.
- **Treinos** com exercícios, séries, repetições e carga.
- **Catálogo de alimentos**: ~600 alimentos brasileiros da TACO + produtos industrializados do Open Food Facts (por código de barras ou nome) + alimentos cadastrados pelo usuário.
- **Catálogo de exercícios**: ~870 exercícios com grupo muscular em português, equipamento, nível, instruções e imagem.

## Tecnologias
| | |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, RestClient, Actuator) |
| Banco de dados | PostgreSQL (Supabase) |
| ORM | Hibernate (`ddl-auto=update` — o schema é criado/atualizado automaticamente) |
| Segurança | `spring-security-crypto` (apenas BCrypt para senhas) |
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
| `OFF_CONTACT` | E-mail ou site enviado no `User-Agent` do Open Food Facts (a API deles pede uma forma de contato) |

O `.env` é carregado automaticamente pelo `application.properties` e está no `.gitignore` — nunca faça commit dele.

### Propriedades (`application.properties`)
| Propriedade | Padrão | Descrição |
|---|---|---|
| `app.seed.enabled` | `true` | Importa TACO e free-exercise-db na inicialização (se ainda não estiverem no banco) |
| `app.openfoodfacts.base-url` | `https://world.openfoodfacts.org` | Endereço da API do Open Food Facts |
| `app.openfoodfacts.user-agent` | `FantesDay/0.0.1 (${OFF_CONTACT})` | Identificação enviada ao Open Food Facts |

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

## Endpoints
Todas as rotas começam com `/api`. Atualizações usam `PATCH` e alteram apenas os campos enviados no corpo.

### Usuários e progresso
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/users` | Lista usuários |
| `POST` | `/api/users` | Cria usuário (e seu progresso zerado) |
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
| `PATCH` | `/api/tasks/{id}/complete` | Marca como concluída |
| `PATCH` | `/api/tasks/{id}/uncomplete` | Desmarca |
| `GET` | `/api/users/{userId}/activities` | Lista atividades |
| `POST` | `/api/users/{userId}/activities` | Cria atividade |
| `GET` / `PATCH` / `DELETE` | `/api/activities/{id}` | Busca, atualiza ou exclui |

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
| `POST` | `/api/users/{userId}/meals` | Cria refeição (data padrão: hoje) |
| `GET` / `PATCH` / `DELETE` | `/api/meals/{id}` | Busca, atualiza ou exclui (com os itens) |
| `GET` | `/api/meals/{id}/items` | Itens da refeição |
| `POST` | `/api/meals/{id}/items` | Adiciona alimento (`{"foodId": 3, "quantity": 150, "unit": "g"}`) |
| `GET` | `/api/meals/{id}/nutrition` | Total de calorias, proteína, carboidrato e gordura |
| `PATCH` / `DELETE` | `/api/meal-items/{id}` | Atualiza ou remove um item |

Os valores nutricionais são calculados como `quantidade × valor por 100g ÷ 100`, considerando a quantidade em gramas (ou ml).

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
| `POST` | `/api/workouts/{id}/exercises` | Adiciona exercício (`{"exerciseId": 1, "sets": 4, "reps": 10, "weightKg": 40}`) |
| `PATCH` / `DELETE` | `/api/workout-exercises/{id}` | Atualiza ou remove um exercício do treino |

### Exemplo
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Usuario Teste","email":"teste@fantesday.test","password":"senha123"}'
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
| `404` | Registro não encontrado |

## Testes

### Unitários
```bash
./mvnw test -Dtest=MuscleGroupTranslatorTest,OpenFoodFactsClientTest,MealServiceCalculationTest
```
Não precisam de banco: cobrem a tradução de músculos, o client do Open Food Facts (com `MockRestServiceServer`) e o cálculo nutricional.
O `FantesdayApplicationTests` sobe o contexto completo e precisa de um banco configurado no `.env`.

### Manuais (Insomnia)
A pasta [`insomnia/`](insomnia/) tem uma coleção pronta com todas as requisições:
1. No Insomnia: **Import → From File** → `insomnia/FantesDay.insomnia.json`.
2. Ajuste as variáveis do ambiente (`user_id`, `meal_id`, ...) com os ids retornados.

Os corpos JSON de cada requisição também estão em `insomnia/bodies/`. Veja o [README da pasta](insomnia/README.md).

## Estrutura do projeto
```
src/main/java/com/vhfantes/fantesday
├── client/        # Client HTTP do Open Food Facts e DTOs da resposta
├── config/        # BCrypt, RestClient e log de conexão com o banco
├── controller/    # Endpoints REST
├── dto/           # Records de request/response (XxxRequestDTO / XxxResponseDTO)
├── entity/        # Entidades JPA
├── exception/     # Exceções de negócio e handler global de erros
├── repository/    # Repositórios Spring Data JPA
├── seed/          # Importação da TACO e do free-exercise-db
└── service/       # Regras de negócio
src/main/resources
├── application.properties
└── seed/          # taco.json e exercises.json
```

### Modelo de dados
- `User` 1—1 `Progress`
- `User` 1—N `Task`, `Activity`, `Meal`, `Workout`
- `Meal` 1—N `MealItem` N—1 `Food`
- `Workout` 1—N `WorkoutExercise` N—1 `Exercise`
- `Food` e `Exercise` são catálogos globais (não pertencem a um usuário)

## Limitações conhecidas
- **Sem autenticação**: qualquer cliente acessa qualquer rota. O `userId` vai na URL.
- **Excluir um usuário com dados** (tarefas, refeições, treinos ou atividades) falha, porque esses registros não são apagados junto. Apague-os antes.
- **Busca no Open Food Facts**: a busca por nome deles tem limite de requisições (~10/min) e às vezes responde 503. Nesses casos `/api/foods/external` devolve lista vazia.
- **Schema gerenciado pelo Hibernate** (`ddl-auto=update`): não há migrações versionadas (Flyway/Liquibase).
- **Unidades**: o cálculo nutricional considera a quantidade em gramas/ml.

## Créditos dos dados
- **TACO** — Tabela Brasileira de Composição de Alimentos, 4ª edição, NEPA/UNICAMP. Dados processados a partir do repositório [brolesi/taco](https://github.com/brolesi/taco) (MIT).
- **free-exercise-db** — [yuhonas/free-exercise-db](https://github.com/yuhonas/free-exercise-db) (Unlicense / domínio público). As imagens são carregadas direto do repositório no GitHub.
- **Open Food Facts** — [openfoodfacts.org](https://world.openfoodfacts.org), dados sob a licença [ODbL](https://opendatacommons.org/licenses/odbl/1-0/).
