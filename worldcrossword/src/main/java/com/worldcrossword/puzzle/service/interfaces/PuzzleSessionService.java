package com.worldcrossword.puzzle.service.interfaces;

import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface PuzzleSessionService {

    public PuzzleSessionEntity findBySessionName(String sessionName);
    public void updateSession(Long memberId, String oldSessionName, String newSessionName);
    public void updateScore(Long memberId);
}
