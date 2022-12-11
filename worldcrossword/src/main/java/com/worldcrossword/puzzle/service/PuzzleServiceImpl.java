package com.worldcrossword.puzzle.service;

import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import com.worldcrossword.puzzle.entity.UserEntity;
import com.worldcrossword.puzzle.repository.DictionaryRepository;
import com.worldcrossword.puzzle.repository.PuzzleRepository;
import com.worldcrossword.puzzle.repository.PuzzleSessionRepository;
import com.worldcrossword.puzzle.repository.UserRepository;
import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
@EnableAsync
@Slf4j
public class PuzzleServiceImpl implements PuzzleService {

    @Autowired
    PuzzleSessionRepository puzzleSessionRepository;

    @Autowired
    DictionaryRepository dictionaryRepository;

    @Autowired
    PuzzleRepository puzzleRepository;

    @Autowired
    UserRepository userRepository;

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
                ClassPathResource resource = new ClassPathResource("puzzleData/"+puzzleName+".csv");
                File csv = resource.getFile();
                BufferedReader br = null;
                String line = "";

                try {
                    br = new BufferedReader(new FileReader(csv));
                    while ((line = br.readLine()) != null) { // readLine()은 파일에서 개행된 한 줄의 데이터를 읽어온다.
                        String[] lineArr = line.split(","); // 파일의 한 줄을 ,로 나누어 배열에 저장 후 리스트로 변환한다.
                        if(lineArr[2].equals("row")) continue;
                        PuzzleEntity singleword = PuzzleEntity.builder()
                                .word(lineArr[1])
                                .rowpoint(Long.parseLong(lineArr[2]))
                                .colpoint(Long.parseLong(lineArr[3]))
                                .endpoint(Long.parseLong(lineArr[4]))
                                .direction(lineArr[5])
                                .completion(Long.parseLong(lineArr[6]))
                                .sessionName(puzzleName)
                                .build();
                        log.info(singleword.toString());
                        puzzleRepository.save(singleword);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (br != null) {
                            br.close(); // 사용 후 BufferedReader를 닫아준다.
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
            else return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<PuzzleEntity> getPuzzle(String puzzleName) {
        List<PuzzleEntity> puzzles = puzzleRepository.findAllBySessionName(puzzleName);
        return puzzles;
    }

    @Override
    public DictionaryEntity getWord(String word) {
        DictionaryEntity ans = dictionaryRepository.findByEnglish(word).orElseThrow();
        return ans;
    }

    @Override
    public List<UserEntity> getUsers(String sessionName) {
        List<UserEntity> users = userRepository.findBySessionName(sessionName);
        return users;
    }


}
