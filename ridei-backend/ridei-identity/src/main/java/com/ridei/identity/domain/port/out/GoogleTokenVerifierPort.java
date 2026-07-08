package com.ridei.identity.domain.port.out;

import com.ridei.identity.domain.model.GoogleUserInfo;

public interface GoogleTokenVerifierPort {
    GoogleUserInfo verify(String idToken);
}
