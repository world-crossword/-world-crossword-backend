package com.worldcrossword.google.controller;

import com.worldcrossword.google.dto.GoogleToken;
import com.worldcrossword.google.dto.TokenDTO;
import com.worldcrossword.google.service.GoogleService;
import com.worldcrossword.member.service.MemberService;
import com.worldcrossword.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class GoogleController {

    @Value("${google.client-id}")
    private String client_id;
    @Value("${google.client-secret}")
    private String client_secret;
    @Value("${google.redirect-uri}")
    private String redirect_uri;

    private final GoogleService googleService;
    private final MemberService memberService;
    private final CookieUtil cookieUtil;

    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<TokenDTO> loginCallback(@RequestParam(name = "code") String code,
                                                  HttpServletResponse res) throws IOException {
        GoogleToken token = googleService.getToken(client_id, client_secret, code, redirect_uri);
        String accessToken = token.getAccess_token();
        String googleId = googleService.getGoogleId(token.getId_token());
        memberService.checkMember(googleId);
        googleService.cacheToken(accessToken, googleId);

        ResponseCookie cookie = cookieUtil.createCookie("WCW_access", accessToken);
        res.addHeader("Set-Cookie", cookie.toString());
        res.sendRedirect("http://localhost:3000");

        return new ResponseEntity<>(new TokenDTO(accessToken), HttpStatus.OK);
    }
}
