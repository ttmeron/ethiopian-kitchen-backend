package com.resturant.service;

import com.resturant.dto.UserDTO;
import com.resturant.dto.UserRegistrationDTO;

import java.util.List;


public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserByEmail(String email);

    UserDTO registerUser(UserRegistrationDTO registrationDTO);
    List<UserDTO> getAllUsers();
    UserDTO updateUserByEmail(String email, UserDTO userDTO);
    boolean deleteUserByEmail(String email);
    boolean emailExists(String email);


    }
