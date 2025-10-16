package com.resturant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User information data transfer object")
public class UserDTO {
    @Schema(
            description = "Unique identifier of the user",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
    @Schema(
            description = "Username for authentication and display",
            example = "ethio_food_lover",
            required = true,
            minLength = 3,
            maxLength = 20
    )

    @NotBlank(message = "Username is required")
    private String userName;
    @Schema(
            description = "Email address of the user",
            example = "user@ethiopiankitchen.com",
            required = true,
            format = "email"
    )

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    private String role;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    public String getPassword() {
        return this.password;
    }

}
