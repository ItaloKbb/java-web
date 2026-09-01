package br.senai.aula.web.application.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User " + userId + " not found");
    }
}
