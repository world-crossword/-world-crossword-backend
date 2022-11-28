package com.worldcrossword.utils;

import lombok.RequiredArgsConstructor;

public class ApiUtil {

    @RequiredArgsConstructor
    public static class SuccessResponse<T> {
        private final T data;
    }

    @RequiredArgsConstructor
    public static class ErrorResponse {
        private final int errorCode;
        private final String message;
    }
}
