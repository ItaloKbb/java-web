package br.senai.aula.web.infrastructure.persistence.chat.entity;

import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessageJpaEntity {
    public static final int MAX_LENGTH = 280;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;
    @Column(nullable = false, length = MAX_LENGTH)
    private String text;
    @Column(nullable = false)
    private Instant sentAt;

    protected ChatMessageJpaEntity() {}
    public ChatMessageJpaEntity(UserJpaEntity user, String text, Instant sentAt) {
        this.user = user;
        this.text = text;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public UserJpaEntity getUser() { return user; }
    public String getText() { return text; }
    public Instant getSentAt() { return sentAt; }
}
