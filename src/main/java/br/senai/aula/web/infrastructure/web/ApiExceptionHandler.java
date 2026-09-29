package br.senai.aula.web.infrastructure.web;

import br.senai.aula.web.application.puzzle.exception.PuzzleNotFoundException;
import br.senai.aula.web.infrastructure.web.response.ApiErrorResponse;
import br.senai.aula.web.application.auth.UnauthorizedException;
import br.senai.aula.web.application.auth.ForbiddenException;
import org.springframework.dao.OptimisticLockingFailureException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleUnauthorized(UnauthorizedException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleForbidden(ForbiddenException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler({OptimisticLockException.class, OptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleOptimisticLock(Exception exception) {
        return new ApiErrorResponse("O estado do jogo mudou; atualize e tente novamente");
    }

    @ExceptionHandler({
            PuzzleNotFoundException.class,
            NoSuchElementException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(RuntimeException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Dados inválidos");
        return new ApiErrorResponse(message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidArgument(IllegalArgumentException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleConflict(IllegalStateException exception) {
        return new ApiErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDataConflict() {
        return new ApiErrorResponse("Já existe um registro com esses dados");
    }
}
