package com.worldcrossword.utils;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Component
public class CookieUtil {

    private final int COOKIE_VALIDATION_SECOND = 3600 * 24 * 100;

    public ResponseCookie createCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .secure(true)
                .sameSite("None")
                .maxAge(COOKIE_VALIDATION_SECOND)
                .build();
    }

    public ResponseCookie getCookie(HttpServletRequest req, String name) {
        Cookie[] findCookies = req.getCookies();
        if (findCookies == null) {
            throw new RuntimeException("전달된 쿠키가 없습니다.");
        }
        for (Cookie cookie : findCookies) {
            if (cookie.getName().equals(name)) {
                return ResponseCookie.from(name, cookie.getValue()).build();
            }
        }

        throw new RuntimeException("쿠키를 찾을 수 없습니다.");
    }
}