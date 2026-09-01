package br.senai.aula.web.infrastructure.web.user.controller;

import br.senai.aula.web.application.port.in.CreateUserUseCase;
import br.senai.aula.web.application.port.in.GetUserCoinUseCase;
import br.senai.aula.web.domain.user.User;
import br.senai.aula.web.infrastructure.web.user.request.CreateUserRequest;
import br.senai.aula.web.infrastructure.web.user.response.CoinResponse;
import br.senai.aula.web.infrastructure.web.user.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserCoinUseCase getUserCoinUseCase;

    public UserController(CreateUserUseCase createUserUseCase, GetUserCoinUseCase getUserCoinUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getUserCoinUseCase = getUserCoinUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        User user = createUserUseCase.create(request.name(), request.email());
        return UserResponse.from(user);
    }

    @GetMapping("/{userId}/coins")
    public CoinResponse getCoins(@PathVariable Long userId) {
        return CoinResponse.from(getUserCoinUseCase.getByUserId(userId));
    }
}
