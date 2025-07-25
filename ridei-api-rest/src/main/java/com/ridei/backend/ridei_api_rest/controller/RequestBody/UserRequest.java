package com.ridei.backend.ridei_api_rest.controller.RequestBody;

import com.ridei.backend.ridei_api_rest.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserRequest {

    private String email;
    private String phoneNumber;
    private String fullName;
    private String country;
    private String password;

    public User toEntity(String hashedPassword) {
        return User.builder()
                .email(this.email)
                .fullName(this.fullName)
                .phoneNumber(this.phoneNumber)
                .country(this.country)
                .encryptedPassword(hashedPassword)
                .build();
    }

}
