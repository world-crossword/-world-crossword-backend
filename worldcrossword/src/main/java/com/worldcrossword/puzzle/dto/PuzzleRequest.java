package com.worldcrossword.puzzle.dto;

import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.entity.UserEntity;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PuzzleRequest {
    List<PuzzleEntity> puzzle;
    List<UserEntity> user;
}
