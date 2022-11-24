package com.worldcrossword.puzzle.controller;

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

    @GetMapping("/get/{sessionName}")
    public ResponseEntity<List<PuzzleEntity>> getPuzzle(@PathVariable String sessionName) {
        return new ResponseEntity<>(puzzleService.getPuzzle(sessionName), HttpStatus.OK);
    }


}
