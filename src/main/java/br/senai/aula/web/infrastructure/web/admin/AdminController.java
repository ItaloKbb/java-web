package br.senai.aula.web.infrastructure.web.admin;

import br.senai.aula.web.infrastructure.web.admin.AdminCatalogService.AdminPuzzleResponse;
import br.senai.aula.web.infrastructure.web.admin.AdminCatalogService.PuzzleInput;
import br.senai.aula.web.infrastructure.web.admin.AdminCatalogService.SkillInput;
import br.senai.aula.web.infrastructure.web.skill.response.SkillResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    public record LoginRequest(String username, String password) {}

    private final AdminSessionService sessions;
    private final AdminCatalogService catalog;

    public AdminController(AdminSessionService sessions, AdminCatalogService catalog) {
        this.sessions = sessions;
        this.catalog = catalog;
    }

    @PostMapping("/sessions")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        return Map.of("token", sessions.login(request.username(), request.password()));
    }

    @GetMapping("/session")
    public Map<String, String> session(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        sessions.require(token);
        return Map.of("username", "aluno");
    }

    @DeleteMapping("/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        sessions.logout(token);
    }

    @GetMapping("/skills")
    public List<SkillResponse> skills(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        sessions.require(token);
        return catalog.skills();
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse createSkill(@RequestHeader(value = "X-Admin-Token", required = false) String token,
                                     @RequestBody SkillInput request) {
        sessions.require(token);
        return catalog.createSkill(request);
    }

    @PutMapping("/skills/{id}")
    public SkillResponse updateSkill(@RequestHeader(value = "X-Admin-Token", required = false) String token,
                                     @PathVariable long id, @RequestBody SkillInput request) {
        sessions.require(token);
        return catalog.updateSkill(id, request);
    }

    @DeleteMapping("/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveSkill(@RequestHeader(value = "X-Admin-Token", required = false) String token,
                             @PathVariable long id) {
        sessions.require(token);
        catalog.archiveSkill(id);
    }

    @GetMapping("/puzzles")
    public List<AdminPuzzleResponse> puzzles(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        sessions.require(token);
        return catalog.puzzles();
    }

    @PostMapping("/puzzles")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminPuzzleResponse createPuzzle(@RequestHeader(value = "X-Admin-Token", required = false) String token,
                                            @RequestBody PuzzleInput request) {
        sessions.require(token);
        return catalog.createPuzzle(request);
    }

    @PutMapping("/puzzles/{id}")
    public AdminPuzzleResponse updatePuzzle(@RequestHeader(value = "X-Admin-Token", required = false) String token,
                                            @PathVariable long id, @RequestBody PuzzleInput request) {
        sessions.require(token);
        return catalog.updatePuzzle(id, request);
    }

    @DeleteMapping("/puzzles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archivePuzzle(@RequestHeader(value = "X-Admin-Token", required = false) String token,
                              @PathVariable long id) {
        sessions.require(token);
        catalog.archivePuzzle(id);
    }
}
