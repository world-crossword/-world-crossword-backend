package com.worldcrossword.puzzle.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="puzzle")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PuzzleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String word;

    @Column
    private Long rowpoint;

    @Column
    private Long colpoint;

    @Column
    private Long endpoint;

    @Column
    private String direction;

    @Column
    private Long completion;

    @Column
    private String sessionName;
}
