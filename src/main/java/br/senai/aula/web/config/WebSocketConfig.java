package br.senai.aula.web.config;
import br.senai.aula.web.infrastructure.web.game.websocket.GameSocketHandshakeInterceptor;
import br.senai.aula.web.infrastructure.web.game.websocket.GameStateWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
@Configuration @EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final GameStateWebSocketHandler handler; private final GameSocketHandshakeInterceptor interceptor; private final String[] origins;
    public WebSocketConfig(GameStateWebSocketHandler handler,GameSocketHandshakeInterceptor interceptor,@Value("${app.cors.allowed-origins:http://localhost:4200,https://verbose-broccoli-p5wx9jvjpqq3674g-3000.app.github.dev,https://*.app.github.dev,https://italokbb.github.io}") String origins){this.handler=handler;this.interceptor=interceptor;this.origins=origins.split(",");}
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){registry.addHandler(handler,"/ws/games/*").addInterceptors(interceptor).setAllowedOriginPatterns(origins);}
}
