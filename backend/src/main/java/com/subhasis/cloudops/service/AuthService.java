package com.subhasis.cloudops.service;

import com.subhasis.cloudops.config.JwtService;
import com.subhasis.cloudops.dto.AuthResponse;
import com.subhasis.cloudops.dto.LoginRequest;
import com.subhasis.cloudops.dto.RegisterRequest;
import com.subhasis.cloudops.entity.Role;
import com.subhasis.cloudops.entity.User;
import com.subhasis.cloudops.exception.EmailAlreadyExistsException;
import com.subhasis.cloudops.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuthenticationManager authManager;

    public AuthService(UserRepository users, PasswordEncoder encoder,
                       JwtService jwt, AuthenticationManager authManager) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.authManager = authManager;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().toLowerCase().trim();
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        User u = new User();
        u.setName(req.name());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.USER);              // role is never taken from the request
        users.save(u);
        return new AuthResponse(jwt.generateToken(email), email, u.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().toLowerCase().trim();
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        User u = users.findByEmail(email).orElseThrow();
        return new AuthResponse(jwt.generateToken(email), email, u.getRole().name());
    }
}
