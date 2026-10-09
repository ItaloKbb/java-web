package br.senai.aula.web.infrastructure.web.game.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Sessões abertas por partida; cada uma decorada para permitir envios concorrentes. */
@Component
public class GameSocketRegistry {
    private static final int SEND_TIME_LIMIT_MS = 10_000;
    private static final int BUFFER_SIZE_LIMIT = 512 * 1024;
    private final Map<Long, Map<String, WebSocketSession>> byGame = new ConcurrentHashMap<>();

    public WebSocketSession add(long gameId, WebSocketSession session) {
        WebSocketSession safe = new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT_MS, BUFFER_SIZE_LIMIT);
        byGame.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(session.getId(), safe);
        return safe;
    }

    public WebSocketSession find(long gameId, WebSocketSession session) {
        Map<String, WebSocketSession> sessions = byGame.get(gameId);
        return sessions == null ? null : sessions.get(session.getId());
    }

    public void remove(long gameId, WebSocketSession session) {
        byGame.computeIfPresent(gameId, (id, sessions) -> {
            sessions.remove(session.getId());
            return sessions.isEmpty() ? null : sessions;
        });
    }

    public List<WebSocketSession> sessions(long gameId) {
        Map<String, WebSocketSession> sessions = byGame.get(gameId);
        return sessions == null ? List.of() : List.copyOf(sessions.values());
    }
}
