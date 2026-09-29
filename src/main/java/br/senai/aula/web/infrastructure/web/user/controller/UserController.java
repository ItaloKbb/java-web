package br.senai.aula.web.infrastructure.web.user.controller;
import br.senai.aula.web.infrastructure.persistence.user.repository.UserJpaRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/users")
public class UserController { private final UserJpaRepository users; public UserController(UserJpaRepository users){this.users=users;} public record RankingResponse(Long id,String nickname,Integer rankingPoints){} @GetMapping("/ranking") public List<RankingResponse> ranking(){return users.findAllByOrderByRankingPointsDescNicknameAsc().stream().map(u->new RankingResponse(u.getId(),u.getNickname(),u.getRankingPoints())).toList();}}
