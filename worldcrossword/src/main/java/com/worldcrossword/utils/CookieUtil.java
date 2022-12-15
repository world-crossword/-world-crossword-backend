package com.worldcrossword.utils;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 서버 스펙의 브라우저 쿠키 생성/조회 보조 클래스
 *
 * @see ResponseCookie
 */
@Component
public class CookieUtil {

<<<<<<< HEAD
    /**
     * 쿠키 유효 기간(default)
     */
    private final int COOKIE_VALIDATION_SECOND = 1000 * 60 * 60 * 48;
=======
    private final int COOKIE_VALIDATION_SECOND = 3600 * 24 * 100;
>>>>>>> 762650f49c084a918894c2994d4fc5f25a199c7f

    /**
     * 서버 스펙의 브라우저 쿠키 생성
     *
     * @param name  쿠키 이름
     * @param value 쿠키 값
     */
    public ResponseCookie createCookie(String name, String value) {
		System.out.println(name);
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
<<<<<<< HEAD
                // .secure(false)
=======
                .secure(true)
>>>>>>> 762650f49c084a918894c2994d4fc5f25a199c7f
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