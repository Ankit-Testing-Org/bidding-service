package com.evatech.bidplatform.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_mobile_verification_token")
public class MobileVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String mobileNumber;

    @Column(unique = true)
    private String token;

    private LocalDateTime expiry;

    @OneToOne
    private User user;
}
