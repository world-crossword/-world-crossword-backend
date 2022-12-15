package com.worldcrossword.puzzle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Data
public class PuzzleSolveDto {
    private Long id;
//    private Long row;
//    private Long col;
//    private String direction;
    private String word;
    private String sessionName;
//    private String googleId;
}
