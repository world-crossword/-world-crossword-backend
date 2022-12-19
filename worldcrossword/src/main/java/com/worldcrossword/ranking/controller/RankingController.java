package com.worldcrossword.ranking.controller;

import com.worldcrossword.ranking.dto.RankingDTO;
import com.worldcrossword.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/ranking")
    public ResponseEntity<RankingDTO> getRanking(HttpServletRequest req) {
//        Long memberId = (Long) req.getAttribute("memberId");
        Long memberId = 1L;
        return new ResponseEntity<>(rankingService.getRanking(memberId), HttpStatus.OK);
    }
}
