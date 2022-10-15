package com.worldcrossword.google.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcrossword.google.dto.GoogleToken;
import com.worldcrossword.google.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoogleService {

    public GoogleToken getToken(String client_id, String client_secret, String code, String redirect_uri) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("code", code);
        param.add("client_id", client_id);
        param.add("client_secret", client_secret);
        param.add("redirect_uri", redirect_uri);
        param.add("grant_type", "authorization_code");
        HttpEntity<MultiValueMap<String, String>> requestInfo = new HttpEntity<>(param, headers);
        RestTemplate req = new RestTemplate();
        ResponseEntity<String> res = req.exchange("https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                requestInfo,
                String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(res.getBody(), GoogleToken.class);
    }

    public String getGoogleId(String accessToken) throws JsonProcessingException {
        RestTemplate req = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        ResponseEntity<String> res = req.exchange("https://www.googleapis.com/userinfo/v2/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(res.getBody(), UserInfo.class).getId();
    }

    public void cacheToken(String accessToken, String refreshToken, String googleId) {

    }

    public String refreshAccessToken(String client_id, String client_secret, String refreshToken) throws JsonProcessingException {
        RestTemplate req = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("client_id", client_id);
        param.add("client_secret", client_secret);
        param.add("refresh_token", refreshToken);
        param.add("grant_type", "refresh_token");
        HttpEntity<MultiValueMap<String, String>> requestInfo = new HttpEntity<>(param, headers);
        ResponseEntity<String> res = req.exchange("https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                requestInfo,
                String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(res.getBody(), GoogleToken.class).getAccess_token();
    }
}
