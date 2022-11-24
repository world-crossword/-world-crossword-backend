package com.worldcrossword.puzzle.service;




import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcrossword.puzzle.dto.SessionRequestResDto;
import com.worldcrossword.puzzle.entity.PuzzleSessionEntity;
import com.worldcrossword.puzzle.repository.PuzzleSessionRepository;
import com.worldcrossword.puzzle.service.interfaces.PuzzleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PuzzleWebsocket extends TextWebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

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
    private static final ConcurrentHashMap<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<String, WebSocketSession>();

    // 접속한 유저 관리
    private final ArrayList<User> users = new ArrayList<>();

    // 생성된 퍼즐 세션 체크용.
    @Autowired
    PuzzleSessionRepository puzzleSessionRepository;
    
    // 퍼즐 생성용 코드
    @Autowired
    PuzzleService puzzleService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 최초 등록 과정
        CLIENTS.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        CLIENTS.remove(session.getId());
        // 세션 ID가 동일한 유저를 제거함.
        users.remove(users.stream().filter(i -> i.getSessionId() == session.getId()).findFirst().orElse(null));
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
            users.add(User.builder().sessionId(session.getId()).googldId(googleId).sessionName(sessionName).build());
            session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat(true).message("세션 연결됨").build())));
            return;
        }

        // 게임 가져오기 - 없으면 생성됨.
        // 보낼때 task에 getmap, sessionName에 필요한 세션 이름 보내줘야함.
        else if(parsed.get("task").equals("getmap")) {
            Optional<PuzzleSessionEntity> puzzle = puzzleSessionRepository.findBySessionName((String) parsed.get("sessionName"));
            if(!puzzle.isPresent()) {
                // 퍼즐 생성 요청
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat(false).message("퍼즐 생성중").sessionName((String) parsed.get("sessionName")).build())));
                puzzleService.generatePuzzle((String) parsed.get("sessionName"));
            } 
            // 퍼즐 생성 요청은 갔으나 아직 생성중인 경우
            else if (puzzle.get().getComplete() == false) {
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat(false).message("퍼즐 생성중").sessionName((String) parsed.get("sessionName")).build())));
            }
            // 퍼즐이 있을 경우
            else {
                session.sendMessage(new TextMessage(objToJson(SessionRequestResDto.builder().stat(true).message("퍼즐을 보냅니다").sessionName((String) parsed.get("sessionName")).build())));
                // Front에 퍼즐을 보내줘야함.
            }
        }
    }
}
