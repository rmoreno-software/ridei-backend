package com.ridei.garage.infrastructure.adapter.in.rest;

import org.springframework.security.core.Authentication;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.garage.application.ConfirmMotorbikePhotoCommand;
import com.ridei.garage.application.DeleteMotorbikePhotoCommand;
import com.ridei.garage.application.RequestMotorbikePhotoUploadCommand;
import com.ridei.garage.application.SetPrimaryMotorbikePhotoCommand;
import com.ridei.garage.application.UpdateMotorbikePhotoFocalPointCommand;
import com.ridei.garage.domain.model.ImageContentType;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.PhotoId;
import com.ridei.garage.domain.model.PresignedUpload;
import com.ridei.garage.domain.port.in.ActivateMotorbikeUseCase;
import com.ridei.garage.domain.port.in.ConfirmMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.in.DeactivateMotorbikeUseCase;
import com.ridei.garage.domain.port.in.DeleteMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.in.ListMyMotorbikesUseCase;
import com.ridei.garage.domain.port.in.RegisterMotorbikeUseCase;
import com.ridei.garage.domain.port.in.RequestMotorbikePhotoUploadUseCase;
import com.ridei.garage.domain.port.in.SetPrimaryMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.in.UpdateMotorbikePhotoFocalPointUseCase;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController 
@RequestMapping("/api/v1/garage/motorbikes") 
@AllArgsConstructor 
public class MotorbikeController {

    private final RegisterMotorbikeUseCase registerMotorbikeUseCase;
    private final ListMyMotorbikesUseCase listMyMotorbikesUseCase;
    private final ActivateMotorbikeUseCase activateMotorbikeUseCase;
    private final DeactivateMotorbikeUseCase deactivateMotorbikeUseCase;
    private final RequestMotorbikePhotoUploadUseCase requestMotorbikePhotoUploadUseCase;
    private final ConfirmMotorbikePhotoUseCase confirmMotorbikePhotoUseCase;
    private final SetPrimaryMotorbikePhotoUseCase setPrimaryMotorbikePhotoUseCase;
    private final UpdateMotorbikePhotoFocalPointUseCase updateMotorbikePhotoFocalPointUseCase;
    private final DeleteMotorbikePhotoUseCase deleteMotorbikePhotoUseCase;

    @PostMapping 
    public ResponseEntity<MotorbikeResponseDTO> register(
        @RequestBody @Valid RegisterMotorbikeRequestDTO dto,
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(authentication.getName());
        Motorbike motorbike = registerMotorbikeUseCase.register(dto.toCommand(ownerId));
        return ResponseEntity.status(HttpStatus.CREATED).body(MotorbikeResponseDTO.fromDomain(motorbike));
    }

    @GetMapping
    public ResponseEntity<List<MotorbikeResponseDTO>> listMine(
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(authentication.getName());
        List<MotorbikeResponseDTO> motorbikes = listMyMotorbikesUseCase.list(ownerId)
            .stream()
            .map(MotorbikeResponseDTO::fromDomain)
            .toList();
        return ResponseEntity.ok(motorbikes);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<MotorbikeResponseDTO> activate(
        @PathVariable String id,
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(authentication.getName());
        Motorbike motorbike = activateMotorbikeUseCase.activate(ownerId, MotorbikeId.of(id));
        return ResponseEntity.ok(MotorbikeResponseDTO.fromDomain(motorbike));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MotorbikeResponseDTO> deactivate(
        @PathVariable String id,
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(authentication.getName());
        Motorbike motorbike = deactivateMotorbikeUseCase.deactivate(ownerId, MotorbikeId.of(id));
        return ResponseEntity.ok(MotorbikeResponseDTO.fromDomain(motorbike));
    }

    @PostMapping("/{id}/photo/presign")
    public ResponseEntity<MotorbikePhotoUploadResponseDTO> presignPhoto(
        @PathVariable String id,
        @RequestBody @Valid MotorbikePhotoUploadRequestDTO dto,
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(authentication.getName());
        RequestMotorbikePhotoUploadCommand command = new RequestMotorbikePhotoUploadCommand(
            ownerId,
            MotorbikeId.of(id),
            new ImageContentType(dto.getContentType())
        );
        PresignedUpload upload = requestMotorbikePhotoUploadUseCase.request(command);
        return ResponseEntity.ok(MotorbikePhotoUploadResponseDTO.fromDomain(upload));
    }

    @PostMapping("/{id}/photo/confirm")
    public ResponseEntity<MotorbikeResponseDTO> confirmPhoto(
        @PathVariable String id,
        @RequestBody @Valid ConfirmMotorbikePhotoRequestDTO dto,
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(authentication.getName());
        ConfirmMotorbikePhotoCommand command = new ConfirmMotorbikePhotoCommand(
            ownerId,
            MotorbikeId.of(id),
            dto.getPublicUrl(),
            dto.focalPoint()
        );
        Motorbike motorbike = confirmMotorbikePhotoUseCase.confirm(command);
        return ResponseEntity.ok(MotorbikeResponseDTO.fromDomain(motorbike));
    }

    @PatchMapping("/{id}/photos/{photoId}/primary")
    public ResponseEntity<MotorbikeResponseDTO> setPrimatyPhoto(
        @PathVariable String id,
        @PathVariable String photoId,
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(authentication.getName());
        Motorbike motorbike = setPrimaryMotorbikePhotoUseCase.setPrimary(
            new SetPrimaryMotorbikePhotoCommand(
                ownerId,
                MotorbikeId.of(id),
                PhotoId.of(photoId)
            )
        );
        return ResponseEntity.ok(MotorbikeResponseDTO.fromDomain(motorbike));
    }

    @PatchMapping("/{id}/photos/{photoId}/focal-point")
    public ResponseEntity<MotorbikeResponseDTO> updatePhotoFocalPoint(
        @PathVariable String id,
        @PathVariable String photoId,
        @RequestBody  @Valid UpdateFocalPointRequestDTO dto,
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(authentication.getName());
        Motorbike motorbike = updateMotorbikePhotoFocalPointUseCase.updateFocalPoint(
            new UpdateMotorbikePhotoFocalPointCommand(
                ownerId,
                MotorbikeId.of(id),
                PhotoId.of(photoId),
                dto.toFocalPoint()
            )
        );
        return ResponseEntity.ok(MotorbikeResponseDTO.fromDomain(motorbike));
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public ResponseEntity<MotorbikeResponseDTO> deletePhoto(
        @PathVariable String id,
        @PathVariable String photoId,
        Authentication authentication
    ) {
        OwnerId ownerId = OwnerId.of(id);
        Motorbike motorbike = deleteMotorbikePhotoUseCase.delete(
            new DeleteMotorbikePhotoCommand(
                ownerId,
                MotorbikeId.of(id),
                PhotoId.of(photoId)
            )
        );
        return ResponseEntity.ok(MotorbikeResponseDTO.fromDomain(motorbike));
    }
}
