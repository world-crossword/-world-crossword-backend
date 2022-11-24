package com.worldcrossword.puzzle.service;

import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class PuzzleServiceImpl implements PuzzleService {

    @Override
    public Boolean generatePuzzle(String puzzleName) {
        try {
            String[] cmd = new String[] {"main.exe", puzzleName};
            //리눅스 환경에서는 아래 코드로 변경 필요
            //[] cmd = new String[] {"./main.exe", puzzleName};
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File("puzzleGeneration"));
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if(exitCode == 0) return true;
            else return false;
        } catch (Exception e) {
            return false;
        }
    }
}
