package br.senai.aula.web;

import br.senai.aula.web.infrastructure.persistence.skills.repository.SkillsJpaRepository;
import br.senai.aula.web.infrastructure.web.ApiExceptionHandler;
import br.senai.aula.web.infrastructure.web.admin.AdminController;
import br.senai.aula.web.infrastructure.web.admin.AdminCatalogService;
import br.senai.aula.web.infrastructure.web.card.CardController;
import br.senai.aula.web.infrastructure.web.puzzle.controller.PuzzleController;
import br.senai.aula.web.infrastructure.web.skill.controller.SkillController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.senai.aula.web.infrastructure.persistence.card.entity.repository.CardJpaRepository;
import br.senai.aula.web.infrastructure.persistence.match.repository.PuzzleChallengeJpaRepository;
import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AdminCatalogTests {
    @Autowired AdminController admin;
    @Autowired CardController cards;
    @Autowired SkillController skills;
    @Autowired PuzzleController puzzles;
    @Autowired SkillsJpaRepository skillRepository;
    @Autowired br.senai.aula.web.infrastructure.persistence.puzzle.repository.PuzzleJpaRepository puzzleRepository;
    @Autowired ApplicationRunner catalogSeed;
    private final ObjectMapper json = new ObjectMapper();

    private MockMvc mvc() {
        return standaloneSetup(admin, cards, skills, puzzles)
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void adminLoginAndCatalogLifecycle() throws Exception {
        MockMvc mvc = mvc();
        mvc.perform(get("/admin/skills")).andExpect(status().isUnauthorized());
        mvc.perform(post("/admin/sessions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"aluno\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());

        String loginBody = mvc.perform(post("/admin/sessions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"aluno\",\"password\":\"aluno\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = json.readTree(loginBody).get("token").asText();
        assertFalse(token.isBlank());
        mvc.perform(get("/admin/session").header("X-Admin-Token", token)).andExpect(status().isOk());

        String skillInput = "{\"name\":\"Teste\",\"description\":\"Efeito de teste\",\"type\":\"BLOCK\",\"naipe\":\"COPAS\",\"valor\":\"AS\"}";
        String created = mvc.perform(post("/admin/skills").header("X-Admin-Token", token)
                .contentType(MediaType.APPLICATION_JSON).content(skillInput))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long skillId = json.readTree(created).get("id").asLong();
        mvc.perform(post("/admin/skills").header("X-Admin-Token", token)
                .contentType(MediaType.APPLICATION_JSON).content(skillInput)).andExpect(status().isConflict());
        mvc.perform(put("/admin/skills/{id}", skillId).header("X-Admin-Token", token)
                .contentType(MediaType.APPLICATION_JSON).content(skillInput.replace("Teste", "Editada")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Editada"));
        mvc.perform(delete("/admin/skills/{id}", skillId).header("X-Admin-Token", token))
                .andExpect(status().isNoContent());
        assertTrue(skillRepository.findById(skillId).isPresent());
        assertNotNull(skillRepository.findById(skillId).orElseThrow().getArchivedAt());
        assertTrue(skillRepository.findByValorAndNaipeAndArchivedAtIsNull(
                br.senai.aula.web.domain.cards.Valor.AS,
                br.senai.aula.web.domain.cards.Naipe.COPAS).isEmpty());

        String puzzleInput = "{\"question\":\"Teste?\",\"alternativas\":[\"A\",\"B\"],\"alternativaCorreta\":1}";
        String puzzleCreated = mvc.perform(post("/admin/puzzles").header("X-Admin-Token", token)
                .contentType(MediaType.APPLICATION_JSON).content(puzzleInput))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long puzzleId = json.readTree(puzzleCreated).get("id").asLong();
        JsonNode publicPuzzle = json.readTree(mvc.perform(get("/puzzles/{id}", puzzleId))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertFalse(publicPuzzle.has("alternativaCorreta"));
        mvc.perform(put("/admin/puzzles/{id}", puzzleId).header("X-Admin-Token", token)
                .contentType(MediaType.APPLICATION_JSON).content(puzzleInput.replace("Teste?", "Editado?")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.question").value("Editado?"));
        mvc.perform(delete("/admin/puzzles/{id}", puzzleId).header("X-Admin-Token", token))
                .andExpect(status().isNoContent());
        assertNotNull(puzzleRepository.findById(puzzleId).orElseThrow().getArchivedAt());
        mvc.perform(get("/puzzles/{id}", puzzleId)).andExpect(status().isNotFound());

        catalogSeed.run(null);
        assertNotNull(skillRepository.findById(skillId).orElseThrow().getArchivedAt());
        assertNotNull(puzzleRepository.findById(puzzleId).orElseThrow().getArchivedAt());
        mvc.perform(delete("/admin/session").header("X-Admin-Token", token)).andExpect(status().isNoContent());
        mvc.perform(get("/admin/session").header("X-Admin-Token", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void pendingChallengeBlocksPuzzleEdit() {
        CardJpaRepository cards = mock(CardJpaRepository.class);
        SkillsJpaRepository skills = mock(SkillsJpaRepository.class);
        var puzzles = mock(br.senai.aula.web.infrastructure.persistence.puzzle.repository.PuzzleJpaRepository.class);
        PuzzleChallengeJpaRepository challenges = mock(PuzzleChallengeJpaRepository.class);
        PuzzleJpaEntity puzzle = new PuzzleJpaEntity("Pergunta original?", new String[]{"A", "B"}, 0);
        when(puzzles.findById(42L)).thenReturn(java.util.Optional.of(puzzle));
        when(challenges.existsByPuzzleAndAnsweredFalse(puzzle)).thenReturn(true);
        AdminCatalogService service = new AdminCatalogService(cards, skills, puzzles, challenges);

        assertThrows(IllegalStateException.class, () -> service.updatePuzzle(42,
                new AdminCatalogService.PuzzleInput("Pergunta alterada?", new String[]{"C", "D"}, 1)));
        assertEquals("Pergunta original?", puzzle.getQuestion());
    }
}
