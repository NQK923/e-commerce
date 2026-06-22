package com.learnfirebase.ecommerce.notification.infrastructure.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.notification.application.port.out.EmailGateway;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailGatewayImpl implements EmailGateway {
    
    private final JavaMailSender javaMailSender;
    
    @Value("${spring.mail.host:localhost}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.from:no-reply@example.com}")
    private String fromEmail;

    @Override
    public String send(String recipient, String subject, String body) {
        String messageId = "email-" + UUID.randomUUID().toString();
        
        if ("localhost".equals(mailHost) && (mailUsername == null || mailUsername.isEmpty())) {
            log.info("[DUMMY EMAIL] To: {}, Subject: {}, Body: {}", recipient, subject, body);
            return messageId;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, true);
            
            javaMailSender.send(message);
            log.info("Email sent to: {}, Subject: {}", recipient, subject);
            return messageId;
        } catch (Exception e) {
            log.error("Failed to send email to {}", recipient, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
