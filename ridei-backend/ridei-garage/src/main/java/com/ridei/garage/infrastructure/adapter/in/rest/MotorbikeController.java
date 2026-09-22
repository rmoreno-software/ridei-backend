package com.ridei.garage.infrastructure.adapter.in.rest;

import org.springframework.security.core.Authentication;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.in.ActivateMotorbikeUseCase;
import com.ridei.garage.domain.port.in.DeactivateMotorbikeUseCase;
import com.ridei.garage.domain.port.in.ListMyMotorbikesUseCase;
import com.ridei.garage.domain.port.in.RegisterMotorbikeUseCase;

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
}
