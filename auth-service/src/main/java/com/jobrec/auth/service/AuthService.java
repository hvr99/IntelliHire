package com.jobrec.auth.service;

import com.jobrec.auth.domain.entity.User;
import com.jobrec.auth.dto.AuthResponse;
import com.jobrec.auth.dto.LoginRequest;
import com.jobrec.auth.dto.RegisterRequest;
import com.jobrec.auth.exception.InvalidCredentialsException;
import com.jobrec.auth.exception.UserAlreadyExistsException;
import com.jobrec.auth.repository.UserRepository;
import com.jobrec.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        
        User user = new User(
                UUID.randomUUID(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                true,
                LocalDateTime.now()
        );
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
