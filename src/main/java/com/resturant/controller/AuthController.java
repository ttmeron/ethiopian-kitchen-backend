package com.resturant.controller;

import com.resturant.dto.LoginRequest;
import com.resturant.dto.ResetPasswordRequestDTO;
import com.resturant.dto.response.AuthResponse;
import com.resturant.entity.User;
import com.resturant.exception.ErrorResponse;
import com.resturant.exception.UserNotFoundException;
import com.resturant.repository.UserRepository;
import com.resturant.security.JwtUtil;

import com.resturant.service.EmailService;
import com.resturant.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepo.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if ("meron21bela@gmail.com".equals(user.getEmail())) {
            user.setRole("ADMIN");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        userRepo.save(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getUserName(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try{
        String normalizedEmail = req.getEmail().trim().toLowerCase();

            User user = userRepo.findByEmailIgnoreCase(normalizedEmail)
                    .orElseThrow(() -> {
                        System.err.println("User not found for email: " + normalizedEmail);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                    });
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token, user.getUserName(), user.getEmail()));

        } catch (ResponseStatusException e) {
            // This will automatically convert to proper HTTP response
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Login failed. Please try again later."
            );
        }
    }

    @GetMapping("/validate-reset-token/{token}")
    public ResponseEntity<?> validateResetToken(@PathVariable String token) {
        try {
            boolean isValid = passwordResetService.validateResetToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);

            if (!isValid) {
                response.put("message", "Invalid or expired reset token");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Token validation error: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to validate reset token"
            );
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            System.out.println("=== FORGOT PASSWORD REQUEST START ===");
            String email = request.get("email");
            System.out.println("Received email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            String normalizedEmail = email.trim().toLowerCase();
            System.out.println("Normalized email: " + normalizedEmail);

            // Check if user exists
            System.out.println("Searching for user in database...");
            User user = userRepo.findByEmailIgnoreCase(normalizedEmail)
                    .orElseThrow(() -> {
                        System.out.println("USER NOT FOUND IN DATABASE: " + normalizedEmail);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "No account found with this email address");
                    });

            System.out.println("User found: " + user.getEmail() + ", ID: " + user.getId());

            // Generate reset token
            System.out.println("Generating reset token...");
            String resetToken = passwordResetService.generateResetToken(normalizedEmail);
            System.out.println("Generated token: " + resetToken);

            // Send mock reset email
            System.out.println("Sending email...");
            emailService.sendPasswordResetEmail(normalizedEmail, resetToken);
            System.out.println("Email sent successfully");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset instructions have been sent to your email");
            response.put("debug_info", "Check server console for reset token during development");

            System.out.println("=== FORGOT PASSWORD REQUEST SUCCESS ===");
            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            System.out.println("ResponseStatusException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("=== UNEXPECTED ERROR ===");
            System.err.println("Forgot password error: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to process password reset request"
            );
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        try {
            // Validate token
            if (!passwordResetService.validateResetToken(request.getToken())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token");
            }

            // Check if passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
            }

            // Check password strength
            if (request.getNewPassword().length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters long");
            }

            // Get email from token
            String email = passwordResetService.getEmailFromToken(request.getToken());
            if (email == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset token");
            }

            // Find user and update password
            User user = userRepo.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            user.setPassword(encoder.encode(request.getNewPassword()));
            userRepo.save(user);

            // Invalidate the used token
            passwordResetService.invalidateToken(request.getToken());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Reset password error: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to reset password"
            );
        }
    }
}


