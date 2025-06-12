// UserService.java
package com.nikolas.leaflet.service;
import com.nikolas.leaflet.domain.User;
import com.nikolas.leaflet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerNewUser(String username, String email, String password) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password)); // Codificar la contraseña
        newUser.setRole("USER"); // Asigna un rol por defecto

        userRepository.save(newUser);
        logger.info("Nuevo usuario registrado: {}", username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Intentando cargar el usuario con nombre de usuario: {}", username);
    
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });
    
            logger.info("Usuario encontrado: {}. Preparando detalles de usuario.", username);
            return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    
        } catch (Exception e) {
            logger.error("Error en el método loadUserByUsername: {}", e.getMessage(), e);
            throw e;
        }
    }
    
}
