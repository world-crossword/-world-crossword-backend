package com.worldcrossword.puzzle.dto;

import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.entity.UserEntity;

import java.util.List;

public class PuzzleRequest {
    List<PuzzleEntity> puzzle;
    List<UserEntity> user;
}
