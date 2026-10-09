package br.senai.aula.web.infrastructure.web.game.websocket;

import br.senai.aula.web.infrastructure.web.game.response.GameStateResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

/** Mensagem enviada pelo servidor: STATE, IN_SYNC ou ERROR. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GameSocketMessage(String type, GameStateResponse state, Long stateVersion, String message) {
    public static final String STATE = "STATE";
    public static final String IN_SYNC = "IN_SYNC";
    public static final String ERROR = "ERROR";
    public static final String SYNC = "SYNC";

    public static GameSocketMessage state(GameStateResponse state) { return new GameSocketMessage(STATE, state, null, null); }
    public static GameSocketMessage inSync(Long stateVersion) { return new GameSocketMessage(IN_SYNC, null, stateVersion, null); }
    public static GameSocketMessage error(String message) { return new GameSocketMessage(ERROR, null, null, message); }

    /** Mensagem enviada pelo cliente: {"type":"SYNC","stateVersion":n}. */
    public record ClientMessage(String type, Long stateVersion) {}
}
