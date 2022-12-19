package com.worldcrossword.puzzle.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PuzzleRequest {
    List<PuzzleDTO> puzzle;
//    List<UserEntity> user;

}
