package com.ridei.garage.application;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.port.in.EditMotorbikeUseCase;
import com.ridei.garage.domain.port.out.BrandRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.service.MotorbikeActivationPolicy;

public class EditMotorbikeService implements EditMotorbikeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final BrandRepositoryPort brandRepository;

    public EditMotorbikeService(
        MotorbikeRepositoryPort motorbikeRepository,
        BrandRepositoryPort brandRepository
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.brandRepository = brandRepository;
    }

    @Override
    @Transactional 
    public Motorbike edit(EditMotorbikeCommand command) {
        Motorbike motorbike = OwnedMotorbikes.require(motorbikeRepository, command.ownerId(), command.motorbikeId());

        BrandResolver.Resolved brand = BrandResolver.resolve(brandRepository, command.brandId(), command.customBrandName());

        motorbike.update(
            brand.brandId(),
            brand.brandName(),
            command.model(),
            command.category(),
            command.year(),
            command.displacementCc(),
            command.weightKg(),
            command.acquisitionDate(),
            command.disposalDate()
        );

        List<Motorbike> siblings = motorbikeRepository.findAllByOwnerId(command.ownerId());

        // Si no está de baja, no está ya activa, y es la única candidata de su grupo, se activa sola.
        if (!motorbike.isActive() && !motorbike.isRetired()
                && MotorbikeActivationPolicy.isSoleCandidateInGroup(motorbike, siblings)) {
            motorbike.activate();
        }

        // Si queda activa (ya lo estaba, o se acaba de activar arriba), resuelve cualquier conflicto
        // de categoryGroup — por ejemplo, si cambió de grupo y el nuevo ya tenía otra activa.
        if (motorbike.isActive()) {
            MotorbikeActivationPolicy.deactivateConflictsWith(motorbike, siblings)
                .forEach(motorbikeRepository::save);
        }

        motorbikeRepository.save(motorbike);
        return motorbike;
    }
    
}
