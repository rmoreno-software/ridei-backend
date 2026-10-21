package com.ridei.garage.application;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.exception.BrandNotFoundException;
import com.ridei.garage.domain.model.Brand;
import com.ridei.garage.domain.model.BrandId;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.port.in.RegisterMotorbikeUseCase;
import com.ridei.garage.domain.port.out.BrandRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.service.MotorbikeActivationPolicy;

public class RegisterMotorbikeService implements  RegisterMotorbikeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final BrandRepositoryPort brandRepository;

    public RegisterMotorbikeService(
        MotorbikeRepositoryPort motorbikeRepository,
        BrandRepositoryPort brandRepository
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.brandRepository = brandRepository;
    }

    @Override
    @Transactional 
    public Motorbike register(RegisterMotorbikeCommand command) {
        BrandResolver.Resolved brand = BrandResolver.resolve(brandRepository, command.brandId(), command.customBrandName());

        Motorbike motorbike = Motorbike.register(
            command.ownerId(),
            brand.brandId(),
            brand.brandName(),
            command.model(),
            command.category(),
            command.year(),
            command.displacementCc(),
            command.weightKg(),
            command.acquisitionDate(),
            command.disposalDate(),
            command.active()
        );

        if (motorbike.isActive()) {
            List<Motorbike> siblings = motorbikeRepository.findAllByOwnerId(command.ownerId());
            MotorbikeActivationPolicy.deactivateConflictsWith(motorbike, siblings)
                .forEach(motorbikeRepository::save);
        }

        motorbikeRepository.save(motorbike);
        return motorbike;
    }
}
