package br.senai.aula.web.infrastructure.persistence.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String codeHash;

    private String codeSalt;

    @Column(nullable = false)
    private Integer rankingPoints = 0;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UserJpaEntity(String nickname, String codeHash, String codeSalt) {
        this.name = nickname;
        this.email = nickname.toLowerCase() + "@truno.local";
        this.nickname = nickname;
        this.codeHash = codeHash;
        this.codeSalt = codeSalt;
        this.rankingPoints = 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() { return nickname == null ? name : nickname; }
    public String getCodeHash() { return codeHash; }
    public String getCodeSalt() { return codeSalt; }
    public Integer getRankingPoints() { return rankingPoints; }
    public void addRankingPoints(int points) { this.rankingPoints += points; }
}
