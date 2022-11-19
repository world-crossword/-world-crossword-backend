package com.worldcrossword.puzzle.service.interfaces;


import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public interface PuzzleSessionService {
    /**
     * 특정 이름의 퍼즐을 생성합니다.
     */
    ResponseEntity<HttpEntity> generatePuzzle(String puzzleName);
}
