package com.eerdem07.mira.gateway.merchants.application;

import com.eerdem07.mira.gateway.merchants.application.exception.PasswordsDoNotMatchException;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.Merchant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class RegisterMerchantService implements RegisterMerchantUseCase {
    private final MerchantRepositoryPort merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public RegisterMerchantService(MerchantRepositoryPort merchantRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RegisterMerchantResult execute(RegisterMerchantCommand command) {
        if (!Objects.equals(command.password(), command.passwordConfirm())) {
            throw new PasswordsDoNotMatchException("Password do not match!");
        }
        UUID id = UUID.randomUUID();
        Instant now = clock.instant();
        final String passwordHash = passwordEncoder.encode(command.password());
        Merchant merchant = Merchant.register(id, command.email(), passwordHash, command.legalName(), now);
        merchantRepository.save(merchant);
        return new RegisterMerchantResult(id);
    }
}
