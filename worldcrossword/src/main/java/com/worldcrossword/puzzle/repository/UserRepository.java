package com.worldcrossword.puzzle.repository;

import com.worldcrossword.puzzle.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    void deleteBySessionId(String sessionId);
    List<UserEntity> findBySessionName(String sessionName);
    Optional<UserEntity> findBySessionId(String sessionId);

    Optional<UserEntity> findByGoogleId(String googleId);
}
