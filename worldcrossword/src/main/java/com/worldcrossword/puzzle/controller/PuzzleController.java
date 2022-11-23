package com.worldcrossword.puzzle.controller;

import com.worldcrossword.puzzle.service.interfaces.PuzzleSessionService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/puzzle")
public class PuzzleController {

    @Autowired
    PuzzleSessionService puzzleSessionService;

    @PostMapping("/generate/{sessionName}")
    public ResponseEntity<HttpEntity> generatePuzzle(@PathVariable String sessionName) {
        return puzzleSessionService.generatePuzzle(sessionName);
    }
}
