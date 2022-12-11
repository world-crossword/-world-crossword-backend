package com.worldcrossword.puzzle.controller;

import com.worldcrossword.puzzle.dto.PuzzleRequest;
import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/puzzle")
public class PuzzleController {

    private final PuzzleService puzzleService;

    @GetMapping("/{sessionName}")
    public ResponseEntity<List<PuzzleEntity>> getPuzzle(@PathVariable String sessionName) {
        return new ResponseEntity<>(puzzleService.getPuzzle(sessionName), HttpStatus.OK);
    }

    @GetMapping("/mean/{word}")
    public ResponseEntity<DictionaryEntity> getMean(@PathVariable String word) {
        try {
            return new ResponseEntity<>(puzzleService.getWord(word), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}
