package com.worldcrossword.puzzle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolveDTO {

    private State solved;

    private enum State {
        RIGHT, WRONG, ALREADY
    }

    public SolveDTO(int solve) {
        switch (solve) {
            case -1 : this.solved = State.ALREADY; break;
            case 0 : this.solved = State.WRONG; break;
            case 1 : this.solved = State.RIGHT;
        }
    }
}