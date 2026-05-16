package com.eerdem07.mira.gateway.auth.application;

import com.eerdem07.mira.gateway.auth.application.exception.InvalidCredentialsException;
import com.eerdem07.mira.gateway.auth.application.port.in.GenerateAccessTokenCommand;
import com.eerdem07.mira.gateway.auth.application.port.in.LoginCommand;
import com.eerdem07.mira.gateway.auth.application.port.in.LoginResult;
import com.eerdem07.mira.gateway.auth.application.port.in.LoginUseCase;
import com.eerdem07.mira.gateway.auth.application.port.out.MerchantAuthPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LoginService implements LoginUseCase {
    private final MerchantAuthPort merchantAuthPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginService(MerchantAuthPort merchantAuthPort, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.merchantAuthPort = merchantAuthPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult execute(LoginCommand request) {
        var merchant = this.merchantAuthPort.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), merchant.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        UUID merchantId = merchant.getMerchantId();

        String accessToken = jwtTokenProvider.generateToken(new GenerateAccessTokenCommand(merchantId));

        return new LoginResult(accessToken);
    }


}
