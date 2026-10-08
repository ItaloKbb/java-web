package br.senai.aula.web.application.profile;

import br.senai.aula.web.domain.match.GamePhase;
import br.senai.aula.web.infrastructure.persistence.card.entity.entity.CardEntity;
import br.senai.aula.web.infrastructure.persistence.card.entity.repository.CardJpaRepository;
import br.senai.aula.web.infrastructure.persistence.match.entity.MatchPlayerJpaEntity;
import br.senai.aula.web.infrastructure.persistence.match.repository.MatchPlayerJpaRepository;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Área `/perfil` do app: tudo é derivado do usuário e das partidas finalizadas.
 * Não há carteira nem inventário persistidos, então moedas = pontos de ranking,
 * gemas = 0 e a coleção é o catálogo inteiro.
 */
@Service
public class ProfileService {
    static final int POINTS_PER_LEVEL = 100;

    private final MatchPlayerJpaRepository players;
    private final CardJpaRepository cards;

    public ProfileService(MatchPlayerJpaRepository players, CardJpaRepository cards) {
        this.players = players;
        this.cards = cards;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ProfileResponse(String id, String displayName, String username, String avatarUrl, String bio,
                                  int level, int experience, int experienceToNextLevel, int coins, int gems,
                                  String favoriteCardId) {}
    public record StatsResponse(int matchesPlayed, int wins, double winRate, int currentWinStreak, int coins, int gems) {}
    public record CollectionCardResponse(String name, String rarity, int level, boolean obtained) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AchievementResponse(String id, String title, String description, int progress, int goal,
                                      String reward, Instant unlockedAt) {}
    public record ActivityResponse(String id, String result, String opponentName, Instant playedAt) {}

    public ProfileResponse profile(UserJpaEntity user) {
        int points = user.getRankingPoints();
        return new ProfileResponse(String.valueOf(user.getId()), user.getNickname(), username(user.getNickname()),
                null, null, points / POINTS_PER_LEVEL + 1, points % POINTS_PER_LEVEL, POINTS_PER_LEVEL, points, 0, null);
    }

    @Transactional(readOnly = true)
    public StatsResponse stats(UserJpaEntity user) {
        List<MatchPlayerJpaEntity> finished = finished(user);
        int wins = (int) finished.stream().filter(this::won).count();
        int streak = 0;
        for (MatchPlayerJpaEntity match : finished) { if (!won(match)) break; streak++; }
        double rate = finished.isEmpty() ? 0 : (double) wins / finished.size();
        return new StatsResponse(finished.size(), wins, rate, streak, user.getRankingPoints(), 0);
    }

    @Transactional(readOnly = true)
    public List<CollectionCardResponse> collection() {
        return cards.findAll().stream()
                .sorted(Comparator.comparing(CardEntity::getNaipe).thenComparing(CardEntity::getValor))
                .map(card -> new CollectionCardResponse(label(card.getValor().name()) + " de " + label(card.getNaipe().name()),
                        "Comum", 1, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> achievements(UserJpaEntity user) {
        List<MatchPlayerJpaEntity> finished = finished(user);
        List<MatchPlayerJpaEntity> played = new ArrayList<>(finished);
        Collections.reverse(played);
        List<MatchPlayerJpaEntity> victories = played.stream().filter(this::won).toList();
        return List.of(
                achievement("first-win", "Primeira vitória", "Vença sua primeira partida.", victories, 1, "10 pontos de ranking"),
                achievement("veteran", "Veterano", "Termine 10 partidas.", played, 10, "Selo de veterano"),
                achievement("champion", "Campeão", "Vença 10 partidas.", victories, 10, "Selo de campeão"));
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> activities(UserJpaEntity user) {
        return finished(user).stream().limit(20).map(match -> new ActivityResponse(
                String.valueOf(match.getGame().getId()),
                won(match) ? "VITORIA" : "DERROTA",
                opponents(match),
                match.getGame().getUpdatedAt())).toList();
    }

    private List<MatchPlayerJpaEntity> finished(UserJpaEntity user) {
        return players.findByUserAndPhase(user, GamePhase.FINALIZADO);
    }

    private boolean won(MatchPlayerJpaEntity match) {
        UserJpaEntity winner = match.getGame().getWinner();
        return winner != null && Objects.equals(winner.getId(), match.getUser().getId());
    }

    private String opponents(MatchPlayerJpaEntity match) {
        String names = match.getGame().getPlayers().stream()
                .filter(other -> !Objects.equals(other.getId(), match.getId()))
                .map(other -> other.getUser().getNickname())
                .collect(Collectors.joining(", "));
        return names.isEmpty() ? "—" : names;
    }

    /** {@code ordered} vai da mais antiga para a mais recente: a meta é atingida no item {@code goal}. */
    private AchievementResponse achievement(String id, String title, String description,
                                            List<MatchPlayerJpaEntity> ordered, int goal, String reward) {
        Instant unlockedAt = ordered.size() >= goal ? ordered.get(goal - 1).getGame().getUpdatedAt() : null;
        return new AchievementResponse(id, title, description, Math.min(ordered.size(), goal), goal, reward, unlockedAt);
    }

    /** Mesmo recorte de `profileUsername` no app. */
    static String username(String nickname) {
        String clean = nickname.replaceAll("[^a-zA-Z0-9_]", "");
        if (clean.length() > 20) clean = clean.substring(0, 20);
        return clean.isEmpty() ? "jogador" : clean;
    }

    private static String label(String constant) {
        if (constant.equals("AS")) return "Ás";
        String lower = constant.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
