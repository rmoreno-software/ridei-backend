package com.ridei.identity.application.port.in;

import com.ridei.identity.shared.SelfValidating;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ExistsNicknameQuery extends SelfValidating<ExistsNicknameQuery> {
    @NotBlank(message = "El nickname no puede estar vacío")
    private final String nickname;

    public ExistsNicknameQuery(String nickname) {
        this.nickname = nickname;
        this.validateSelf();
    }
}
