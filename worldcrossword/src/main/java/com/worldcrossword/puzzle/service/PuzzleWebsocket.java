package com.worldcrossword.puzzle.service;




import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcrossword.puzzle.dto.PuzzleRequest;
import com.worldcrossword.puzzle.dto.SessionRequestResDto;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import com.worldcrossword.puzzle.entity.UserEntity;
import com.worldcrossword.puzzle.repository.PuzzleRepository;
import com.worldcrossword.puzzle.repository.PuzzleSessionRepository;
import com.worldcrossword.puzzle.repository.UserRepository;
import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PuzzleWebsocket extends TextWebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

    // 접속한 유저 관리
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PuzzleRepository puzzleRepository;

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

    // 세션 관리
    private final List<WebSocketSession> CLIENTS = new ArrayList<>();

    // 생성된 퍼즐 세션 체크용.
    @Autowired
    PuzzleSessionRepository puzzleSessionRepository;
    
    // 퍼즐 생성용 코드
    @Autowired
    PuzzleService puzzleService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 최초 등록 과정
        CLIENTS.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        CLIENTS.remove(session);
        // 세션 ID가 동일한 유저를 제거함.
        userRepository.deleteBySessionId(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // JSON Payload 가져와서 파싱함.
        HashMap<String, Object> parsed = convertJsonToObject(message.getPayload());

        // session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat(false).build())));
        // 유저한테 메시지 다시 보내는 예시. SessionRequestResDto를 JSON으로 파싱 후 보낸다.

        // 유저가 처음 들어왔을때, 등록하는 과정 -> 반드시 필요함!! 이거 끝나면 페이지 로딩 되도록 해야함.
        if(parsed.get("task").equals("auth")) {
            String googleId = (String) parsed.get("googleId");
            String sessionName = (String) parsed.get("sessionName");
            // 등록 과정에서는 보낼때 googleId와 현재 존재하는 sessionName을 보내야 한다.
            // 이후 Users에 추가한다.
            userRepository.save(UserEntity.builder().sessionId(session.getId()).googldId(googleId).sessionName(sessionName).solving(false).build());
            session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true").message("세션 연결됨").build())));
            return;
        }

        // 게임 가져오기 - 없으면 생성됨.
        // 보낼때 task에 getmap, sessionName에 필요한 퍼즐 세션 이름 보내줘야함.
        else if(parsed.get("task").equals("getmap")) {
            Optional<PuzzleSessionEntity> puzzle = puzzleSessionRepository.findBySessionName((String) parsed.get("sessionName"));
            if(puzzle.isEmpty()) {
                // 퍼즐 생성 요청
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("퍼즐 생성 요청").sessionName((String) parsed.get("sessionName")).build())));
                puzzleService.generatePuzzle((String) parsed.get("sessionName"));
            } 
            // 퍼즐 생성 요청은 갔으나 아직 생성중인 경우
            else if (puzzle.get().getComplete() == false) {
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("퍼즐 생성중").sessionName((String) parsed.get("sessionName")).build())));
            }
            // 퍼즐이 있을 경우
            else {
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true").message("퍼즐이 있습니다").sessionName((String) parsed.get("sessionName")).build())));
                // Front에 퍼즐을 보내줘야함.
            }
        }
        
        // 유저 퍼즐 세션 변경 -> 기존 세션에서 변경시 퍼즐 받아오는 REST API 실행 전에 보내줘야함 // task는 changePuzzle , sessionName은 퍼즐 이름.
        else if(parsed.get("task").equals("changePuzzle")) {
            if(puzzleSessionRepository.findBySessionName((String) parsed.get("sessionName")).isEmpty() || !puzzleSessionRepository.findBySessionName((String) parsed.get("sessionName")).get().getComplete()) {
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("찾는 퍼즐이 없습니다.").sessionName((String) parsed.get("sessionName")).build())));
                return;
            }
            Optional<UserEntity> changingUser = userRepository.findBySessionId(session.getId());
            if(changingUser.isEmpty()) {
                return;
            }
            UserEntity user = changingUser.get();
            user.changeSession((String) parsed.get("sessionName"));
            userRepository.save(user);
            session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true").message("퍼즐 세션 변경됨").sessionName((String) parsed.get("sessionName")).build())));
        }

        // 퍼즐 풀이 시작 전파 // row, col은 문자열 형식으로, direction은 방향에 따라 ACROSS와 DOWN, sessionName은 퍼즐 세션 이름.
        else if (parsed.get("task").equals("selectPuzzle")) {
            UserEntity user = userRepository.findBySessionId(session.getId()).orElseThrow();
            Long row = Long.parseLong((String) parsed.get("row"));
            Long col = Long.parseLong((String) parsed.get("col"));
            String direction = (String) parsed.get("direction");
            List<PuzzleEntity> puzzles = puzzleRepository.findAllBySessionName((String) parsed.get("sessionName"));

            // 현재 풀려고 하는 퍼즐을 찾음.
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
            if(puzzle == null) {
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("찾는 퍼즐이 없습니다.").sessionName((String) parsed.get("sessionName")).build())));
                return;
            }

            // 유저 상태 변경 및 풀고있는 단어 설정.
            user.changeSolve(true, puzzle.getWord());
            userRepository.save(user);

            // 퍼즐 푼다고 요청한 사람에게 퍼즐의 정답을 제공함. -> 퍼즐의 정답 여부는 프론트엔드에서 확인.
            session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true").word(puzzle.getWord()).build())));

            for(WebSocketSession s: CLIENTS) {
                // 나 자신이 아니고, 현재 접속중인 유저 중 하나이면서, 퍼즐 세션이 일치하는 유저들에게 solving task로 다른사람이 푸는 문제를 전파. - 현재 풀고있는 단어와 푸는 사람의 아이디가 포함됨.
                if (!s.getId().equals(session.getId()) && userRepository.findBySessionId(s.getId()).isPresent() && userRepository.findBySessionId(s.getId()).get().getSessionName().equals((String) parsed.get("sessionName"))) {
                    session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("solving").word(puzzle.getWord()).message(user.getGoogldId()).build())));
                }
            }
        }

        // 퍼즐 풀이 그만둘때 - 위와 나머지는 동일한데, success에 "true" 혹은 "false"를 보내 성공 여부 보내줌
        else if (parsed.get("task").equals("releasePuzzle")) {
            UserEntity user = userRepository.findBySessionId(session.getId()).orElseThrow();
            Long row = Long.parseLong((String) parsed.get("row"));
            Long col = Long.parseLong((String) parsed.get("col"));
            String direction = (String) parsed.get("direction");
            String success = (String) parsed.get("success");
            List<PuzzleEntity> puzzles = puzzleRepository.findAllBySessionName((String) parsed.get("sessionName"));

            // 현재 그만두려는 퍼즐을 찾음.
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
            if(puzzle == null) {
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("false").message("찾는 퍼즐이 없습니다.").sessionName((String) parsed.get("sessionName")).build())));
                return;
            }

            // 풀이 그만둠 저장.
            user.changeSolve(false, null);
            userRepository.save(user);

            // 풀이 성공했을시 puzzle 성공 체크 및 저장.
            if(success.equals("true")) {
                puzzle.successPuzzle();
                puzzleRepository.save(puzzle);
            }

            // 퍼즐 그만두기를 요청한 사람에게 처리가 끝났음을 알림
            session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("true").message("처리 완료").build())));

            for(WebSocketSession s: CLIENTS) {
                // 나 자신이 아니고, 현재 접속중인 유저 중 하나이면서, 퍼즐 세션이 일치하는 유저들에게 not_solving task로 풀이 끝났음을 알림
                if (!s.getId().equals(session.getId()) && userRepository.findBySessionId(s.getId()).isPresent() && userRepository.findBySessionId(s.getId()).get().getSessionName().equals((String) parsed.get("sessionName"))) {
                    session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat("not_solving").word(puzzle.getWord()).build())));
                }
            }
        }


    }
}
