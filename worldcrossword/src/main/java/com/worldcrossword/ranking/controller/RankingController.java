package com.worldcrossword.ranking.controller;

import com.worldcrossword.ranking.dto.RankingDTO;
import com.worldcrossword.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/ranking")
    public ResponseEntity<RankingDTO> getRanking() {
        return new ResponseEntity<>(rankingService.getRanking(), HttpStatus.OK);
    }
}
