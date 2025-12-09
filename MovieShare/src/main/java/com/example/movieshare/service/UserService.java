package com.example.movieshare.service;

import com.example.movieshare.dto.RegistrationForm;
import com.example.movieshare.model.AppUser;
import com.example.movieshare.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public AppUser registerNewUser(RegistrationForm form) {
        AppUser user = new AppUser();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEnabled(true);
        user.setRoles(Set.of("ROLE_USER"));
        return userRepository.save(user);
    }
}
