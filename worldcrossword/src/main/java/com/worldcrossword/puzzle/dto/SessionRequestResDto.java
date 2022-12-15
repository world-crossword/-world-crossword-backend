package com.worldcrossword.puzzle.dto;

import com.worldcrossword.puzzle.entity.PuzzleEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SessionRequestResDto {
    // true, false, solving, not_solving
//    private String stat;
//    private String sessionName;
    private PuzzleEntity puzzle;
    private State stat;
    private String message;
//
//    private String word;

    public enum State {
        SOLVED, NOT_SOLVED, TRUE, FALSE
    }
}
