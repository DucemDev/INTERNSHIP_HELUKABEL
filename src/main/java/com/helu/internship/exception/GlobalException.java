package com.helu.internship.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, Exception exception) {
        return "error";
    }
}
