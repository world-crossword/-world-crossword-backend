package com.worldcrossword.puzzle.service.interfaces;


import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;

public interface PuzzleService {
    /**
     * 특정 이름의 퍼즐을 생성합니다.
     */
    Boolean generatePuzzle(String puzzleName);

    List<PuzzleEntity> getPuzzle(String puzzleName);

    DictionaryEntity getWord(String word);

}
