package br.senai.aula.web.application.chat;

import br.senai.aula.web.infrastructure.persistence.chat.entity.ChatMessageJpaEntity;
import br.senai.aula.web.infrastructure.persistence.chat.repository.ChatMessageJpaRepository;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Chat global do lobby: mensagens persistidas, lidas por polling HTTP. */
@Service
public class ChatService {
    private static final Duration MIN_INTERVAL = Duration.ofSeconds(1);
    private final ChatMessageJpaRepository messages;

    public ChatService(ChatMessageJpaRepository messages) { this.messages = messages; }

    /** Sem {@code after}: as últimas 50. Com {@code after}: as posteriores a esse id, em ordem. */
    @Transactional(readOnly = true)
    public List<ChatMessageJpaEntity> list(Long after) {
        if (after != null) return messages.findTop100ByIdGreaterThanOrderByIdAsc(after);
        List<ChatMessageJpaEntity> latest = new ArrayList<>(messages.findTop50ByOrderByIdDesc());
        Collections.reverse(latest);
        return latest;
    }

    @Transactional
    public ChatMessageJpaEntity send(UserJpaEntity user, String text) {
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) throw new IllegalArgumentException("Digite uma mensagem");
        if (clean.length() > ChatMessageJpaEntity.MAX_LENGTH)
            throw new IllegalArgumentException("Mensagem com mais de " + ChatMessageJpaEntity.MAX_LENGTH + " caracteres");
        Instant now = Instant.now();
        messages.findTopByUserOrderByIdDesc(user).ifPresent(last -> {
            if (last.getSentAt().plus(MIN_INTERVAL).isAfter(now))
                throw new IllegalStateException("Aguarde um instante antes de enviar outra mensagem");
        });
        return messages.save(new ChatMessageJpaEntity(user, clean, now));
    }
}
