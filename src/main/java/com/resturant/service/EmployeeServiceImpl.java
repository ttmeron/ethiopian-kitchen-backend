package com.resturant.service;

import com.resturant.dto.EmployeeRequestDTO;
import com.resturant.dto.EmployeeResponseDTO;
import com.resturant.entity.Employee;
import com.resturant.entity.User;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.EmployeeMapper;
import com.resturant.repository.EmployeeRepository;
import com.resturant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO) {

        if (employeeRepository.existsByEmail(employeeRequestDTO.getEmail())) {
            throw new RuntimeException("Employee with email " + employeeRequestDTO.getEmail() + " already exists");
        }
        if (userRepository.existsByEmail(employeeRequestDTO.getEmail())) {
            throw new RuntimeException("User account already exists with this email");
        }


        User user = new User();
        user.setEmail(employeeRequestDTO.getEmail());
        user.setUserName(employeeRequestDTO.getFirstName() + " " + employeeRequestDTO.getLastName());
        user.setRole(employeeRequestDTO.getPosition().toUpperCase());
        user.setEmployee(true);

        // Generate temporary password
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setTemporaryPassword(true);
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(7));
        user.setActive(true);

        User savedUser = userRepository.save(user);

        Employee employee = employeeMapper.toEntity(employeeRequestDTO);


        if (employee.getDepartment() == null) {
            employee.setDepartment(determineDepartment(employee.getPosition()));
        }

        employee.setUser(savedUser);
        employee.setActive(true);

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

        List<Employee> employees = employeeRepository.findByPosition(position);
        return employeeMapper.toResponseDTOList(employees);
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartment(String department) {

        List<Employee> employees = employeeRepository.findByDepartment(department);
        return employeeMapper.toResponseDTOList(employees);
    }

    @Override
    public List<EmployeeResponseDTO> getActiveEmployees() {

        List<Employee> employees = employeeRepository.findByActiveTrue();
        return employeeMapper.toResponseDTOList(employees);
    }

    @Override
    public List<EmployeeResponseDTO> searchEmployeesByName(String name) {

        List<Employee> employees = employeeRepository.findByNameContaining(name);

        return employeeMapper.toResponseDTOList(employees);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO employeeRequestDTO) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        employee.setFirstName(employeeRequestDTO.getFirstName());
        employee.setLastName(employeeRequestDTO.getLastName());
        employee.setEmail(employeeRequestDTO.getEmail());
        employee.setPhoneNumber(employeeRequestDTO.getPhoneNumber());
        employee.setPosition(employeeRequestDTO.getPosition());
        employee.setDepartment(employeeRequestDTO.getDepartment());
        employee.setHireDate(employeeRequestDTO.getHireDate());
        employee.setSalary(employeeRequestDTO.getSalary());
        employee.setAddress(employeeRequestDTO.getAddress());
        employee.setEmergencyContact(employeeRequestDTO.getEmergencyContact());
        employee.setDateOfBirth(employeeRequestDTO.getDateOfBirth());
        employee.setShift(employeeRequestDTO.getShift());

        User user = employee.getUser();
        if (user != null) {
            user.setEmail(employeeRequestDTO.getEmail());
            user.setUserName(employeeRequestDTO.getFirstName() + " " + employeeRequestDTO.getLastName());
            user.setRole(employeeRequestDTO.getPosition().toUpperCase());
            userRepository.save(user);
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponseDTO(updatedEmployee);

    }

    @Override
    public void deactivateEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setActive(false);

        // Also deactivate the User account
        if (employee.getUser() != null) {
            employee.getUser().setActive(false);
            userRepository.save(employee.getUser());
        }

        employeeRepository.save(employee);
    }

    @Override
    public void activateEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setActive(true);

        // Also activate the User account
        if (employee.getUser() != null) {
            employee.getUser().setActive(true);
            userRepository.save(employee.getUser());
        }

        employeeRepository.save(employee);
    }

    @Override
    public void deleteEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        employee.setActive(false);
        if (employee.getUser() != null) {
            employee.getUser().setActive(false);
            userRepository.save(employee.getUser());
        }
        employeeRepository.save(employee);
    }

    @Override
    public long getEmployeesCount() {
        return employeeRepository.count();
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByShift(String shift) {

        List<Employee> employees = employeeRepository.findByShift(shift);
        return employeeMapper.toResponseDTOList(employees);
    }


    private String generateTemporaryPassword() {

        return UUID.randomUUID().toString().substring(0, 10);
    }
    public void resetEmployeePassword(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String newTempPassword = generateTemporaryPassword();

        if (employee.getUser() != null) {
            employee.getUser().setPassword(passwordEncoder.encode(newTempPassword));
            employee.getUser().setTemporaryPassword(true);
            employee.getUser().setPasswordExpiryDate(LocalDateTime.now().plusDays(7));
            userRepository.save(employee.getUser());
        }

        System.out.println("Password reset for " + employee.getEmail() + ": " + newTempPassword);
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
