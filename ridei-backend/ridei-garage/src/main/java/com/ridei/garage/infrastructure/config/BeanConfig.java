package com.ridei.garage.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ridei.garage.application.ActivateMotorbikeService;
import com.ridei.garage.application.ConfirmMotorbikePhotoService;
import com.ridei.garage.application.CorrectSetupEntryService;
import com.ridei.garage.application.GetMotorbikeSetupService;
import com.ridei.garage.application.RegisterSetupChangeService;
import com.ridei.garage.application.port.in.CorrectSetupEntryUseCase;
import com.ridei.garage.application.port.in.GetMotorbikeSetupUseCase;
import com.ridei.garage.application.port.in.RegisterSetupChangeUseCase;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;
import com.ridei.garage.application.DeleteMotorbikeService;
import com.ridei.garage.application.PurgeMotorbikePhotosService;
import com.ridei.garage.application.port.in.DeleteMotorbikeUseCase;
import com.ridei.garage.application.port.in.PurgeMotorbikePhotosUseCase;
import com.ridei.garage.domain.port.out.EventPublisherPort;
import com.ridei.garage.application.DeactivateMotorbikeService;
import com.ridei.garage.application.DeleteMotorbikePhotoService;
import com.ridei.garage.application.EditMotorbikeService;
import com.ridei.garage.application.ListBrandsService;
import com.ridei.garage.application.ListMyMotorbikeService;
import com.ridei.garage.application.RegisterMotorbikeService;
import com.ridei.garage.application.RequestMotorbikePhotoUploadService;
import com.ridei.garage.application.SetPrimaryMotorbikePhotoService;
import com.ridei.garage.application.UpdateMotorbikePhotoFocalPointService;
import com.ridei.garage.application.port.in.ActivateMotorbikeUseCase;
import com.ridei.garage.application.port.in.ConfirmMotorbikePhotoUseCase;
import com.ridei.garage.application.port.in.DeactivateMotorbikeUseCase;
import com.ridei.garage.application.port.in.DeleteMotorbikePhotoUseCase;
import com.ridei.garage.application.port.in.EditMotorbikeUseCase;
import com.ridei.garage.application.port.in.ListBrandsUseCase;
import com.ridei.garage.application.port.in.ListMyMotorbikesUseCase;
import com.ridei.garage.application.port.in.RegisterMotorbikeUseCase;
import com.ridei.garage.application.port.in.RequestMotorbikePhotoUploadUseCase;
import com.ridei.garage.application.port.in.SetPrimaryMotorbikePhotoUseCase;
import com.ridei.garage.application.port.in.UpdateMotorbikePhotoFocalPointUseCase;
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

    @Bean 
    public EditMotorbikeUseCase editMotorbikeUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        BrandRepositoryPort brandRepository
    ) {
        return new EditMotorbikeService(motorbikeRepository, brandRepository);
    }

    // ---- Baja definitiva de la moto -------------------------------------

    @Bean
    public DeleteMotorbikeUseCase deleteMotorbikeUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikeSetupRepositoryPort setupRepository,
        EventPublisherPort eventPublisher
    ) {
        return new DeleteMotorbikeService(motorbikeRepository, setupRepository, eventPublisher);
    }

    @Bean
    public PurgeMotorbikePhotosUseCase purgeMotorbikePhotosUseCase(MotorbikePhotoStoragePort photoStorage) {
        return new PurgeMotorbikePhotosService(photoStorage);
    }

    // ---- Setup de la moto ------------------------------------------------

    @Bean
    public GetMotorbikeSetupUseCase getMotorbikeSetupUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikeSetupRepositoryPort setupRepository
    ) {
        return new GetMotorbikeSetupService(motorbikeRepository, setupRepository);
    }

    @Bean
    public RegisterSetupChangeUseCase registerSetupChangeUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikeSetupRepositoryPort setupRepository
    ) {
        return new RegisterSetupChangeService(motorbikeRepository, setupRepository);
    }

    @Bean
    public CorrectSetupEntryUseCase correctSetupEntryUseCase(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikeSetupRepositoryPort setupRepository
    ) {
        return new CorrectSetupEntryService(motorbikeRepository, setupRepository);
    }
}
