package com.ridei.landing.domain.port.in;

import com.ridei.landing.domain.model.Email;
import com.ridei.landing.domain.model.WaitlistEntry;

public interface WaitlistRepositoryPort {
    boolean existsByEmail(Email email);
    void save(WaitlistEntry entry);
}
