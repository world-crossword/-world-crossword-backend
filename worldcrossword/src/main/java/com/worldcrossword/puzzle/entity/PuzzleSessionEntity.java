package com.worldcrossword.puzzle.entity;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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

    @Column(name = "session_name", unique = true, nullable = false)
    private String sessionName;

    @Column(name="complete")
    private Boolean complete;

    public void completeGenerate() {
        this.complete = true;
    }
}
