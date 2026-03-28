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

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">" +
                "<div style=\"background-color: #f4f4f4; padding: 20px; text-align: center;\">" +
                "<h2 style=\"color: #333;\">Ethiopian Kitchen</h2>" +
                "</div>" +
                "<div style=\"padding: 20px;\">" +
                "<p>Hello,</p>" +
                "<p>We received a request to reset your password. Click the button below:</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"" + resetLink + "\" style=\"background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px;\">Reset Password</a>" +
                "</div>" +
                "<p>If you didn't request this, please ignore this email.</p>" +
                "<p>This link expires in 24 hours.</p>" +
                "</div>" +
                "<div style=\"background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; color: #666;\">" +
                "<p>Ethiopian Kitchen • Your Restaurant</p>" +
                "</div>" +
                "</body>" +
                "... <p><a href=\"https://yourdomain.com/unsubscribe?email=" + toEmail + "\">Unsubscribe</a></p> ...";

        try {
            sendGridService.sendEmail(toEmail, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }
}

