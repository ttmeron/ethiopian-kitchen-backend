package com.resturant.controller;

import com.resturant.dto.UserDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "Operations for managing user accounts")
public class UserController {
    @Autowired
    private UserService userService;

    // Create a new user
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

    // Get a user by email
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

    // Get all users
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

    // Update a user by email
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

    // Delete a user by email
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
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Success
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // User not found
        }
    }
}
