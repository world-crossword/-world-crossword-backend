package com.worldcrossword.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingDTO {
    private List<RankUser> ranking = new ArrayList<>();
    private RankUser mine;
}
