package com.ridei.identity.application.service;

import org.springframework.stereotype.Service;

import com.ridei.identity.application.port.in.ExistsEmailQuery;
import com.ridei.identity.application.port.in.ExistsEmailUseCase;
import com.ridei.identity.application.port.out.CheckUserPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExistsEmailService implements ExistsEmailUseCase{
    
    private final CheckUserPort checkUserPort;
    
    @Override
    public boolean check(ExistsEmailQuery query) {
        log.info("Checking nickname for: {}", query.getEmail());
        return checkUserPort.existsByEmail(query.getEmail());
    }
    
}
