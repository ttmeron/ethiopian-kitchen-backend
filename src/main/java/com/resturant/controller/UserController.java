package com.resturant.controller;
import java.util.*;

import com.resturant.dto.GuestOrderDTO;
import com.resturant.dto.UserDTO;
import com.resturant.dto.UserRegistrationDTO;
import com.resturant.exception.ErrorResponse;
import com.resturant.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "Operations for managing user accounts")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired private PasswordEncoder encoder;

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Register a new user account in the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                    })
    public ResponseEntity<UserDTO> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User details to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDTO.class)))
                    @RequestBody UserDTO userDTO) {

        UserDTO createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Endpoint for user registration with validation"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or passwords don't match",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<?> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegistrationDTO.class)))
                    @Valid @RequestBody UserRegistrationDTO registrationDTO) {

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", "Passwords do not match"));
        }

        if (userService.emailExists(registrationDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ErrorResponse("Registration Error", "Email already exists"));
        }
        String hashedPassword = encoder.encode(registrationDTO.getPassword());

        UserDTO userDTO = UserDTO.builder()
                .userName(registrationDTO.getUserName())
                .email(registrationDTO.getEmail())
                .password(hashedPassword)
                .role("USER")
                .build();

        UserDTO registeredUser = userService.createUser(userDTO);
        return ResponseEntity.ok(registeredUser);
    }
    @GetMapping("/check-email")
    @Operation(
            summary = "Check email availability",
            description = "Check if an email address is already registered"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Email availability status",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@Parameter(
            description = "Email address to check",
            example = "user@example.com",
            required = true) @RequestParam String email) {
        boolean exists = userService.emailExists(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{email}")
    @Operation(
            summary = "Get user by email",
            description = "Retrieve a user's details using their email address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                    })
    public ResponseEntity<UserDTO> getUserByEmail(
            @Parameter(
                    description = "Email address of the user to retrieve",
                    example = "user@example.com",
                    required = true)
            @PathVariable String email) {
        UserDTO userDTO = userService.getUserByEmail(email);
        if (userDTO == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PostMapping("/guest")
    @Operation(
            summary = "Create a guest user",
            description = "Creates a temporary guest account with generated credentials"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Guest user created successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            )
    })
    public ResponseEntity<?> createGuestUser(
            @Valid @RequestBody GuestOrderDTO orderDTO,
            BindingResult bindingResult,
            HttpServletRequest request) {
        log.info("Received guest order: {}", orderDTO);
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    log.error("Validation error: {} - {}", error.getField(), error.getDefaultMessage()));

            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(
                            "Validation failed",
                            bindingResult,
                            request.getRequestURI(),
                            HttpStatus.BAD_REQUEST
                    ));
        }

        String guestUsername = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        String guestPassword = UUID.randomUUID().toString().substring(0, 12);

        String encodedPassword = encoder.encode(guestPassword);
        System.out.println("[DEBUG] Encoded password: " + encodedPassword);


        UserDTO guestUser = UserDTO.builder()
                .userName(guestUsername)
                .email(guestUsername + "@temp.guest")
                .password(encodedPassword)
                .role("GUEST")
                .build();

        System.out.println("[DEBUG] Before createUser - DTO password: " + guestUser.getPassword());

        UserDTO createdGuest = userService.createUser(guestUser);

        System.out.println("[DEBUG] After createUser - DTO password: " + createdGuest.getPassword());
        createdGuest.setPassword(guestPassword);
        return new ResponseEntity<>(createdGuest, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieve a list of all registered users"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of all users",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))

    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PutMapping("/{email}")
    @Operation(
            summary = "Update user by email",
            description = "Update an existing user's information using their email address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
     public ResponseEntity<UserDTO> updateUserByEmail(
            @Parameter(
                    description = "Email address of the user to update",
                    example = "user@example.com",
                    required = true)
            @PathVariable String email,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDTO.class)))
            @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUserByEmail(email, userDTO);
        if (updatedUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{email}")
    @Operation(
            summary = "Delete user by email",
            description = "Remove a user account from the system using their email address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUserByEmail(
            @Parameter(
                    description = "Email address of the user to delete",
                    example = "user@example.com",
                    required = true)
            @PathVariable String email) {
        boolean isDeleted = userService.deleteUserByEmail(email);
        if (isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
