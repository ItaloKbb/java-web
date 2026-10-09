package br.senai.aula.web.infrastructure.web.game.websocket;

import br.senai.aula.web.application.match.GameStateChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.WebSocketSession;

/** Após o commit de uma ação, empurra o novo estado para cada jogador conectado à partida. */
@Component
public class GameStateBroadcaster {
    private static final Logger log = LoggerFactory.getLogger(GameStateBroadcaster.class);
    private final GameSocketRegistry registry;
    private final GameStateWebSocketHandler handler;

    public GameStateBroadcaster(GameSocketRegistry registry, GameStateWebSocketHandler handler) {
        this.registry = registry;
        this.handler = handler;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onStateChanged(GameStateChangedEvent event) {
        for (WebSocketSession session : registry.sessions(event.gameId())) {
            try {
                handler.sendState(session);
            } catch (RuntimeException exception) {
                // A ação REST já foi confirmada; uma falha no push não deve propagar.
                log.warn("Falha ao enviar estado da partida {} para a sessão {}", event.gameId(), session.getId(), exception);
            }
        }
    }
}
