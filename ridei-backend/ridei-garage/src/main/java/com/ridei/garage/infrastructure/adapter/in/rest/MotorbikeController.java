package com.ridei.garage.infrastructure.adapter.in.rest;

import org.springframework.security.core.Authentication;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.OwnerId;
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
}
