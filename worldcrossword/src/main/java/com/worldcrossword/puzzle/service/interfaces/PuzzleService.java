package com.worldcrossword.puzzle.service.interfaces;


public interface PuzzleService {
    /**
     * 특정 이름의 퍼즐을 생성합니다.
     */
    Boolean generatePuzzle(String puzzleName);

}
