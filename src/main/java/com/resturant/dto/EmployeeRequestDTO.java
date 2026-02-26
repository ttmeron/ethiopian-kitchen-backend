package com.resturant.dto;

import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDate;


@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phoneNumber;

    @NotBlank(message = "Position is required")
    private String position;

    private String department;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private Double salary;

    private String address;
    private String emergencyContact;

    @NotBlank(message = "Shift is required")
    private String shift;

}
