package com.worldcrossword.config;


import com.worldcrossword.puzzle.service.PuzzleWebsocket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class websocketConfig implements WebSocketConfigurer {

    private final PuzzleWebsocket puzzleWebsocket;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(puzzleWebsocket, "/puzzle")
                .setAllowedOriginPatterns("*");
    }
}
