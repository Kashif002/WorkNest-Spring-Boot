package com.wipro.controller;

import com.wipro.dto.AuthResponse;
import com.wipro.entity.User;
import com.wipro.security.JwtUtil;
import com.wipro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.wipro.repository.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestParam String email, @RequestParam String password) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, password));
        } catch (BadCredentialsException e) {
            // Fallback: manual match in case provider is misconfigured in some envs
            User dbUser = userRepository.findByEmail(normalizedEmail).orElse(null);
            if (dbUser == null || !passwordEncoder.matches(password, dbUser.getPassword())) {
                return ResponseEntity.status(401).body("Incorrect email or password");
            }
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(normalizedEmail);
        final String jwt = jwtUtil.generateToken(userDetails);
        User user = (User) userDetails;
        return ResponseEntity.ok(new AuthResponse(jwt, user.getRole(), user.getName()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        try {
            if (user.getEmail() != null) {
                user.setEmail(user.getEmail().trim().toLowerCase());
            }
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("USER"); // Store without ROLE_ prefix
            } else if (user.getRole().startsWith("ROLE_")) {
                // Remove ROLE_ prefix if provided
                user.setRole(user.getRole().substring(5));
            }
            
            User savedUser = userService.save(user);
            return ResponseEntity.ok("User registered successfully! You can now login with your credentials.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    User user = (User) userDetails;
                    return ResponseEntity.ok(new AuthResponse(jwt, user.getRole(), user.getName()));
                }
            }
            return ResponseEntity.status(401).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // For JWT, logout is handled client-side by removing tokens
        // This endpoint is mainly for consistency
        return ResponseEntity.ok("Logged out successfully");
    }
}