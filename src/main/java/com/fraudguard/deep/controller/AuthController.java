package com.fraudguard.deep.controller;

import com.fraudguard.hardik.model.user.User;
import com.fraudguard.deep.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@jakarta.validation.Valid @RequestBody com.fraudguard.deep.dto.LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.username());
        if (user != null && user.getPassword().equals(request.password())) {
            session.setAttribute("user", user);
            return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole(),
                "accountId", user.getAccountId() != null ? user.getAccountId() : ""
            ));
        }
        return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@jakarta.validation.Valid @RequestBody com.fraudguard.deep.dto.SignupRequest request) {
        if (userRepository.findByUsername(request.username()) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
        }
        
        userRepository.save(
            request.username(),
            request.password(),
            request.fullName(),
            "CUSTOMER",
            request.accountId()
        );
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole(),
                "accountId", user.getAccountId() != null ? user.getAccountId() : ""
            ));
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
