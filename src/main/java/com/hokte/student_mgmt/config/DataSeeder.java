package com.hokte.student_mgmt.config;

import com.hokte.student_mgmt.models.Role;
import com.hokte.student_mgmt.models.User;
import com.hokte.student_mgmt.repo.RoleRepo;
import com.hokte.student_mgmt.repo.UserRepo;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Data
@RequiredArgsConstructor
public class DataSeeder {

    @Autowired
    private final RoleRepo roleRepo;

    @Autowired
    private final UserRepo userRepo;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        createRoleAndAdminIfNotExist();
    }

    private void createRoleAndAdminIfNotExist() {

        // Create roles if missing
        Role adminRole = roleRepo
                .findByName("ADMIN")
                .orElseGet(() -> roleRepo.save(Role.builder().name("ADMIN").build()));

        Role studentRole = roleRepo
                .findByName("STUDENT")
                .orElseGet(() -> roleRepo.save(Role.builder().name("STUDENT").build()));

        // Create default admin if not exists
        if(userRepo.findByEmail("admin@gmail.com").isEmpty()){
            User admin = User.builder()
                    .email("admin@gmail.com").
                    password(passwordEncoder.encode("admin@123"))
                    .role(adminRole)
                    .build();

            userRepo.save(admin);
            System.out.println("Default admin created successfully: admin@gmail.com and admin@123");
        }
    }
}
