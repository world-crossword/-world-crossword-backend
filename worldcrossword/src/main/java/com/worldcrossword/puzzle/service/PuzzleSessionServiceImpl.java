package com.worldcrossword.puzzle.service;

import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import com.worldcrossword.puzzle.repository.PuzzleSessionRepository;
import com.worldcrossword.puzzle.service.interfaces.PuzzleSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class PuzzleSessionServiceImpl implements PuzzleSessionService {

    private final PuzzleSessionRepository puzzleSessionRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final String MemberToSessionPrefix = "MI:SN:";
    private final String SessionNameToSessionIdsPrefix = "SN:SI:";
    private final int USER_SESSION_DURATION = 3600 * 24 * 100;

    @Override
    public PuzzleSessionEntity findBySessionName(String sessionName) {
        return puzzleSessionRepository.findBySessionName(sessionName)
                .orElse(null);
    }

    @Override
    public void loadSession(Long memberId, String oldSessionName, String newSessionName) {
        String key = MemberToSessionPrefix + memberId;
        redisTemplate.opsForValue().set(key, newSessionName);
        redisTemplate.expire(key, Duration.ofSeconds(USER_SESSION_DURATION));

        if(!redisTemplate.opsForValue().get(key).equals(newSessionName)) throw new RuntimeException("세션 이름 업데이트 실패");

        if(PuzzleWebsocket.sessionID == null) throw new RuntimeException("웹소켓 연결되지 않음");
        SetOperations<String, Object> redisSet = redisTemplate.opsForSet();
        if(!oldSessionName.equals("0")) {
            String keyOld = SessionNameToSessionIdsPrefix + oldSessionName;
            redisSet.remove(keyOld, PuzzleWebsocket.sessionID);
        }

        String keyNew = SessionNameToSessionIdsPrefix + newSessionName;
        redisSet.add(keyNew, PuzzleWebsocket.sessionID);

        // 예외처리
    }

}
