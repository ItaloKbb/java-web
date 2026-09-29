package br.senai.aula.web.infrastructure.persistence.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_sessions")
public class UserSessionJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String token;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserJpaEntity user;

    protected UserSessionJpaEntity() {}
    public UserSessionJpaEntity(String token, UserJpaEntity user) { this.token = token; this.user = user; }
    public String getToken() { return token; }
    public UserJpaEntity getUser() { return user; }
    public void replaceToken(String token) { this.token = token; }
}
