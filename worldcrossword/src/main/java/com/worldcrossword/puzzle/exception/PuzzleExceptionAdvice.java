package com.worldcrossword.puzzle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PuzzleExceptionAdvice {
    @ExceptionHandler(NoEntityException.class)
    public ResponseEntity<HttpStatus> handle(NoEntityException e) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
