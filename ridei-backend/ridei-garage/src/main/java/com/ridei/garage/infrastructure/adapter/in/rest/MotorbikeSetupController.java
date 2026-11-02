package com.ridei.garage.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.garage.application.port.in.CorrectSetupEntryUseCase;
import com.ridei.garage.application.port.in.GetMotorbikeSetupUseCase;
import com.ridei.garage.application.port.in.RegisterSetupChangeUseCase;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.MotorbikeSetup;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.SetupComponent;
import com.ridei.garage.domain.model.SetupEntryId;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/** Setup de la moto (neumáticos y relación final) con histórico por componente. */
@RestController
@RequestMapping("/api/v1/garage/motorbikes/{id}/setup")
@AllArgsConstructor
public class MotorbikeSetupController {

    private final GetMotorbikeSetupUseCase getMotorbikeSetupUseCase;
    private final RegisterSetupChangeUseCase registerSetupChangeUseCase;
    private final CorrectSetupEntryUseCase correctSetupEntryUseCase;

    @GetMapping
    public ResponseEntity<MotorbikeSetupResponseDTO> get(
        @PathVariable String id,
        Authentication authentication
    ) {
        MotorbikeSetup setup = getMotorbikeSetupUseCase.get(ownerOf(authentication), MotorbikeId.of(id));
        return ResponseEntity.ok(MotorbikeSetupResponseDTO.fromDomain(setup));
    }

    @GetMapping("/{component}/history")
    public ResponseEntity<List<SetupEntryResponseDTO>> history(
        @PathVariable String id,
        @PathVariable String component,
        Authentication authentication
    ) {
        SetupComponent setupComponent = parseComponent(component);
        MotorbikeSetup setup = getMotorbikeSetupUseCase.get(ownerOf(authentication), MotorbikeId.of(id));
        return ResponseEntity.ok(
            setup.history(setupComponent).stream().map(SetupEntryResponseDTO::fromDomain).toList()
        );
    }

    @PostMapping("/changes")
    public ResponseEntity<MotorbikeSetupResponseDTO> registerChange(
        @PathVariable String id,
        @RequestBody @Valid RegisterSetupChangeRequestDTO dto,
        Authentication authentication
    ) {
        MotorbikeSetup setup = registerSetupChangeUseCase.register(
            dto.toCommand(ownerOf(authentication), MotorbikeId.of(id))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(MotorbikeSetupResponseDTO.fromDomain(setup));
    }

    @PutMapping("/entries/{entryId}")
    public ResponseEntity<MotorbikeSetupResponseDTO> correctEntry(
        @PathVariable String id,
        @PathVariable String entryId,
        @RequestBody @Valid CorrectSetupEntryRequestDTO dto,
        Authentication authentication
    ) {
        MotorbikeSetup setup = correctSetupEntryUseCase.correct(
            dto.toCommand(ownerOf(authentication), MotorbikeId.of(id), SetupEntryId.of(entryId))
        );
        return ResponseEntity.ok(MotorbikeSetupResponseDTO.fromDomain(setup));
    }

    private static OwnerId ownerOf(Authentication authentication) {
        return OwnerId.of(authentication.getName());
    }

    /** Acepta tanto {@code REAR_TYRE} como {@code rear-tyre}. */
    private static SetupComponent parseComponent(String value) {
        try {
            return SetupComponent.valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unknown setup component '" + value + "'. Expected one of: front-tyre, rear-tyre, final-drive");
        }
    }
}
