package com.resturant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    JavaMailSender mailSender;
//
//    public void sendPasswordResetEmail(String toEmail, String resetToken) {
//        try {
//            // MOCK EMAIL SERVICE - No real email sending
//            System.out.println("🔐 === PASSWORD RESET EMAIL (MOCK) ===");
//            System.out.println("📧 To: " + toEmail);
//            System.out.println("🔗 Reset Link: http://localhost:4200/reset-password?token=" + resetToken);
//            System.out.println("🔑 Token (copy this): " + resetToken);
//            System.out.println("📝 Instructions: Copy the token above and use it in the reset password page");
//            System.out.println("🔐 === END MOCK EMAIL ===");
//
//            // No real email sending - this is just for development
//            // The token is printed to console for testing
//
//        } catch (Exception e) {
//            System.err.println("❌ Mock email error: " + e.getMessage());
//            // Don't throw exception - just log it since this is mock
//        }
//    }



    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Your Restaurant App");

            String resetUrl = "http://localhost:4200/reset-password?token=" + resetToken;

            String emailText =
                    "You have requested to reset your password.\n\n" +
                            "Please click the link below to reset your password:\n" +
                            resetUrl + "\n\n" +
                            "This link will expire in 1 hour.\n\n" +
                            "If you didn't request this, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "Your Restaurant App Team";

            message.setText(emailText);
            message.setFrom("noreply@yourrestaurantapp.com"); // This can be your Gmail address

            mailSender.send(message);

            System.out.println("✅ Real password reset email sent to: " + toEmail);
            System.out.println("🔗 Reset URL: " + resetUrl);

        } catch (Exception e) {
            System.err.println("❌ Failed to send real email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to send reset email", e);
        }
    }
}

