package com.evatech.bidplatform.user.dto.request;

import com.evatech.bidplatform.user.dto.RoleType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Username is required")
    private String userName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private LocalDate dateOfBirth;

    private String addressLine1;

    private String addressLine2;

    private String postCode;

    private String city;

    private String state;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    private String country;

    @NotNull(message = "Role is required")
    private RoleType roleType;
}
