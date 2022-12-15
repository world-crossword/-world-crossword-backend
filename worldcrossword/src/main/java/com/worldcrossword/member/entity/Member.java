package com.worldcrossword.member.entity;

import lombok.*;
import javax.persistence.*;
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="username", nullable = false)
    private String username;
    @Column(name="google_id")
    private String googleId;
    @Column(name="role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name="score", nullable = false)
    private Long score;

    @Builder
    public Member(String username, String googleId, Role role, Long score) {
        this.username = username;
        this.googleId = googleId;
        this.role = role;
        this.score = score;
    }
}
