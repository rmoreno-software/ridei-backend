package com.ridei.landing.infrastructure.adapter.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.landing.domain.model.Email;
import com.ridei.landing.application.JoinWaitlistCommand;
import com.ridei.landing.domain.port.in.JoinWaitlistUseCase;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/waitlist")
@AllArgsConstructor
public class WaitlistController {
    private final JoinWaitlistUseCase joinWaitlistUseCase;

    @PostMapping
    public ResponseEntity<Void> join(@RequestBody @Valid JoinWaitlistRequestDTO dto) {
        joinWaitlistUseCase.join(new JoinWaitlistCommand(new Email(dto.getEmail())));
        return ResponseEntity.accepted().build();
    }
}
