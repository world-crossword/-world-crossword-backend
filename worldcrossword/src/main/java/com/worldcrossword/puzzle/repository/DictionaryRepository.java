package com.worldcrossword.puzzle.repository;


import com.worldcrossword.puzzle.entity.DictionaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DictionaryRepository extends JpaRepository<DictionaryEntity, Long> {
    Optional<DictionaryEntity> findByEnglish(String english);
}
