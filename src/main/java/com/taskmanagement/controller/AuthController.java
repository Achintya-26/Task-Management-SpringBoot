package com.taskmanagement.controller;

import com.taskmanagement.dto.AuthRequest;
import com.taskmanagement.dto.AuthResponse;
import com.taskmanagement.model.User;
import com.taskmanagement.service.AuthService;
import com.taskmanagement.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
 // Secure endpoint to get current user info
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                User user = authService.getCurrentUserFromToken(token);
                
                if (user != null) {
                    // Don't return password
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                }
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    @GetMapping("/verify-role/{role}")
    public ResponseEntity<Map<String, Boolean>> verifyRole(@PathVariable String role, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Map<String, Boolean> response = new HashMap<>();
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String userRole = jwtUtil.extractRole(token);
                boolean hasRole = role.equalsIgnoreCase(userRole) || 
                                 (role.equalsIgnoreCase("user") && userRole.equalsIgnoreCase("admin"));
                
                response.put("hasRole", hasRole);
                return ResponseEntity.ok(response);
            }
        }
        
        response.put("hasRole", false);
        return ResponseEntity.ok(response);
    }

//	Admin-only endpoint example
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAdminData() {
        return ResponseEntity.ok("Admin data");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.register(authRequest);
        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
//    	System.out.println(authRequest);
        AuthResponse response = authService.login(authRequest);
//        System.out.println(response);
        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}