package com.ims.expo.controller; // Update to match your package structure!

import com.ims.expo.entity.Role;
import com.ims.expo.entity.User;
import com.ims.expo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // 1. Verify they are actually an Admin
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Not an Administrator.");
            }
            
            // 2. Verify the hashed password matches
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Success! We send back the Admin's details.
                return ResponseEntity.ok(user); 
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
    }
}