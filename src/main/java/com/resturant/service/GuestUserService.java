package com.resturant.service;


import com.resturant.entity.User;
import com.resturant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;


    public User createTemporaryGuest(String email, String name){
        User guest = userRepository.findByEmail(email).orElseGet(()-> {
            User newGuest = new User();
            newGuest.setEmail((email));
            newGuest.setUserName(name);
            newGuest.setUserName(name);
            newGuest.setPassword(encoder.encode(UUID.randomUUID().toString()));
            newGuest.setRole("GUEST");
            return userRepository.save(newGuest);
        });
        return guest;
    }
}
