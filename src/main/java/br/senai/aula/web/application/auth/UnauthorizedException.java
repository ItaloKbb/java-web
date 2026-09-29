package br.senai.aula.web.application.auth;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}
