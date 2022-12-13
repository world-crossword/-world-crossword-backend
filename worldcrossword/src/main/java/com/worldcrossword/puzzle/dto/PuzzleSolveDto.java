package com.worldcrossword.puzzle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PuzzleSolveDto {
    private Long row;
    private Long col;
    private String direction;
    private String word;
    private String googleId;
}
