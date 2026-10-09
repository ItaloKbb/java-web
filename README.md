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

## WebSocket do estado da partida

Em vez de fazer polling em `GET /games/{id}/state`, o front pode abrir um socket por partida:

```
ws://localhost:3000/ws/games/{id}?token=<X-Player-Token>
```

O token vai na query porque o `WebSocket` do navegador não envia headers customizados. O handshake é recusado com `401` (token inválido), `403` (usuário fora do jogo) ou `404` (jogo inexistente). As ações continuam no REST; o socket serve só para receber e validar o estado.

Mensagens do servidor:

- `{"type":"STATE","state":{...}}`: o mesmo `GameStateResponse` do REST, com a mão de quem está conectado. É enviada ao conectar e após cada ação confirmada na partida, de qualquer jogador;
- `{"type":"IN_SYNC","stateVersion":n}`: resposta a um `SYNC` quando o cliente já tem a versão atual;
- `{"type":"ERROR","message":"..."}`.

Para validar o estado local, o cliente envia `{"type":"SYNC","stateVersion":n}`. Se `n` estiver defasado, recebe `STATE`; se não, recebe `IN_SYNC`.

```ts
const ws = new WebSocket(`${wsUrl}/ws/games/${gameId}?token=${token}`);
ws.onmessage = ({ data }) => {
  const msg = JSON.parse(data);
  if (msg.type === 'STATE' && msg.state.stateVersion > (this.state?.stateVersion ?? -1)) this.state = msg.state;
};
// ao voltar o foco da aba, por exemplo:
ws.send(JSON.stringify({ type: 'SYNC', stateVersion: this.state.stateVersion }));
```

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
