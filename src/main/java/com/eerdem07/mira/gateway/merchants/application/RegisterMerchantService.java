package com.eerdem07.mira.gateway.merchants.application;

import com.eerdem07.mira.gateway.merchants.application.exception.PasswordsDoNotMatchException;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.Merchant;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class RegisterMerchantService implements RegisterMerchantUseCase {
    private final MerchantRepositoryPort merchantRepository;
    private final Clock clock;

    public RegisterMerchantService(MerchantRepositoryPort merchantRepository, Clock clock) {
        this.merchantRepository = merchantRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RegisterMerchantResult execute(RegisterMerchantCommand command) {
        if (!Objects.equals(command.password(), command.paswordConfirm())) {
            throw new PasswordsDoNotMatchException(command.password() + command.paswordConfirm() + "isn't same!");
        }
        UUID id = UUID.randomUUID();
        Instant now = clock.instant();
        final String passwordHash = new BCryptPasswordEncoder().encode(command.password());
        Merchant merchant = Merchant.register(id, command.email(), passwordHash, command.legalName(), now);
        merchantRepository.save(merchant);
        return new RegisterMerchantResult(id);
    }
}
