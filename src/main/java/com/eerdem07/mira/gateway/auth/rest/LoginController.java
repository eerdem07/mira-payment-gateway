package com.eerdem07.mira.gateway.auth.rest;

import com.eerdem07.mira.gateway.auth.application.port.in.LoginCommand;
import com.eerdem07.mira.gateway.auth.application.port.in.LoginUseCase;
import com.eerdem07.mira.gateway.auth.rest.dto.LoginRequest;
import com.eerdem07.mira.gateway.auth.rest.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/login")
public class LoginController {
    private final LoginUseCase loginUseCase;

    public LoginController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = this.loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        LoginResponse response = new LoginResponse(result.accessToken());
        return ResponseEntity.ok(response);
    }
}
