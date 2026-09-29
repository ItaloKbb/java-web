package br.senai.aula.web.application.auth;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}
