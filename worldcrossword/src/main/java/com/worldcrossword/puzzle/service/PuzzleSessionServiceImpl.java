package com.worldcrossword.puzzle.service;

import com.worldcrossword.puzzle.service.interfaces.PuzzleSessionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
@Slf4j
public class PuzzleSessionServiceImpl implements PuzzleSessionService {

    @Override
    public ResponseEntity<HttpEntity> generatePuzzle(String puzzleName) {
        try {
            String[] cmd = new String[] {"main.exe", puzzleName};
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File("puzzleGeneration"));
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if(exitCode == 0) return new ResponseEntity<>(HttpStatus.OK);
            else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }
    }
}
