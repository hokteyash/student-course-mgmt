package com.hokte.student_mgmt.config;

import com.hokte.student_mgmt.models.Role;
import com.hokte.student_mgmt.repo.RoleRepo;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@RequiredArgsConstructor
public class DataSeeder {

    @Autowired
    private final RoleRepo roleRepo;

    @PostConstruct
    public void init() {
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("STUDENT");
    }

    private void createRoleIfNotExists(String roleName) {
        if(roleRepo.findByName(roleName).isEmpty()) {
            Role role = Role.builder().name(roleName).build();
            roleRepo.save(role);
        }
    }
}
