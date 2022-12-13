package com.worldcrossword.puzzle.service.interfaces;

import com.worldcrossword.puzzle.dto.PuzzleSolveDto;
import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public interface PuzzleService {
    /**
     * 특정 이름의 퍼즐을 생성합니다.
     */
    Boolean generatePuzzle(String puzzleName);

    List<PuzzleEntity> getPuzzle(String puzzleName);

    DictionaryEntity getWord(String word);

    List<UserEntity> getUsers(String sessionName);

    Boolean solvePuzzle(PuzzleSolveDto puzzleSolveDto, String sessionName) throws IOException;
}

