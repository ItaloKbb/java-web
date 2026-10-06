# Truno Crazzy API

API didática em Java 17 e Spring Boot para partidas completas de Truno Crazzy. O cliente Angular consome a API por REST e pode consultar o estado da partida aproximadamente a cada segundo.

## Executar

No Windows:

```powershell
.\gradlew.bat bootRun
```

A API usa `http://localhost:3000` e o Swagger fica em `http://localhost:3000/swagger-ui.html`. O banco H2 é persistido em `data/` e permite retomar partidas depois de reiniciar a aplicação.

O CORS permite inicialmente `http://localhost:4200`. Outros endereços podem ser informados, separados por vírgula, em `app.cors.allowed-origins`.

Em um GitHub Codespace, consulte [como acessar a API publicamente](docs/public-forwarding.md) quando o frontend ou outro dispositivo não conseguir usar `localhost`.

## Autenticação

Crie ou acesse uma conta com:

```http
POST /auth/sessions
Content-Type: application/json

{"nickname":"Maria","code":"1234"}
```

O primeiro acesso cria a conta. Os seguintes validam o mesmo `code`. A resposta contém um token; envie-o nas demais ações:

```http
X-Player-Token: token-retornado
```

Um novo login invalida o token anterior.

## Fluxo de uma partida

| Método | Rota | Uso |
|---|---|---|
| `POST` | `/games` | Cria o lobby e adiciona o host |
| `POST` | `/games/access` | Entra pelo código de seis caracteres |
| `POST` | `/games/{id}/start` | Host inicia com dois ou mais jogadores |
| `GET` | `/games/{id}/state` | Consulta o estado agregado e a própria mão |
| `POST` | `/games/{id}/plays` | Joga uma carta da própria mão |
| `POST` | `/games/{id}/puzzle-answers` | Responde ao desafio pendente |
| `POST` | `/games/{id}/trophies` | Compra no máximo um troféu na janela atual |
| `POST` | `/games/{id}/ready` | Passa/confirma a janela entre rodadas |
| `POST` | `/games/{id}/cancel` | Host cancela a partida |
| `GET` | `/users/ranking` | Lista o ranking permanente |

Exemplo de criação:

```json
{
  "name": "Sala 1",
  "maxPlayers": 4,
  "initialCards": 3,
  "roundReward": 2,
  "emptyHandReward": 1,
  "trophyPrice": 5
}
```

Para jogar uma carta, use o `handCardId` recebido somente na mão do jogador autenticado:

```json
{"handCardId": 18}
```

O campo `stateVersion` muda depois de cada ação. O Angular pode ignorar respostas cuja versão já tenha processado. As mãos adversárias expõem somente `handSize`; puzzles nunca expõem `alternativaCorreta`.

## Catálogos

As 40 cartas, dez skills e puzzles demonstrativos são carregados de forma idempotente. São somente leitura pela API:

- `GET /cards` e `GET /cards/{id}`;
- `GET /skills`;
- `GET /puzzles` e `GET /puzzles/{id}`.

## Erros

Todos os erros retornam `{"message":"..."}`:

- `400`: entrada inválida;
- `401`: token ou credenciais inválidas;
- `403`: usuário sem permissão ou carta de outro jogador;
- `404`: recurso não encontrado;
- `409`: fase, turno ou versão conflitante.

Para conferir apenas a compilação, sem testes:

```powershell
.\gradlew.bat compileJava
```
