package com.worldcrossword.puzzle.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {

    /**
     * 항상 저장되어 있는 Member와는 달리, 이 Table은 게임에 참여할때만 Row에 존재합니다.
     */


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;
    @Column(nullable = false)
    private String googldId;
    @Column(nullable = false)
    private String sessionName;

    // 현재 풀고 있는지 여부와 풀고 있을시의 위치
    @Column(nullable = false)
    private Boolean solving;

    @Column
    private String word;

    public void changeSession(String sessionName) {
        this.sessionName = sessionName;
        this.solving = false;
        this.word = null;
    }

    public void changeSolve(Boolean solving, String word) {
        this.solving = solving;
        this.word = word;
    }
}
