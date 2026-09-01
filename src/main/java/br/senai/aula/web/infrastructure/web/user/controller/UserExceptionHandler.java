package br.senai.aula.web.infrastructure.web.user.controller;

import br.senai.aula.web.application.exception.UserNotFoundException;
import br.senai.aula.web.infrastructure.web.user.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleUserNotFound(UserNotFoundException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }
}
