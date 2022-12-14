package com.worldcrossword.google.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worldcrossword.google.dto.GoogleToken;
import com.worldcrossword.google.dto.TokenDTO;
import com.worldcrossword.google.service.GoogleService;
import com.worldcrossword.member.service.MemberService;
import com.worldcrossword.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.worldcrossword.utils.ApiUtil.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public SuccessResponse<TokenDTO> loginCallback(@RequestParam(name = "code") String code,
                                                   HttpServletResponse res) throws JsonProcessingException {
        GoogleToken token = googleService.getToken(client_id, client_secret, code, redirect_uri);
        String accessToken = token.getAccess_token();
        String refreshToken = token.getRefresh_token();
        String googleId = googleService.getGoogleId(token.getId_token());

        memberService.checkMember(googleId);
        googleService.cacheToken(accessToken, refreshToken, googleId);

        ResponseCookie cookie = cookieUtil.createCookie("WCW_refresh", refreshToken);
        res.setHeader("Set-Cookie", cookie.toString());

        return new SuccessResponse<>(new TokenDTO(accessToken, refreshToken));
    }

    @GetMapping("/token")
    public SuccessResponse<TokenDTO> refreshToken(HttpServletRequest req,
                                                  HttpServletResponse res) throws JsonProcessingException {
        ResponseCookie cookie = cookieUtil.getCookie(req, "WCW_refresh");
        String refreshToken = cookie.getValue();
        String accessToken;
        if(!googleService.validateToken(refreshToken)) {
            accessToken = googleService.refreshAccessToken(client_id, client_secret, refreshToken);
            googleService.cacheToken(accessToken, refreshToken, googleService.getGoogleId(accessToken));
        } else accessToken = googleService.getAccessTokenFromCache(refreshToken);
        ResponseCookie newCookie = cookieUtil.createCookie("WCW_refresh", refreshToken);
        res.setHeader("Set-Cookie", newCookie.toString());

        return new SuccessResponse<>(new TokenDTO(accessToken, refreshToken));
    }
}
