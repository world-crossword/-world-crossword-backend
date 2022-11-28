package com.worldcrossword.utils;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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

    public ResponseCookie getCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(name)) return ResponseCookie.from(name, cookie.getValue()).build();
        }
        return null;    // exception 추가 해야함
    }
}
