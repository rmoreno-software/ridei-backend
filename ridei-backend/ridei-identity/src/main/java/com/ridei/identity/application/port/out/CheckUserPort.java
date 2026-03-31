package com.ridei.identity.application.port.out;

public interface CheckUserPort {
    boolean existsByNickname(String nickname);
}
