package com.ridei.landing.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import com.ridei.landing.domain.model.WaitlistEntry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "waitlist_entries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistEntryJpaEntity {
    
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public static WaitlistEntryJpaEntity fromDomain(WaitlistEntry waitlistEntry) {
        return WaitlistEntryJpaEntity.builder()
            .id(waitlistEntry.getId())
            .email(waitlistEntry.getEmail().value())
            .joinedAt(waitlistEntry.getJoinedAt())
            .build();
    }

}
