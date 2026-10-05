package br.senai.aula.web.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String[] origins;
    public CorsConfig(@Value("${app.cors.allowed-origins:http://localhost:4200,https://verbose-broccoli-p5wx9jvjpqq3674g-3000.app.github.dev,https://*.app.github.dev}") String origins){this.origins=origins.split(",");}
    @Override public void addCorsMappings(CorsRegistry registry){registry.addMapping("/**").allowedOriginPatterns(origins).allowedMethods("GET","POST","PUT","OPTIONS").allowedHeaders("*");}
}
