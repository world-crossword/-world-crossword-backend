package com.worldcrossword.google.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcrossword.google.dto.GoogleToken;
import com.worldcrossword.google.dto.UserInfo;
import com.worldcrossword.member.entity.Member;
import com.worldcrossword.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GoogleService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberRepository memberRepository;
    private ValueOperations<String, Object> redis;
    private final String RT_AT_PREFIX = "RT:AT:";
    private final String AT_GG_PREFIX = "AT:GG:";
    private final int TOKEN_CACHING_VALIDITY_DURATION = 3600 * 24 * 100;    // 100일동안 캐싱


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

    public String getGoogleId(String idToken) throws JsonProcessingException {
        RestTemplate req = new RestTemplate();
        ResponseEntity<String> res = req.exchange("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken,
                HttpMethod.GET,
                new HttpEntity<>(null),
                String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(res.getBody(), UserInfo.class).getEmail();
    }

    public void cacheToken(String accessToken, String refreshToken, String googleId) {
        redis = redisTemplate.opsForValue();
        String key_RT_AT = RT_AT_PREFIX + refreshToken;
        redis.set(key_RT_AT, accessToken);
        redisTemplate.expire(key_RT_AT, Duration.ofSeconds(TOKEN_CACHING_VALIDITY_DURATION));

        String key_AT_GG = AT_GG_PREFIX + accessToken;
        redis.set(key_AT_GG, googleId);
        redisTemplate.expire(key_AT_GG, Duration.ofSeconds(TOKEN_CACHING_VALIDITY_DURATION));
    }

    public String getAccessTokenFromCache(String refreshToken) {
        redis = redisTemplate.opsForValue();
        String key = RT_AT_PREFIX + refreshToken;
        String accessToken = (String) redis.get(key);
        return accessToken;
    }

    public boolean validateToken(String refreshToken) {
        redis = redisTemplate.opsForValue();
        String key = RT_AT_PREFIX + refreshToken;
        return redis.get(key) != null;
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

    public Long findMemberIdByAccessToken(String token) {
        redis = redisTemplate.opsForValue();
        String key_AT_GG = AT_GG_PREFIX + token.replace("Bearer ", "");
        String googleId = (String) redis.get(key_AT_GG);
        if(googleId == null) throw new RuntimeException("만료된 토큰입니다.");
        Member member = memberRepository.findByGoogleId(googleId).orElseThrow(
                () -> new RuntimeException("존재하지 않는 회원입니다.")
        );
        return member.getId();
    }
}
