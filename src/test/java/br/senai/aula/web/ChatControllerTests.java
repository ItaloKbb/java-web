package br.senai.aula.web;

import br.senai.aula.web.application.auth.AuthenticationService;
import br.senai.aula.web.infrastructure.web.ApiExceptionHandler;
import br.senai.aula.web.infrastructure.web.chat.ChatController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ChatControllerTests {
    @Autowired ChatController chat;
    @Autowired AuthenticationService authentication;
    private final ObjectMapper json = new ObjectMapper();

    private MockMvc mvc() {
        return standaloneSetup(chat).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void sendAndPollMessages() throws Exception {
        MockMvc mvc = mvc();
        String ana = authentication.login("ana-chat", "1234").token();
        String bia = authentication.login("bia-chat", "1234").token();

        mvc.perform(get("/chat/messages").header("X-Player-Token", "invalido")).andExpect(status().isUnauthorized());
        mvc.perform(post("/chat/messages").header("X-Player-Token", ana)
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/chat/messages").header("X-Player-Token", ana)
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"" + "a".repeat(281) + "\"}"))
                .andExpect(status().isBadRequest());

        String created = mvc.perform(post("/chat/messages").header("X-Player-Token", ana)
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"  Bora? ABC234  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("ana-chat"))
                .andExpect(jsonPath("$.text").value("Bora? ABC234"))
                .andReturn().getResponse().getContentAsString();
        long firstId = json.readTree(created).get("id").asLong();

        mvc.perform(post("/chat/messages").header("X-Player-Token", ana)
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"flood\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/chat/messages").header("X-Player-Token", bia)
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Partiu\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/chat/messages").header("X-Player-Token", bia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Bora? ABC234"))
                .andExpect(jsonPath("$[1].nickname").value("bia-chat"));
        mvc.perform(get("/chat/messages").param("after", String.valueOf(firstId)).header("X-Player-Token", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Partiu"));
    }
}
