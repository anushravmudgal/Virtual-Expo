package com.ims.expo.config; // Update to match your package structure!

import com.ims.expo.entity.Role;
import com.ims.expo.entity.User;
import com.ims.expo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class AdminDataSeeder {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner seedAdmins(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // The default password for all new faculty accounts (they can change it later)
            String defaultPassword = passwordEncoder.encode("IMSadmin2026!");

            List<String[]> facultyList = List.of(
                new String[]{"Anushrav Sir", "anushrav@imsnoida.edu"},
                new String[]{"Anjali Ma'am", "anjali.pandey@imsnoida.edu"},
                new String[]{"Dr. Ajay Gupta", "hodsoit@imsnoida.edu"},
                new String[]{"Dr. Jyoti ma'am", "jyotikumari.soit@imsnoida.edu"}
            );

            for (String[] faculty : facultyList) {
                if (userRepository.findByEmail(faculty[1]).isEmpty()) {
                    User admin = new User();
                    admin.setUsername(faculty[0]);
                    admin.setEmail(faculty[1]);
                    admin.setPassword(defaultPassword);
                    admin.setRole(Role.ADMIN);
                    admin.setApproved(true);
                    admin.setProfileComplete(true);
                    userRepository.save(admin);
                    System.out.println("Seeded Admin Account: " + faculty[0]);
                }
            }
        };
    }
}