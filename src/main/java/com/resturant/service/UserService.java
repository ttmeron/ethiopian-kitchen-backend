package com.resturant.service;

import com.resturant.dto.UserDTO;
import com.resturant.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    UserDTO updateUserByEmail(String email, UserDTO userDTO);
    boolean deleteUserByEmail(String email);

    }
