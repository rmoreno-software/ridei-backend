package com.ridei.identity.application;

import com.ridei.identity.domain.model.UserId;

public record ChangePasswordCommand(UserId userId, String newPassword) {
    
}
