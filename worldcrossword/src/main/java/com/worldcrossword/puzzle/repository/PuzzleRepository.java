package com.worldcrossword.puzzle.repository;

import com.worldcrossword.puzzle.entity.PuzzleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PuzzleRepository extends JpaRepository<PuzzleEntity, Long> {
    List<PuzzleEntity> findAllBySessionName(String sessionName);
    Optional<PuzzleEntity> findByRowpointAndColpoint(Long rowPoint, Long colPoint);
}
