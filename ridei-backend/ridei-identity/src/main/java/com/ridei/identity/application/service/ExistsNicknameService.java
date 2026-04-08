package com.ridei.identity.application.service;

import org.springframework.stereotype.Service;

import com.ridei.identity.application.port.in.ExistsNicknameQuery;
import com.ridei.identity.application.port.in.ExistsNicknameUseCase;
import com.ridei.identity.application.port.out.CheckUserPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExistsNicknameService implements ExistsNicknameUseCase{

    private final CheckUserPort checkUserPort;

    @Override
    public boolean check(ExistsNicknameQuery query) {
        log.info("Checking nickname for: {}", query.getNickname());
        return checkUserPort.existsByNickname(query.getNickname());
    }
    
}
