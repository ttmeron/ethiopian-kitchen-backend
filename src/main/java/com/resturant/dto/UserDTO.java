package com.resturant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 20
    )
    private String userName;
    @Schema(
            description = "Email address of the user",
            example = "user@ethiopiankitchen.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "email"
    )
    private String email;
}
