package com.resturant.dto;


import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private String position;
    private String department;
    private LocalDate hireDate;
    private Double salary;
    private String address;
    private String emergencyContact;
    private String shift;
    private boolean active;
    private LocalDateTime createdAt;
    private String employeeCode = "EMP0001";

    public boolean isActive() {
        return active;
    }

}
