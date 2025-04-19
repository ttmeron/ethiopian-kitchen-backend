package com.resturant.service;

import com.resturant.dto.UserDTO;
import com.resturant.entity.User;
import com.resturant.mapper.UserMapper;
import com.resturant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserMapper userMapper;

    @Override
    public UserDTO createUser(UserDTO userDTO) {

        User user = userMapper.toEntity(userDTO);

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found with email address: " + email));

        return userMapper.toDTO(user);
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
}
