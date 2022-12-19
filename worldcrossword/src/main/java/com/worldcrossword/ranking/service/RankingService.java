package com.worldcrossword.ranking.service;

import com.worldcrossword.member.entity.Member;
import com.worldcrossword.member.service.MemberService;
import com.worldcrossword.ranking.dto.RankUser;
import com.worldcrossword.ranking.dto.RankingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String RankingKey = "RANK";

    private final MemberService memberService;


    public void incrementScore(Long memberId) {
        ZSetOperations<String, Object> zSet = redisTemplate.opsForZSet();
        // 없다는 것은 처음 로그인한 유저라는 것
//        zSet.addIfAbsent(RankingKey, memberId, 0);
        zSet.incrementScore(RankingKey, memberId, 100);

        // 예외처리
    }

    public RankingDTO getRanking(Long memberId) {
        ZSetOperations<String, Object> zSet = redisTemplate.opsForZSet();
        RankingDTO rankingDTO = new RankingDTO();

        Member me = memberService.findById(memberId);
        Long myRank = Optional.ofNullable(zSet.reverseRank(RankingKey, memberId)).orElseThrow(() -> new RuntimeException("redis 연결 실패"));
        Double myScore = Optional.ofNullable(zSet.score(RankingKey, memberId)).orElseThrow(() -> new RuntimeException("redis 연결 실패"));
        rankingDTO.setMine(new RankUser(myRank+1, me, myScore));

        Long size = Optional.ofNullable(zSet.size(RankingKey)).orElseThrow(() -> new RuntimeException("redis 연결 실패"));
        Set<ZSetOperations.TypedTuple<Object>> rankSet;
        if(size > 0 && size < 5) rankSet = Optional.ofNullable(zSet.reverseRangeWithScores(RankingKey, 0, size-1)).orElseThrow(() -> new RuntimeException("redis 연결 실패"));
        else rankSet= Optional.ofNullable(zSet.reverseRangeWithScores(RankingKey, 0, 4))
                .orElseThrow(() -> new RuntimeException("redis 연결 실패"));
        if(rankSet != null) {
            long i = 1L;
            for (ZSetOperations.TypedTuple<Object> rank : rankSet) {
                Member member = memberService.findById((Long.valueOf((Integer) rank.getValue())));
                if(member != null) rankingDTO.getRanking().add(new RankUser(i++, member, rank.getScore()));
                else zSet.remove(RankingKey, rank.getValue());
            }
        }
        return rankingDTO;
    }
}
