package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.ridei.garage.domain.model.FinalDriveRatio;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.SetupComponent;
import com.ridei.garage.domain.model.SetupEntry;
import com.ridei.garage.domain.model.SetupEntryId;
import com.ridei.garage.domain.model.SetupSpec;
import com.ridei.garage.domain.model.TyreSpec;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una fila por periodo del histórico. La especificación polimórfica ({@link SetupSpec}) se
 * aplana en columnas nulables; el CHECK de V9 garantiza que cada fila lleva las de su tipo.
 * {@code motorbikeId} es un UUID simple y no una relación: el setup es un agregado aparte.
 */
@Entity
@Table(name = "motorbike_setup_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetupEntryJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "motorbike_id", nullable = false, updatable = false)
    private UUID motorbikeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private SetupComponent component;

    @Column(name = "tyre_model", length = 100)
    private String tyreModel;

    @Column(name = "tyre_size", length = 30)
    private String tyreSize;

    @Column(name = "front_sprocket")
    private Integer frontSprocket;

    @Column(name = "rear_sprocket")
    private Integer rearSprocket;

    @Column(name = "mounted_on", nullable = false)
    private LocalDate mountedOn;

    @Column(name = "removed_on")
    private LocalDate removedOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static SetupEntryJpaEntity fromDomain(MotorbikeId motorbikeId, SetupEntry entry) {
        SetupEntryJpaEntityBuilder builder = SetupEntryJpaEntity.builder()
            .id(entry.getId().value())
            .motorbikeId(motorbikeId.value())
            .component(entry.getComponent())
            .mountedOn(entry.getMountedOn())
            .removedOn(entry.getRemovedOn())
            .createdAt(entry.getCreatedAt());

        switch (entry.getSpec()) {
            case TyreSpec tyre -> builder.tyreModel(tyre.model()).tyreSize(tyre.size());
            case FinalDriveRatio drive -> builder.frontSprocket(drive.frontSprocket()).rearSprocket(drive.rearSprocket());
        }
        return builder.build();
    }

    public SetupEntry toDomain() {
        return SetupEntry.reconstitute(
            new SetupEntryId(id),
            component,
            toSpec(),
            mountedOn,
            removedOn,
            createdAt
        );
    }

    private SetupSpec toSpec() {
        return switch (component) {
            case FRONT_TYRE, REAR_TYRE -> new TyreSpec(tyreModel, tyreSize);
            case FINAL_DRIVE -> new FinalDriveRatio(frontSprocket, rearSprocket);
        };
    }
}
