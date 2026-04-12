package com.ridei.identity.application.port.in;

public interface ExistsEmailUseCase {
    boolean check(ExistsEmailQuery query);
}
