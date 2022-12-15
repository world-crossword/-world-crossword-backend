package com.worldcrossword.puzzle.controller;

import com.worldcrossword.puzzle.dto.PuzzleRequest;
import com.worldcrossword.puzzle.dto.PuzzleSolveDto;
import com.worldcrossword.puzzle.dto.SolveDTO;
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
        puzzleSessionService.updateSession(memberId, oldSessionName, newSessionName);
        puzzleSessionService.updateScore(memberId);

        PuzzleSessionEntity puzzle = puzzleSessionService.findBySessionName(newSessionName);
        if(puzzle == null) {
            puzzleService.generatePuzzle(newSessionName);
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        else if(puzzle.getComplete()) return new ResponseEntity<>(null, HttpStatus.OK);
        return new ResponseEntity<>(PuzzleRequest.builder()
                .puzzle(puzzleService.getPuzzle(newSessionName))
                .build(), HttpStatus.OK);
    }

    @GetMapping("/mean/{word}")
    public ResponseEntity<DictionaryEntity> getWord(@PathVariable String word) {
        try {
            return new ResponseEntity<>(puzzleService.getWord(word), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("")
    public ResponseEntity<SolveDTO> solveWord(@RequestBody PuzzleSolveDto puzzleSolveDto,
                                              HttpServletRequest req) throws IOException {
        Long memberId = (Long) req.getAttribute("memberId");
        Boolean solve = puzzleService.solvePuzzle(puzzleSolveDto, memberId);
        return new ResponseEntity<>(new SolveDTO(solve), HttpStatus.OK);
    }

}
