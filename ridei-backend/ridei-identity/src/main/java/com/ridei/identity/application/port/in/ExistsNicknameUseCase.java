package com.ridei.identity.application.port.in;

public interface ExistsNicknameUseCase {
    boolean check(ExistsNicknameQuery query);
}
