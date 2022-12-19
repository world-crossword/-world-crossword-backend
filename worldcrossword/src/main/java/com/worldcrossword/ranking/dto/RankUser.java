package com.worldcrossword.ranking.dto;

import com.worldcrossword.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankUser {

    private Integer rank;
    private Ranker ranker;
    private Double score;

    @AllArgsConstructor
	@Data
    private static class Ranker {
        private Long id;
        private String username;
    }

    public RankUser(Integer rank, Member member, Double score) {
        this.rank = rank;
        this.ranker = new Ranker(member.getId(), member.getUsername());
        this.score = score;
    }
}
