package com.evatech.bidplatform.user.controller;


import com.evatech.bidplatform.user.entity.EmailVerificationToken;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.repository.EmailVerificationTokenRepository;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/user/email")
public class EmailVerificationController {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;


    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        EmailVerificationToken ev = tokenRepo.findByToken(token)
                .orElseThrow(() -> new CustomException("Invalid token"));
        if (ev.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException("Token expired");
        }
        User user = ev.getUser();
        user.setEnabled(true);
        userRepo.save(user);
        tokenRepo.delete(ev);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.ALL_VALUE)
                .body("Email verified successfully!");
    }

    @GetMapping("/update/verify")
    public ResponseEntity<?> updateEmailConfirmation(@RequestParam(name = "token") String token,
                                                      @RequestParam(name = "email") String email) {
        EmailVerificationToken ev = tokenRepo.findByToken(token)
                .orElseThrow(() -> new CustomException("Invalid token"));
        if (ev.getExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException("Token expired");
        }
        User user = ev.getUser();
        user.setEmail(email);
        user.setEnabled(true);
        userRepo.save(user);
        tokenRepo.delete(ev);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.ALL_VALUE)
                .body("Email verified successfully!");
    }
}
