package com.ridei.backend.ridei_api_rest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appusers")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String phoneNumber;

    private String fullName;

    private String country;

    private String encryptedPassword;
}
