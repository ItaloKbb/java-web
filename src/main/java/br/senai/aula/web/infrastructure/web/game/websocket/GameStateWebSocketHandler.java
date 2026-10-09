package br.senai.aula.web.infrastructure.web.game.websocket;

import br.senai.aula.web.application.auth.ForbiddenException;
import br.senai.aula.web.application.match.GameEngineService;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import br.senai.aula.web.infrastructure.persistence.user.repository.UserJpaRepository;
import br.senai.aula.web.infrastructure.web.game.response.GameStateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Socket /ws/games/{id}?token=...: envia o estado do jogador ao conectar e a cada mudança,
 * e responde SYNC com IN_SYNC (versão atual) ou STATE (versão defasada).
 */
@Component
public class GameStateWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(GameStateWebSocketHandler.class);
    private final GameSocketRegistry registry;
    private final GameEngineService engine;
    private final UserJpaRepository users;
    private final JsonMapper json;

    public GameStateWebSocketHandler(GameSocketRegistry registry, GameEngineService engine, UserJpaRepository users, JsonMapper json) {
        this.registry = registry;
        this.engine = engine;
        this.users = users;
        this.json = json;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sendState(registry.add(gameId(session), session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession raw, TextMessage message) {
        WebSocketSession session = registry.find(gameId(raw), raw);
        if (session == null) return;
        GameSocketMessage.ClientMessage request;
        try {
            request = json.readValue(message.getPayload(), GameSocketMessage.ClientMessage.class);
        } catch (JacksonException exception) {
            send(session, GameSocketMessage.error("Mensagem inválida"));
            return;
        }
        if (!GameSocketMessage.SYNC.equals(request.type())) {
            send(session, GameSocketMessage.error("Tipo de mensagem não suportado"));
            return;
        }
        GameStateResponse state = loadState(session);
        if (state == null) return;
        send(session, Objects.equals(request.stateVersion(), state.stateVersion())
                ? GameSocketMessage.inSync(state.stateVersion()) : GameSocketMessage.state(state));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        registry.remove(gameId(session), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.remove(gameId(session), session);
    }

    /** Envia o estado atual visto pelo dono da sessão. */
    public void sendState(WebSocketSession session) {
        GameStateResponse state = loadState(session);
        if (state != null) send(session, GameSocketMessage.state(state));
    }

    private GameStateResponse loadState(WebSocketSession session) {
        try {
            UserJpaEntity user = users.findById(userId(session)).orElseThrow(() -> new ForbiddenException("Usuário não encontrado"));
            return engine.getState(user, gameId(session));
        } catch (ForbiddenException | NoSuchElementException exception) {
            send(session, GameSocketMessage.error(exception.getMessage()));
            close(session, CloseStatus.POLICY_VIOLATION);
            return null;
        }
    }

    private void send(WebSocketSession session, GameSocketMessage message) {
        if (!session.isOpen()) return;
        try {
            session.sendMessage(new TextMessage(json.writeValueAsString(message)));
        } catch (IOException | RuntimeException exception) {
            log.debug("Falha ao enviar mensagem para a sessão {}", session.getId(), exception);
            close(session, CloseStatus.SERVER_ERROR);
        }
    }

    private void close(WebSocketSession session, CloseStatus status) {
        registry.remove(gameId(session), session);
        try { session.close(status); } catch (IOException | RuntimeException ignored) { }
    }

    private static long gameId(WebSocketSession session) { return (Long) session.getAttributes().get(GameSocketHandshakeInterceptor.GAME_ID); }
    private static long userId(WebSocketSession session) { return (Long) session.getAttributes().get(GameSocketHandshakeInterceptor.USER_ID); }
}
