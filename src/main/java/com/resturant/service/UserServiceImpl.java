package com.resturant.service;

import com.resturant.dto.UserDTO;
import com.resturant.dto.UserRegistrationDTO;
import com.resturant.entity.User;
import com.resturant.exception.UserAlreadyExistsException;
import com.resturant.mapper.UserMapper;
import com.resturant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserDTO userDTO) {

        User user = userMapper.toEntity(userDTO);

        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }

    @Override
    public UserDTO getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found with email address: " + email));

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + registrationDTO.getEmail());
        }

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(registrationDTO.getUserName());
        userDTO.setEmail(registrationDTO.getEmail());
        userDTO.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        userDTO.setRole("USER"); // Default role

        return this.createUser(userDTO);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return  userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUserByEmail(String email, UserDTO userDTO) {

        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found with this email address: " + email));

        existingUser.setUserName(userDTO.getUserName());


        return userMapper.toDTO(userRepository.save(existingUser));
    }

    @Override
    public boolean deleteUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found with this email address: " + email));

        userRepository.delete(user);
        return true;
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
