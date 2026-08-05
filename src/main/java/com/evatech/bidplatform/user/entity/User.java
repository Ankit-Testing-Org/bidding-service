package com.evatech.bidplatform.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String keycloakUserId;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String addressLine1;
    private String addressLine2;
    private String postCode;
    private String city;
    private String state;
    private String country;
    private String phoneNumber;
    private boolean enabled = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private EmailVerificationToken emailVerificationToken;
}
