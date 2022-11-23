package com.worldcrossword.puzzle.service;

import com.worldcrossword.puzzle.service.interfaces.PuzzleSessionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class PuzzleSessionServiceImpl implements PuzzleSessionService {

    private String pyPath(){
        String puzzleName = "./puzzleGeneration/main.py";
        File file = new File(puzzleName);
        return file.getAbsolutePath();
    }
    @Override
    public ResponseEntity<HttpEntity> generatePuzzle(String puzzleName) {
        log.info(pyPath());
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", pyPath(), puzzleName);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if(exitCode == 0) return new ResponseEntity<>(HttpStatus.OK);
            else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }
    }
}
