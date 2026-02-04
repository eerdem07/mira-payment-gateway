package com.eerdem07.mira.gateway.merchants.application.usecase;

import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantCommand;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantResult;
import com.eerdem07.mira.gateway.merchants.application.port.in.RegisterMerchantUseCase;
import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.merchant.Merchant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class RegisterMerchantService implements RegisterMerchantUseCase {
    private final MerchantRepositoryPort merchantRepository;
    private final Clock clock;

    public RegisterMerchantService(MerchantRepositoryPort merchantRepository, Clock clock){
        this.merchantRepository = merchantRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RegisterMerchantResult register(RegisterMerchantCommand command){
        UUID id  = UUID.randomUUID();
        Merchant merchant = Merchant.register(id, command.legalName(), clock);
        merchantRepository.save(merchant);
        return new RegisterMerchantResult(id);
    }
}
