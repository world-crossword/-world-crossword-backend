package com.worldcrossword.puzzle.repository;

import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PuzzleSessionRepository extends JpaRepository<PuzzleSessionEntity, Long> {
}
