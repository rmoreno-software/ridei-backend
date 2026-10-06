package com.ridei.garage.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ridei.garage.application.ActivateMotorbikeService;
import com.ridei.garage.application.ConfirmMotorbikePhotoService;
import com.ridei.garage.application.DeactivateMotorbikeService;
import com.ridei.garage.application.DeleteMotorbikePhotoService;
import com.ridei.garage.application.ListBrandsService;
import com.ridei.garage.application.ListMyMotorbikeService;
import com.ridei.garage.application.RegisterMotorbikeService;
import com.ridei.garage.application.RequestMotorbikePhotoUploadService;
import com.ridei.garage.application.SetPrimaryMotorbikePhotoService;
import com.ridei.garage.application.UpdateMotorbikePhotoFocalPointService;
import com.ridei.garage.domain.port.in.ActivateMotorbikeUseCase;
import com.ridei.garage.domain.port.in.ConfirmMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.in.DeactivateMotorbikeUseCase;
import com.ridei.garage.domain.port.in.DeleteMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.in.ListBrandsUseCase;
import com.ridei.garage.domain.port.in.ListMyMotorbikesUseCase;
import com.ridei.garage.domain.port.in.RegisterMotorbikeUseCase;
import com.ridei.garage.domain.port.in.RequestMotorbikePhotoUploadUseCase;
import com.ridei.garage.domain.port.in.SetPrimaryMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.in.UpdateMotorbikePhotoFocalPointUseCase;
import com.ridei.garage.domain.port.out.BrandRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikePhotoStoragePort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

@Configuration 
public class BeanConfig {

    @Bean 
    public RegisterMotorbikeUseCase registerMotorbikeUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        BrandRepositoryPort brandRepository
    ) {
        return new RegisterMotorbikeService(
            motorbikeRepository,
            brandRepository
        );
    }

    @Bean 
    public ListMyMotorbikesUseCase listMyMotorbikesUseCase(MotorbikeRepositoryPort motorbikeRepository) {
        return new ListMyMotorbikeService(motorbikeRepository);
    }

    @Bean 
    public ActivateMotorbikeUseCase activateMotorbikeUseCase(MotorbikeRepositoryPort motorbikeRepository) {
        return new ActivateMotorbikeService(motorbikeRepository);
    }

    @Bean 
    public DeactivateMotorbikeUseCase deactivateMotorbikeUseCase(MotorbikeRepositoryPort motorbikeRepository) {
        return new DeactivateMotorbikeService(motorbikeRepository);
    }

    @Bean
    public ListBrandsUseCase listBrandsUseCase(BrandRepositoryPort brandRepository) {
        return new ListBrandsService(brandRepository);
    }

    @Bean
    public RequestMotorbikePhotoUploadUseCase requestMotorbikePhotoUploadUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikePhotoStoragePort storage
    ) {
        return new RequestMotorbikePhotoUploadService(motorbikeRepository, storage);
    }

    @Bean
    public ConfirmMotorbikePhotoUseCase confirmMotorbikePhotoUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikePhotoStoragePort storage
    ) {
        return new ConfirmMotorbikePhotoService(motorbikeRepository, storage);
    } 

    @Bean 
    public SetPrimaryMotorbikePhotoUseCase setPrimaryMotorbikePhotoUseCase(
        MotorbikeRepositoryPort motorbikeRepository
    ) {
        return new SetPrimaryMotorbikePhotoService(motorbikeRepository);
    }

    @Bean
    public UpdateMotorbikePhotoFocalPointUseCase updateMotorbikePhotoFocalPointUseCase(
        MotorbikeRepositoryPort motorbikeRepository
    ) {
        return new UpdateMotorbikePhotoFocalPointService(motorbikeRepository);
    }

    @Bean 
    public DeleteMotorbikePhotoUseCase deleteMotorbikePhotoUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikePhotoStoragePort motorbikePhotoStorage
    ) {
        return new DeleteMotorbikePhotoService(motorbikeRepository, motorbikePhotoStorage);
    }
}
