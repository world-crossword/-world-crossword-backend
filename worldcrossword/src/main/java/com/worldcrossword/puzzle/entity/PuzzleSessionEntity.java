package com.worldcrossword.puzzle.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "puzzle_session_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuzzleSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionName;
}
