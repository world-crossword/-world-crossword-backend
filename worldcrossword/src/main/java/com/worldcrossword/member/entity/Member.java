package com.worldcrossword.member.entity;

import lombok.*;
import javax.persistence.*;
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue
    private Long id;

    private String username;
    private String googleId;
    @Enumerated(EnumType.STRING)
    private Role role;

    private Long score;

    @Builder
    public Member(String username, String googleId, Role role, Long score) {
        this.username = username;
        this.googleId = googleId;
        this.role = role;
        this.score = score;
    }
}
