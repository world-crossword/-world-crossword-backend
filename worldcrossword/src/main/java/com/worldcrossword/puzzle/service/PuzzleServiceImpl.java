package com.worldcrossword.puzzle.service;

import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import com.worldcrossword.puzzle.repository.PuzzleSessionRepository;
import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
@EnableAsync
@Slf4j
public class PuzzleServiceImpl implements PuzzleService {

    @Autowired
    PuzzleSessionRepository puzzleSessionRepository;

    @Override
    @Async
    public Boolean generatePuzzle(String puzzleName) {
        try {
            PuzzleSessionEntity puzzle = PuzzleSessionEntity.builder().sessionName(puzzleName).complete(false).build();
            String[] cmd = new String[] {"main.exe", puzzleName};

            //리눅스 환경에서는 아래 코드로 변경 필요
            //String[] cmd = new String[] {"./main.exe", puzzleName};

            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File("puzzleGeneration"));

            puzzleSessionRepository.save(puzzle);
            Process process = processBuilder.start();

            BufferedReader stdOut = new BufferedReader( new InputStreamReader(process.getInputStream()) );
            String str;
            while( (str = stdOut.readLine()) != null ) {
                System.out.println(str);
            }

            int exitCode = process.waitFor();

            if(exitCode == 0) {
                puzzle.completeGenerate();
                puzzleSessionRepository.save(puzzle);
                // 퍼즐 세션 추가
                return true;
            }
            else return false;
        } catch (Exception e) {
            return false;
        }
    }
}
