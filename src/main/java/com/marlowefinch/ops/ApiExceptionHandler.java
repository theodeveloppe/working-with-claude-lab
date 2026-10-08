package com.marlowefinch.ops;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns invalid request parameters into {@code 400 {"errors": [...]}} (TODO-232). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidParametersException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, List<String>> invalidParameters(InvalidParametersException e) {
        return Map.of("errors", e.errors());
    }
}
