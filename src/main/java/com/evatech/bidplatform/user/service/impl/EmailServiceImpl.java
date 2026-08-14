package com.evatech.bidplatform.user.service.impl;

import com.evatech.bidplatform.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${self.service.url}")
    private String baseUrl;

    @Override
    public void sendOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");
        mailSender.send(message);
    }

    @Override
    public void sendVerificationEmail(String email, String token) {

        String link = baseUrl + "/user/email/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Verify your email");

        message.setText("""
                Click the link below to verify your account:
                
                %s
                """.formatted(link));

        mailSender.send(message);
    }

    @Override
    public void sendEmail(Long contractId, String contractName, List<String> emailAddresses) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(emailAddresses.toArray(new String[0]));

        message.setSubject("New contract is upload");

        message.setText("""
                A new contract is being uploaded , please assign it:
                
                contract id %s and contract name %s
                """.formatted(contractId, contractName));

        mailSender.send(message);
    }
}
