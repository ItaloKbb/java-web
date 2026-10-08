package br.senai.aula.web;

import br.senai.aula.web.application.auth.AuthenticationService;
import br.senai.aula.web.infrastructure.web.ApiExceptionHandler;
import br.senai.aula.web.infrastructure.web.profile.ProfileController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:profile-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProfileControllerTests {
    @Autowired ProfileController profile;
    @Autowired AuthenticationService authentication;

    private MockMvc mvc() {
        return standaloneSetup(profile).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void newPlayerProfile() throws Exception {
        MockMvc mvc = mvc();
        String token = authentication.login("Ana Perfil!", "1234").token();

        mvc.perform(get("/profile").header("X-Player-Token", "invalido")).andExpect(status().isUnauthorized());
        mvc.perform(get("/profile").header("X-Player-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.displayName").value("Ana Perfil!"))
                .andExpect(jsonPath("$.username").value("AnaPerfil"))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.experienceToNextLevel").value(100))
                .andExpect(jsonPath("$.avatarUrl").doesNotExist());
        mvc.perform(get("/profile/stats").header("X-Player-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchesPlayed").value(0))
                .andExpect(jsonPath("$.winRate").value(0.0));
        mvc.perform(get("/profile/collection").header("X-Player-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rarity").value("Comum"));
        mvc.perform(get("/profile/achievements").header("X-Player-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].progress").value(0))
                .andExpect(jsonPath("$[0].unlockedAt").doesNotExist());
        mvc.perform(get("/profile/activities").header("X-Player-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
