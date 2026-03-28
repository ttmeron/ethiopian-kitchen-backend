package com.resturant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    SendGridService sendGridService;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String subject = "Reset Your Password";
        String resetLink = "https://ethiopian-kitchen-frontend.onrender.com/reset-password?token=" + resetToken;

        String htmlContent = "<html>" +
                "<body>" +
                "<h2>Password Reset Request</h2>" +
                "<p>Click the link below to reset your password:</p>" +
                "<a href=\"" + resetLink + "\">Reset Password</a>" +
                "<p>This link expires in 24 hours.</p>" +
                "</body>" +
                "</html>";

        try {
            sendGridService.sendEmail(toEmail, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }
}

