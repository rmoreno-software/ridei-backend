package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.domain.model.AccountStatus;
import com.ridei.identity.domain.model.Gender;
import com.ridei.identity.domain.model.UserProfile;
import com.ridei.identity.domain.model.UserRole;

import lombok.Data;

@Data
public class UserProfileResponseDTO {
    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Gender gender;
    private UserRole role;
    private AccountStatus status;
    private String profileType;
    private String racingLicenseNumber;
    private String dateOfBirth;
    private String countryCode;
    private String phoneNumber;
    private String profilePictureUrl;

    public static UserProfileResponseDTO fromDomain(UserProfile profile) {
        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setId(profile.id().value().toString());
        dto.setEmail(profile.email().value());
        dto.setUsername(profile.username() != null ? profile.username().value() : null);
        dto.setFirstName(profile.firstName());
        dto.setLastName(profile.lastName());
        dto.setGender(profile.gender());
        dto.setRole(profile.role());
        dto.setStatus(profile.status());
        dto.setProfileType(profile.profileType());
        dto.setRacingLicenseNumber(profile.racingLicenseNumber());
        dto.setDateOfBirth(profile.dateOfBirth() != null ? profile.dateOfBirth().toString() : null);
        dto.setCountryCode(profile.countryCode());
        dto.setPhoneNumber(profile.phoneNumber() != null ? profile.phoneNumber().value() : null);
        dto.setProfilePictureUrl(profile.profilePictureUrl());
        return dto;
    }
}
