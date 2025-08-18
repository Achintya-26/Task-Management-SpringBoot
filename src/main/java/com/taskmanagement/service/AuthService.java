package com.taskmanagement.service;

import com.taskmanagement.dto.AuthRequest;
import com.taskmanagement.dto.AuthResponse;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(AuthRequest authRequest) {
        Optional<User> existingUser = userRepository.findByEmpId(authRequest.getEmpId());
        if (existingUser.isPresent()) {
            return new AuthResponse(null, "User already exists with this Employee ID");
        }

        User user = new User();
        user.setEmpId(authRequest.getEmpId());
        user.setName(authRequest.getName());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        user.setRole("user"); // Default role
        
        User savedUser = userRepository.save(user);

        String token = generateTokenForUser(savedUser);
        return new AuthResponse(token, "User registered successfully");
    }

    public AuthResponse login(AuthRequest authRequest) {
        Optional<User> userOpt = userRepository.findByEmpId(authRequest.getEmpId());
        
        if (!userOpt.isPresent()) {
            return new AuthResponse(null, "User not found");
        }

        User user = userOpt.get();
        
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            return new AuthResponse(null, "Invalid credentials");
        }

        String token = generateTokenForUser(user);
        return new AuthResponse(token, "Login successful");
    }

    private String generateTokenForUser(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("empId", user.getEmpId());
        claims.put("name", user.getName());
        claims.put("role", user.getRole());
        
        return jwtUtil.generateToken(user.getEmpId(), claims);
    }

    public User getCurrentUserFromToken(String token) {
        try {
            String empId = jwtUtil.extractUsername(token);
            return userRepository.findByEmpId(empId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}