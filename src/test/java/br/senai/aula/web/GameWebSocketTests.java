package br.senai.aula.web;

import br.senai.aula.web.application.auth.AuthenticationService;
import br.senai.aula.web.application.match.GameEngineService;
import br.senai.aula.web.infrastructure.web.game.response.GameStateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:game-ws-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class GameWebSocketTests {
    @LocalServerPort int port;
    @Autowired AuthenticationService authentication;
    @Autowired GameEngineService engine;
    @Autowired JsonMapper json;

    @Test
    void pushesStateAndValidatesVersion() throws Exception {
        String anaToken = authentication.login("ana-ws", "1234").token();
        String biaToken = authentication.login("bia-ws", "1234").token();
        GameStateResponse created = engine.create(authentication.requireUser(anaToken), "Mesa WS", 4, 3, 2, 1, 5);

        BlockingQueue<JsonNode> inbox = new LinkedBlockingQueue<>();
        WebSocketSession ana = connect(created.id(), anaToken, inbox);

        JsonNode initial = next(inbox);
        assertEquals("STATE", initial.get("type").asText());
        long v0 = initial.get("state").get("stateVersion").asLong();
        assertEquals(1, initial.get("state").get("players").size());

        engine.access(authentication.requireUser(biaToken), created.code());
        JsonNode pushed = next(inbox);
        assertEquals("STATE", pushed.get("type").asText());
        long v1 = pushed.get("state").get("stateVersion").asLong();
        assertTrue(v1 > v0);
        assertEquals(2, pushed.get("state").get("players").size());

        ana.sendMessage(new TextMessage("{\"type\":\"SYNC\",\"stateVersion\":" + v0 + "}"));
        JsonNode stale = next(inbox);
        assertEquals("STATE", stale.get("type").asText());
        assertEquals(v1, stale.get("state").get("stateVersion").asLong());

        ana.sendMessage(new TextMessage("{\"type\":\"SYNC\",\"stateVersion\":" + v1 + "}"));
        JsonNode inSync = next(inbox);
        assertEquals("IN_SYNC", inSync.get("type").asText());
        assertEquals(v1, inSync.get("stateVersion").asLong());

        ana.sendMessage(new TextMessage("{\"type\":\"PLAY\"}"));
        assertEquals("ERROR", next(inbox).get("type").asText());
        ana.close();
    }

    @Test
    void rejectsInvalidTokenAndNonMembers() {
        String anaToken = authentication.login("ana-ws-deny", "1234").token();
        String caioToken = authentication.login("caio-ws-deny", "1234").token();
        GameStateResponse created = engine.create(authentication.requireUser(anaToken), "Mesa fechada", 4, 3, 2, 1, 5);

        assertThrows(ExecutionException.class, () -> connect(created.id(), "invalido", new LinkedBlockingQueue<>()));
        assertThrows(ExecutionException.class, () -> connect(created.id(), caioToken, new LinkedBlockingQueue<>()));
    }

    private WebSocketSession connect(long gameId, String token, BlockingQueue<JsonNode> inbox) throws Exception {
        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                inbox.add(json.readTree(message.getPayload()));
            }
        };
        URI uri = URI.create("ws://localhost:" + port + "/ws/games/" + gameId + "?token=" + token);
        return new StandardWebSocketClient().execute(handler, null, uri).get(5, TimeUnit.SECONDS);
    }

    private static JsonNode next(BlockingQueue<JsonNode> inbox) throws InterruptedException {
        JsonNode message = inbox.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "Nenhuma mensagem recebida pelo socket");
        return message;
    }
}
