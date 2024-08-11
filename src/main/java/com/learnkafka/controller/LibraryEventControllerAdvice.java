package com.learnkafka.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;
@Slf4j
@ControllerAdvice
public class LibraryEventControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleException(MethodArgumentNotValidException methodArgumentNotValidException){
       var errorMessage =  methodArgumentNotValidException.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error->error.getField() +" - "+ error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        log.info("error message {}",errorMessage);
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);

    }
}
