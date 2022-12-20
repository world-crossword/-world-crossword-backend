package com.worldcrossword.puzzle.dto;

import com.worldcrossword.puzzle.entity.PuzzleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuzzleDTO {

    private Long id;

    private String word;

    private Long row_point;

    private Long col_point;

    private Dir direction;

    private Long completion;

    private Long solver_id;

    private String sessionName;

    private enum Dir {
        ACROSS, DOWN
    }

    public PuzzleDTO(PuzzleEntity puzzle) {
        this.id = puzzle.getId();
        this.word = puzzle.getWord();
        this.row_point = puzzle.getRowpoint();
        this.col_point = puzzle.getColpoint();
        this.direction = Dir.valueOf(puzzle.getDirection());
        this.completion = puzzle.getCompletion();
        if(completion == 1) this.solver_id = puzzle.getMember().getId();
        this.sessionName = puzzle.getSessionName();
    }

    public static List<PuzzleDTO> entityToList(List<PuzzleEntity> puzzle) {
        return puzzle.stream()
                .map(PuzzleDTO::new)
                .collect(Collectors.toList());
    }
}
