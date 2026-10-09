package br.senai.aula.web.infrastructure.web.game.websocket;

import br.senai.aula.web.application.auth.AuthenticationService;
import br.senai.aula.web.application.auth.ForbiddenException;
import br.senai.aula.web.application.auth.UnauthorizedException;
import br.senai.aula.web.application.match.GameEngineService;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.NoSuchElementException;

/** Valida o token (query "token") e a participação no jogo antes de abrir o socket. */
@Component
public class GameSocketHandshakeInterceptor implements HandshakeInterceptor {
    static final String GAME_ID = "gameId";
    static final String USER_ID = "userId";
    private final AuthenticationService authentication;
    private final GameEngineService engine;

    public GameSocketHandshakeInterceptor(AuthenticationService authentication, GameEngineService engine) {
        this.authentication = authentication;
        this.engine = engine;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Map<String, Object> attributes) {
        Long gameId = gameId(request.getURI().getPath());
        if (gameId == null) { response.setStatusCode(HttpStatus.NOT_FOUND); return false; }
        String token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        try {
            UserJpaEntity user = authentication.requireUser(token);
            engine.getState(user, gameId);
            attributes.put(GAME_ID, gameId);
            attributes.put(USER_ID, user.getId());
            return true;
        } catch (UnauthorizedException exception) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        } catch (ForbiddenException exception) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException exception) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Exception exception) { }

    private static Long gameId(String path) {
        String last = path.substring(path.lastIndexOf('/') + 1);
        try { return Long.parseLong(last); } catch (NumberFormatException exception) { return null; }
    }
}
