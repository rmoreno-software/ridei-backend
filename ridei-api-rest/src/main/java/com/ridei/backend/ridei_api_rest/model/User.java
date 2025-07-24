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
    private Long id;

    private String name;

    private String email;
}
