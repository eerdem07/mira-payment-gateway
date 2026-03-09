package com.eerdem07.mira.gateway.merchants.persistence;

import com.eerdem07.mira.gateway.auth.application.port.out.MerchantAuthPort;
import com.eerdem07.mira.gateway.merchants.application.port.out.MerchantRepositoryPort;
import com.eerdem07.mira.gateway.merchants.domain.Merchant;
import com.eerdem07.mira.gateway.merchants.domain.exception.MerchantEmailAlreadyExistsException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MerchantPersistenceAdapter implements MerchantRepositoryPort, MerchantAuthPort {
    private final SpringDataMerchantRepository repository;

    public MerchantPersistenceAdapter(SpringDataMerchantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Merchant> findById(UUID merchantId) {
        return repository.findById(merchantId)
                .map(MerchantPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Merchant> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(MerchantPersistenceMapper::toDomain);
    }

    @Override
    public void save(Merchant merchant) {
        try {
            repository.saveAndFlush(MerchantPersistenceMapper.toEntity(merchant));
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateKey(ex)) {
                throw new MerchantEmailAlreadyExistsException(merchant.getEmail());
            }
            throw ex;
        }
    }

    private boolean isDuplicateKey(DataIntegrityViolationException ex) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);

        if (root instanceof java.sql.SQLException sqlEx) {
            // PostgreSQL duplicate: SQLState 23505; MySQL duplicate entry: error code 1062
            String state = sqlEx.getSQLState();
            int code = sqlEx.getErrorCode();
            if ("23505".equals(state) || code == 1062) {
                return true;
            }
        }

        String message = root != null ? root.getMessage() : ex.getMessage();
        if (message == null) return false;

        String lower = message.toLowerCase();
        return lower.contains("duplicate key")
                || lower.contains("unique constraint")
                || lower.contains("unique index")
                || lower.contains("duplicate entry");
    }
}
