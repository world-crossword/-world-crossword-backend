package com.worldcrossword.puzzle.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcrossword.member.entity.Member;
import com.worldcrossword.member.repository.MemberRepository;
import com.worldcrossword.member.service.MemberService;
import com.worldcrossword.puzzle.dto.PuzzleDTO;
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
import com.worldcrossword.ranking.dto.RankingDTO;
import com.worldcrossword.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.time.Duration;
import java.util.*;


@Service
@RequiredArgsConstructor
@EnableAsync
@Slf4j
@Transactional
public class PuzzleServiceImpl implements PuzzleService {

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Object> redisTemplate;
    private final String MemberToSessionPrefix = "MI:SN:";
    private final String RankingKey = "RANK:";
    private final String SessionNameToSessionIds = "SN:SIs:";
    private final int USER_SESSION_DURATION = 3600 * 24 * 100;
    private final int USER_SCORE_DURATION = 3600 * 24 * 100;

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
    private final MemberRepository memberRepository;
    private final RankingService rankingService;

    @Override
    @Async
    public Boolean generatePuzzle(String puzzleName) {
        try {

            ClassPathResource resource = new ClassPathResource("puzzleData/"+puzzleName+".csv");
            int exitCode = 0;
            PuzzleSessionEntity puzzle = PuzzleSessionEntity.builder().sessionName(puzzleName).complete(false).build();

            if(!resource.exists()) {
                // String[] cmd = new String[] {"main.exe", puzzleName};

                //????????? ??????????????? ?????? ????????? ?????? ??????
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

                exitCode = process.waitFor();
            }

            if(exitCode == 0) {

                File csv = resource.getFile();
                log.info("parsing");

                puzzle.completeGenerate();
                puzzleSessionRepository.save(puzzle);
                // ?????? ?????? ??????
                String line = "";

                try(BufferedReader br = new BufferedReader(new FileReader(csv))) {
                    while ((line = br.readLine()) != null) { // readLine()??? ???????????? ????????? ??? ?????? ???????????? ????????????.
                        String[] lineArr = line.split(","); // ????????? ??? ?????? ,??? ????????? ????????? ?????? ??? ???????????? ????????????.
                        if(lineArr[2].equals("row")) continue;
                        PuzzleEntity singleword = PuzzleEntity.builder()
                                .word(lineArr[1])
                                .rowpoint(Long.parseLong(lineArr[2]))
                                .colpoint(Long.parseLong(lineArr[3]))
                                .endpoint(Long.parseLong(lineArr[4]))
                                .direction(lineArr[5])
                                .completion(Long.parseLong(lineArr[6]))
                                .sessionName(puzzleName)
								.dictionary(dictionaryRepository.findByEnglish(lineArr[1]).get())
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
            throw new NoEntityException("???????????? ????????? ????????????.");
        }
        return puzzles;
    }

    @Override
    public PuzzleEntity getWord(Long puzzleId) {
        return puzzleRepository.findById(puzzleId).orElseThrow(() -> new RuntimeException("????????? ????????????."));
    }

    @Override
    public List<UserEntity> getUsers(String sessionName) {
        List<UserEntity> users = userRepository.findBySessionName(sessionName);
        if(users.size() == 0) {
            throw new NoEntityException("???????????? ????????? ????????????.");
        }
        return users;
    }

    @Override
    public int solvePuzzle(PuzzleSolveDto puzzleSolveDto, Long memberId) throws IOException {
        // ????????? ???????????? ?????? ?????? ?????????.
        /*List<PuzzleEntity> puzzles = puzzleRepository.findAllBySessionName(sessionName);
        UserEntity user = userRepository.findByGoogleId(puzzleSolveDto.getGoogleId()).orElseThrow();

        Long row = puzzleSolveDto.getRow();
        Long col = puzzleSolveDto.getCol();
        String direction = puzzleSolveDto.getDirection();

        // ????????? ????????? ???????????? ????????? ?????????.
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
        }*/

        PuzzleEntity puzzle = puzzleRepository.findById(puzzleSolveDto.getId())
                .orElseThrow(() -> new RuntimeException("????????? ????????????."));
        if(puzzle.getCompletion() == 1L) return -1;

        // ????????? ????????? ????????? ????????? ???????????? ????????? ??????.
        /*if(puzzle == null) {
            for(int i = 0;i < PuzzleWebsocket.CLIENTS.size();i++) {
                if(PuzzleWebsocket.CLIENTS.get(i).getId().equals(user.getSessionId())) {
                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("?????? ????????? ????????????.").sessionName(sessionName).build())));
                }
            }
            return false;
        }*/

        if(puzzle.getWord().equals(puzzleSolveDto.getWord())) {
//            String MI_SI_key = MemberToSessionPrefix + memberId;
//            String sessionName = (String) Optional.ofNullable(redisTemplate.opsForValue().get(MI_SI_key))
//                            .orElseThrow(() -> new RuntimeException("????????? ????????????."));
//            String SN_SIs_key = SessionNameToSessionIds + sessionName;
//            Set<Object> sessionIds = Optional.ofNullable(redisTemplate.opsForSet().members(SN_SIs_key))
//                    .orElseThrow(() -> new RuntimeException("?????? id??? ???????????? ??? ??????????????????."));

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("???????????? ?????? ???????????????."));

            rankingService.incrementScore(memberId, 100);
            puzzle.successPuzzle(member);
//            puzzleRepository.save(puzzle); // ????????? ???????????? ?????? permit ??? ?????? ?????? ??????????????? ???????????? ???????????? ????????????????????? ?????? ????????????.

//            user.changeSolve(false, null);
//            userRepository.save(user);

            // ??? ????????? ?????????, ?????? ???????????? ?????? ??? ???????????????, ?????? ????????? ???????????? ??????????????? not_solving task??? ?????? ???????????? ??????
            // ??????????????? ???????????? ??????.
//            for(int i = 0;i < PuzzleWebsocket.CLIENTS.size();i++) {
//                if (PuzzleWebsocket.CLIENTS.get(i).getId().equals(sessionId)) {
//                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true").message("???????????????.").build())));
//                } else if (userRepository.findBySessionId(PuzzleWebsocket.CLIENTS.get(i).getId()).isPresent() && userRepository.findBySessionId(PuzzleWebsocket.CLIENTS.get(i).getId()).get().getSessionName().equals(puzzleSolveDto.getSessionName())) {
//                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true_answer").word(puzzle.getWord()).build())));
//                }
//            }

            for(WebSocketSession user : PuzzleWebsocket.CLIENTS) {
                user.sendMessage(
                    new TextMessage(objToJson(
                        SessionRequestResDto.builder()
                                .puzzle(new PuzzleDTO(puzzle))
                                .ranking(rankingService.getRanking(memberId))
                                .stat(SessionRequestResDto.State.SOLVED)
                                .build()
                    ))
                );
            }

            return 1;
        }
//        else {
//            for(int i = 0;i < PuzzleWebsocket.CLIENTS.size();i++) {
//                if (PuzzleWebsocket.CLIENTS.get(i).getId().equals(user.getSessionId())) {
//                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("???????????????.").build())));
//                } else if (userRepository.findBySessionId(PuzzleWebsocket.CLIENTS.get(i).getId()).isPresent() && userRepository.findBySessionId(PuzzleWebsocket.CLIENTS.get(i).getId()).get().getSessionName().equals(sessionName)) {
//                    PuzzleWebsocket.CLIENTS.get(i).sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false_answer").word(puzzle.getWord()).build())));
//                }
//            }
            return 0;
//        }
    }


}
