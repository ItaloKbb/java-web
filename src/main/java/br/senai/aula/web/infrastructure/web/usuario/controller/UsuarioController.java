package br.senai.aula.web.infrastructure.web.usuario.controller;

import br.senai.aula.web.application.usuario.port.in.CriarUsuarioUseCase;
import br.senai.aula.web.domain.usuario.Usuario;
import br.senai.aula.web.infrastructure.web.usuario.request.CriarUsuarioRequest;
import br.senai.aula.web.infrastructure.web.usuario.response.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final CriarUsuarioUseCase criarUsuarioUseCase;

    public UsuarioController(CriarUsuarioUseCase criarUsuarioUseCase) {
        this.criarUsuarioUseCase = criarUsuarioUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody CriarUsuarioRequest request) {
        Usuario usuario = criarUsuarioUseCase.criar(request.nome(), request.email());
        return UsuarioResponse.de(usuario);
    }
}
