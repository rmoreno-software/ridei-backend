package com.ridei.landing.infrastructure.adapter.out.persistence;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.ridei.landing.domain.model.Email;
import com.ridei.landing.domain.model.WaitlistEntry;
import com.ridei.landing.domain.port.in.WaitlistRepositoryPort;

@Component
public class WaitlistPersistenceAdapter implements WaitlistRepositoryPort {

    private final WaitlistJpaRepository jpaRepository;

    public WaitlistPersistenceAdapter(WaitlistJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public void save(WaitlistEntry entry) {
        try {
            jpaRepository.save(WaitlistEntryJpaEntity.fromDomain(entry));
        } catch (DataIntegrityViolationException e) {

        }
    }
    
}
