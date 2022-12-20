package com.worldcrossword.puzzle.controller;

import com.worldcrossword.puzzle.dto.*;
import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import com.worldcrossword.puzzle.service.interfaces.PuzzleSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/puzzle")
public class PuzzleController {

    private final PuzzleService puzzleService;
    private final PuzzleSessionService puzzleSessionService;

    @GetMapping("/{oldSessionName}/{newSessionName}")
    public ResponseEntity<PuzzleRequest> getPuzzle(@PathVariable String oldSessionName,
                                                   @PathVariable String newSessionName,
                                                   HttpServletRequest req) {
        Long memberId = (Long) req.getAttribute("memberId");
        puzzleSessionService.loadSession(memberId, oldSessionName, newSessionName);

        PuzzleSessionEntity puzzle = puzzleSessionService.findBySessionName(newSessionName);
        if(puzzle == null) {
            puzzleService.generatePuzzle(newSessionName);
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        else if(!puzzle.getComplete()) return new ResponseEntity<>(null, HttpStatus.OK);
        return new ResponseEntity<>(PuzzleRequest.builder()
                .puzzle(PuzzleDTO.entityToList(puzzleService.getPuzzle(newSessionName)))
                .build(), HttpStatus.OK);
    }

    @GetMapping("/mean/{id}")
    public ResponseEntity<DictionaryDTO> getWord(@PathVariable String id) {
        return new ResponseEntity<>(new DictionaryDTO(puzzleService.getWord(Long.parseLong(id))), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<SolveDTO> solveWord(@RequestBody PuzzleSolveDto puzzleSolveDto,
                                              HttpServletRequest req) throws IOException {
        Long memberId = (Long) req.getAttribute("memberId");
        int solve = puzzleService.solvePuzzle(puzzleSolveDto, memberId);
        return new ResponseEntity<>(new SolveDTO(solve), HttpStatus.OK);
    }

}
