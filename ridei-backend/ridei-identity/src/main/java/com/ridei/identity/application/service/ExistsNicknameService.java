package com.ridei.identity.application.service;

import org.springframework.stereotype.Service;

import com.ridei.identity.application.port.in.ExistsNicknameQuery;
import com.ridei.identity.application.port.in.ExistsNicknameUseCase;
import com.ridei.identity.application.port.out.CheckUserPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExistsNicknameService implements ExistsNicknameUseCase{

    private final CheckUserPort checkUserPort;

    @Override
    public boolean check(ExistsNicknameQuery query) {
        return checkUserPort.existsByNickname(query.getNickname());
    }
    
}
