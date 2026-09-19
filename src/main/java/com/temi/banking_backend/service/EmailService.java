package com.temi.banking_backend.service;

import com.temi.banking_backend.exception.EmailDeliveryException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your login verification code");
        message.setText("Your one-time verification code is: " + otp +
                "\n\nThis code expires in 5 minutes. If you didn't request this, please ignore this email.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailDeliveryException(toEmail, e);
        }
    }
}
