package com.resturant.service;


import com.resturant.dto.EmployeeRequestDTO;
import com.resturant.dto.EmployeeResponseDTO;
import java.util.List;

public interface EmployeeService {

    EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO);

    List<EmployeeResponseDTO> getAllEmployees();

    EmployeeResponseDTO getEmployeeById(Long id);

    EmployeeResponseDTO getEmployeeByEmail(String email);

    EmployeeResponseDTO getEmployeeByCode(String employeeCode);

    List<EmployeeResponseDTO> getEmployeesByPosition(String position);

    List<EmployeeResponseDTO> getEmployeesByDepartment(String department);

    List<EmployeeResponseDTO> getActiveEmployees();

    List<EmployeeResponseDTO> searchEmployeesByName(String name);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO employeeRequestDTO);

    void deactivateEmployee(Long id);

    void activateEmployee(Long id);

    void deleteEmployee(Long id);

    long getEmployeesCount();

    List<EmployeeResponseDTO> getEmployeesByShift(String shift);

}
