package com.worldcrossword.puzzle.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcrossword.puzzle.dto.PuzzleSolveDto;
import com.worldcrossword.puzzle.dto.SessionRequestResDto;
import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import com.worldcrossword.puzzle.entity.UserEntity;
import com.worldcrossword.puzzle.exception.NoEntityException;
import com.worldcrossword.puzzle.repository.DictionaryRepository;
import com.worldcrossword.puzzle.repository.PuzzleRepository;
import com.worldcrossword.puzzle.repository.PuzzleSessionRepository;
import com.worldcrossword.puzzle.repository.UserRepository;
import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@EnableAsync
@Slf4j
public class PuzzleServiceImpl implements PuzzleService {

    private final ObjectMapper objectMapper;

    public String objToJson(SessionRequestResDto obj) {

        try {
            String objJackson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            return objJackson;
        } catch (JsonProcessingException e) {
            log.debug("failed conversion: Pfra object to Json", e);
        }

        return "";
    }

    public static HashMap<String, Object> convertJsonToObject(String json) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<HashMap<String, Object>>() {
        };
        return objectMapper.readValue(json, typeReference);

    }

    private final PuzzleSessionRepository puzzleSessionRepository;
    private final DictionaryRepository dictionaryRepository;
    private final PuzzleRepository puzzleRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    public Boolean generatePuzzle(String puzzleName) {
        try {
            PuzzleSessionEntity puzzle = PuzzleSessionEntity.builder().sessionName(puzzleName).complete(false).build();
            // String[] cmd = new String[] {"main.exe", puzzleName};

            //리눅스 환경에서는 아래 코드로 변경 필요
            String[] cmd = new String[] {"./main", puzzleName};

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
                String line = "";

                try(BufferedReader br = new BufferedReader(new FileReader(csv))) {
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
                } catch (IOException e) {
                    e.printStackTrace();
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
        if(puzzles.size() == 0) {
            throw new NoEntityException("해당하는 퍼즐이 없습니다.");
        }
        return puzzles;
    }

    @Override
    public DictionaryEntity getWord(String word) {
        Optional<DictionaryEntity> ans = dictionaryRepository.findByEnglish(word);
        if(ans.isEmpty()) {
            throw new NoEntityException("해당하는 단어가 없습니다.");
        }
        return ans.get();
    }

    @Override
    public List<UserEntity> getUsers(String sessionName) {
        List<UserEntity> users = userRepository.findBySessionName(sessionName);
        if(users.size() == 0) {
            throw new NoEntityException("해당하는 유저가 없습니다.");
        }
        return users;
    }

    @Override
    public Boolean solvePuzzle(PuzzleSolveDto puzzleSolveDto, String sessionName) throws IOException {
        // 세션에 들어있는 퍼즐 전부 찾아옴.
        List<PuzzleEntity> puzzles = puzzleRepository.findAllBySessionName(sessionName);
        UserEntity user = userRepository.findByGoogleId(puzzleSolveDto.getGoogleId()).orElseThrow();

        Long row = puzzleSolveDto.getRow();
        Long col = puzzleSolveDto.getCol();
        String direction = puzzleSolveDto.getDirection();

        // 보내준 위치에 해당하는 퍼즐을 찾아옴.
        PuzzleEntity puzzle = null;
        for(PuzzleEntity p: puzzles) {
            if(Objects.equals(direction, "ACROSS")) {
                if(p.getColpoint() <= col && p.getColpoint() + p.getEndpoint() > col && Objects.equals(p.getRowpoint(), row)) {
                    puzzle = p;
                    break;
                }
            } else {
                if(p.getRowpoint() <= row && p.getRowpoint() + p.getEndpoint() > row && Objects.equals(p.getColpoint(), col)) {
                    puzzle = p;
                    break;
                }
            }
        }

        // 퍼즐이 없으면 푸려고 시도한 사람에게 없다고 보냄.
        if(puzzle == null) {
            for(int i = 0;i < PuzzleWebsocket.CLIENTS.size();i++) {
                if(PuzzleWebsocket.CLIENTS.get(i).getId().equals(user.getSessionId())) {
                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("찾는 퍼즐이 없습니다.").sessionName(sessionName).build())));
                }
            }
            return false;
        }

        if(puzzle.getWord().equals(puzzleSolveDto.getWord())) {
            puzzle.successPuzzle();
            puzzleRepository.save(puzzle);

            user.changeSolve(false, null);
            userRepository.save(user);

            // 나 자신이 아니고, 현재 접속중인 유저 중 하나이면서, 퍼즐 세션이 일치하는 유저들에게 not_solving task로 풀이 끝났음을 알림
            // 본인에게도 정답임을 알림.
            for(int i = 0;i < PuzzleWebsocket.CLIENTS.size();i++) {
                if (PuzzleWebsocket.CLIENTS.get(i).getId().equals(user.getSessionId())) {
                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true").message("정답입니다.").build())));
                } else if (userRepository.findBySessionId(PuzzleWebsocket.CLIENTS.get(i).getId()).isPresent() && userRepository.findBySessionId(PuzzleWebsocket.CLIENTS.get(i).getId()).get().getSessionName().equals(sessionName)) {
                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true_answer").word(puzzle.getWord()).build())));
                }
            }
            return true;
        } else {
            for(int i = 0;i < PuzzleWebsocket.CLIENTS.size();i++) {
                if (PuzzleWebsocket.CLIENTS.get(i).getId().equals(user.getSessionId())) {
                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("오답입니다.").build())));
                } else if (userRepository.findBySessionId(PuzzleWebsocket.CLIENTS.get(i).getId()).isPresent() && userRepository.findBySessionId(PuzzleWebsocket.CLIENTS.get(i).getId()).get().getSessionName().equals(sessionName)) {
                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false_answer").word(puzzle.getWord()).build())));
                }
            }
            return false;
        }
    }


}
