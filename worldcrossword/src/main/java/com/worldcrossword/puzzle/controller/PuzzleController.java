package com.worldcrossword.puzzle.controller;

import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/puzzle")
public class PuzzleController {

    @Autowired
    PuzzleService puzzleService;

    @GetMapping("/{sessionName}")
    public ResponseEntity<List<PuzzleEntity>> getPuzzle(@PathVariable String sessionName) {
        return new ResponseEntity<>(puzzleService.getPuzzle(sessionName), HttpStatus.OK);
    }

    @GetMapping("/mean/{word}")
    public ResponseEntity<DictionaryEntity> getWord(@PathVariable String word) {
        try {
            return new ResponseEntity<>(puzzleService.getWord(word), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}
