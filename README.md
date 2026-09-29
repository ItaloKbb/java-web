# Truno Crazzy API

API didática desenvolvida em Java com Spring Boot. O projeto organiza domínio, casos de uso, entrada HTTP e persistência em camadas separadas, mantendo uma implementação simples para estudo.

## Tecnologias

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- H2 Database
- OpenAPI/Swagger
- Gradle Wrapper

## Executar

No Windows:

```powershell
.\gradlew.bat bootRun
```

No Linux ou macOS:

```bash
./gradlew bootRun
```

A aplicação utiliza a porta `3000`:

```text
http://localhost:3000
```

O Swagger fica disponível em:

```text
http://localhost:3000/swagger-ui.html
```

O banco H2 funciona em memória e é recriado sempre que a aplicação inicia.

## Endpoints

### Usuários

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/users` | Cria um usuário com saldo inicial zerado |
| `GET` | `/users/{userId}/coins` | Consulta o saldo do usuário |

Exemplo para criar um usuário:

```json
{
  "name": "Maria",
  "email": "maria@exemplo.com"
}
```

### Cartas

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/cards` | Cria uma carta |
| `GET` | `/cards` | Lista as cartas |
| `GET` | `/cards/{cardId}` | Busca uma carta |
| `DELETE` | `/cards/{cardId}` | Exclui uma carta |

Exemplo:

```json
{
  "valor": "AS",
  "naipe": "COPAS"
}
```

### Habilidades

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/skills` | Cria uma habilidade |
| `DELETE` | `/skills/{skillId}` | Exclui uma habilidade |

### Puzzles

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/puzzles` | Cria um puzzle |
| `GET` | `/puzzles` | Lista os puzzles |
| `GET` | `/puzzles/{puzzleId}` | Busca um puzzle |

Exemplo:

```json
{
  "alternativas": ["Azul", "Verde", "Vermelho"],
  "alternativaCorreta": 1
}
```

`alternativaCorreta` representa a posição da resposta no array, começando em zero.

### Partidas

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/games` | Cria uma partida |
| `GET` | `/games/{gameId}` | Busca uma partida |
| `POST` | `/games/{gameId}/players` | Adiciona um usuário à partida |
| `GET` | `/games/{gameId}/players` | Lista os jogadores |
| `POST` | `/games/{gameId}/rounds` | Inicia uma rodada |
| `GET` | `/games/rounds/{roundId}` | Busca uma rodada |
| `POST` | `/games/rounds/{roundId}/finish` | Finaliza uma rodada |
| `POST` | `/games/deck` | Cria um deck |

Uma partida aceita de 2 a 6 jogadores. O mesmo usuário não pode ser adicionado duas vezes à mesma partida.

## Respostas de erro

Os erros utilizam o formato:

```json
{
  "message": "Descrição do problema"
}
```

- `400 Bad Request`: dados ou regra de entrada inválidos;
- `404 Not Found`: recurso não encontrado;
- `409 Conflict`: registro duplicado, partida cheia ou operação repetida.

## Estrutura principal

```text
domain/          regras e objetos do domínio
application/     casos de uso, serviços e portas
infrastructure/  controllers, DTOs, JPA, adapters e mappers
config/          configuração dos casos de uso no Spring
```

Para conferir apenas o código principal, sem executar testes:

```powershell
.\gradlew.bat compileJava
```
