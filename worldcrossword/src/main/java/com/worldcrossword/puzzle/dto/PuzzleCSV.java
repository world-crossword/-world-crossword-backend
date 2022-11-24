package com.worldcrossword.puzzle.dto;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PuzzleCSV {
    private Long id;
    private String word;
    private Long row;
    private Long col;
    private Long endpoint;
    private String direction;
    private Long completion;
}
