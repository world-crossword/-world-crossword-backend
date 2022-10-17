package com.worldcrossword.utils;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private final int COOKIE_VALIDITY_DURATION = 3600 * 24 * 100;

    public ResponseCookie createCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .secure(true)
                .sameSite("None")   // 동일 사이트와 크로스 사이트에 모두 쿠키 전송 가능
                .maxAge(COOKIE_VALIDITY_DURATION)
                .build();
    }
}
