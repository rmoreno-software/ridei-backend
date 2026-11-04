package com.ridei.circuits.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenueJpaRepository extends JpaRepository<VenueJpaEntity, UUID> {

    Optional<VenueJpaEntity> findByExternalPlaceId(String externalPlaceId);

    @Query("select t.venue.id from TrackJpaEntity t where t.id = :trackId")
    Optional<UUID> findVenueIdByTrackId(@Param("trackId") UUID trackId);

    @Query("select l.track.venue.id from LayoutJpaEntity l where l.id = :layoutId")
    Optional<UUID> findVenueIdByLayoutId(@Param("layoutId") UUID layoutId);
}
