package com.resturant.service;

import com.resturant.dto.EmployeeRequestDTO;
import com.resturant.dto.EmployeeResponseDTO;
import com.resturant.entity.Employee;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.EmployeeMapper;
import com.resturant.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;



    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO) {

        if (employeeRepository.existsByEmail(employeeRequestDTO.getEmail())) {
            throw new RuntimeException("Employee with email " + employeeRequestDTO.getEmail() + " already exists");
        }

        Employee employee = employeeMapper.toEntity(employeeRequestDTO);


        if (employee.getDepartment() == null) {
            employee.setDepartment(determineDepartment(employee.getPosition()));
        }

        Employee savedEmployee = employeeRepository.save(employee);
        if (savedEmployee.getEmployeeCode() == null) {
            String employeeCode = "EMP" + String.format("%04d", savedEmployee.getId());
            savedEmployee.setEmployeeCode(employeeCode);
            savedEmployee = employeeRepository.save(savedEmployee);
        }

        return employeeMapper.toResponseDTO(savedEmployee);

    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();

        return employeeMapper.toResponseDTOList(employees);
    }

    @Override

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {

        Optional<Employee> employee = employeeRepository.findById(id);

        return employee.map(employeeMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeByEmail(String email) {
        Optional<Employee> employee = employeeRepository.findByEmail(email);
        return employee.map(employeeMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeByCode(String employeeCode) {
        Optional<Employee> employee = employeeRepository.findByEmployeeCode(employeeCode);
        return employee.map(employeeMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeCode));
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByPosition(String position) {
        return null;
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartment(String department) {
        return null;
    }

    @Override
    public List<EmployeeResponseDTO> getActiveEmployees() {
        return null;
    }

    @Override
    public List<EmployeeResponseDTO> searchEmployeesByName(String name) {

        List<Employee> employees = employeeRepository.findByNameContaining(name);

        return employeeMapper.toResponseDTOList(employees);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO employeeRequestDTO) {
        return null;
    }

    @Override
    public void deactivateEmployee(Long id) {

    }

    @Override
    public void activateEmployee(Long id) {

    }

    @Override
    public void deleteEmployee(Long id) {

    }

    @Override
    public long getEmployeesCount() {
        return 0;
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByShift(String shift) {
        return null;
    }

    private String determineDepartment(String position) {
        if (position == null) return "GENERAL";

        String upperPosition = position.toUpperCase();

        switch (upperPosition) {
            case "CHEF":
            case "SOUS_CHEF":
            case "LINE_COOK":
            case "DISHWASHER":
                return "KITCHEN";
            case "WAITER":
            case "WAITRESS":
            case "SERVER":
            case "BARTENDER":
            case "HOST":
            case "HOSTESS":
                return "SERVICE";
            case "MANAGER":
            case "ASSISTANT_MANAGER":
                return "MANAGEMENT";
            case "CASHIER":
                return "FRONT_DESK";
            default:
                return "GENERAL";
        }
    }
}
