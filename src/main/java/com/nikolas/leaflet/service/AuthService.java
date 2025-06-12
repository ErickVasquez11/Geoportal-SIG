package com.nikolas.leaflet.service;

import com.nikolas.leaflet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public boolean authenticate(String username, String rawPassword) {
        // Busca el usuario en la base de datos
        return userRepository.findByUsername(username)
            .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
            .orElse(false); // Si no se encuentra el usuario, retorna false
    }
}
