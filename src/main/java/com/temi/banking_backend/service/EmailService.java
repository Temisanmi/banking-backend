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

    @Value("${app.name}")
    private String appName;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your login verification code");
        message.setText("Your one-time verification code is: " + otp +
                "\n\nThis code expires in 5 minutes. If you didn't request this, please ignore this email."
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailDeliveryException(toEmail, e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Password reset code");
        message.setText("Your password reset code is: " + code +
                "\n\nThis code expires in 10 minutes. If you didn't request this, please ignore this email."
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailDeliveryException(toEmail, e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Welcome to " + appName + "!");
        message.setText("Hi " + firstName + ",\n\n" +
                "Welcome! Your account has been created successfully.\n\n" +
                "Going forward, you'll receive OTPs and password reset codes at this email address " +
                "whenever you log in or need to reset your password. Please check your spam or junk " +
                "folder and mark these emails as \"not spam\" so you don't miss anything important.\n\n" +
                "Thanks for signing up.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailDeliveryException(toEmail, e);
        }
    }
}
