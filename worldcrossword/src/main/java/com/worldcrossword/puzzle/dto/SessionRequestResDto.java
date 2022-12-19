package com.worldcrossword.puzzle.dto;

import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.ranking.dto.RankingDTO;
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
    private PuzzleDTO puzzle;
    private RankingDTO ranking;
    private State stat;
    private String message;
//
//    private String word;

    public enum State {
        SOLVED, NOT_SOLVED, TRUE, FALSE
    }
}
