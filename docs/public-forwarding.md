# Acesso público no GitHub Codespaces

A API roda na porta `3000`. Dentro do Codespace, `localhost:3000` funciona
somente para o próprio ambiente. Para acessar a API pelo navegador, por um
frontend hospedado fora do Codespace ou por outro dispositivo, a porta precisa
estar encaminhada como pública.

## Configuração permanente

O arquivo `.devcontainer/devcontainer.json` já declara:

- encaminhamento automático da porta `3000`;
- visibilidade `public`;
- abertura automática do endereço encaminhado.

Depois de alterar esse arquivo, recrie o Codespace ou execute **Codespaces:
Rebuild Container** no VS Code. A configuração só é aplicada quando o
container é criado/recriado.

## Como obter o endereço

1. Inicie a aplicação com `./gradlew bootRun` (Linux/macOS) ou
   `.\gradlew.bat bootRun` (Windows).
2. Abra a aba **PORTS** no VS Code.
3. Localize a porta `3000` e confirme que **Visibility** está como **Public**.
4. Use o endereço HTTPS exibido nessa linha. O endereço pode mudar quando um
   novo Codespace é criado.

O formato usual é:

```text
https://NOME_DO_CODESPACE-3000.app.github.dev
```

Não use `http://localhost:3000` no frontend que será acessado fora do
Codespace.

## Diagnóstico rápido

Teste primeiro a aplicação dentro do Codespace:

```bash
curl -i http://127.0.0.1:3000/actuator/health
```

O resultado esperado é `HTTP/1.1 200` e `"status":"UP"`.

Depois teste o endereço público copiado da aba **PORTS**:

```bash
curl -i https://NOME_DO_CODESPACE-3000.app.github.dev/actuator/health
```

O resultado esperado é `HTTP/2 200`. Interpretação dos principais resultados:

| Resultado | Causa provável | Solução |
|---|---|---|
| `200` local e `302` para `github.dev/pf-signin` público | A porta está privada | Em **PORTS**, mude **Visibility** para **Public** |
| `200` local e erro `502`/`503` público | A aplicação parou ou a porta não está encaminhada | Inicie novamente `./gradlew bootRun` e confirme a porta `3000` |
| `404` em `/` | A URL ou rota foi digitada incorretamente | Teste `/actuator/health` ou `/swagger-ui.html` |
| API pública responde, mas o frontend falha | O frontend ainda aponta para `localhost` ou há erro de CORS | Troque a base da API pela URL HTTPS pública |

Se a CLI do GitHub estiver autenticada, a visibilidade também pode ser
ajustada pelo terminal:

```bash
gh codespace ports visibility 3000:public -c "$CODESPACE_NAME"
```

Se aparecer `gh auth login`, autentique a CLI com escopo de Codespaces e
repita o comando:

```bash
gh auth login -s codespace
```

## URLs úteis

Substitua `NOME_DO_CODESPACE` pelo nome atual:

```text
https://NOME_DO_CODESPACE-3000.app.github.dev/
https://NOME_DO_CODESPACE-3000.app.github.dev/swagger-ui.html
https://NOME_DO_CODESPACE-3000.app.github.dev/actuator/health
```

O CORS da API já aceita origens `https://*.app.github.dev`. Portanto, quando
a porta estiver pública, normalmente não é necessário alterar o backend por
causa do domínio do Codespace.
