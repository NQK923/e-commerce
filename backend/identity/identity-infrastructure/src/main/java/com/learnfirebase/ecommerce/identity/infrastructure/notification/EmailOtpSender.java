package com.learnfirebase.ecommerce.identity.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.learnfirebase.ecommerce.identity.application.port.out.OtpSender;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailOtpSender implements OtpSender {
    private static final Logger log = LoggerFactory.getLogger(EmailOtpSender.class);
    private final JavaMailSender mailSender;
    @org.springframework.beans.factory.annotation.Value("${frontend.mail.from:no-reply@example.com}")
    private String from;

    @Override
    public void sendOtp(String email, String code) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            if (StringUtils.hasText(from)) {
                message.setFrom(from);
            }
            message.setSubject("Your login verification code");
            message.setText("Your OTP code is: " + code + "\nThis code expires in 5 minutes.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}", email, ex);
        }
    }
}
