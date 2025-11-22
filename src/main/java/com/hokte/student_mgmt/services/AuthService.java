package com.hokte.student_mgmt.services;

import com.hokte.student_mgmt.dto.AuthResponse;
import com.hokte.student_mgmt.dto.LoginRequest;
import com.hokte.student_mgmt.dto.RegisterRequest;
import com.hokte.student_mgmt.models.Role;
import com.hokte.student_mgmt.models.Student;
import com.hokte.student_mgmt.models.User;
import com.hokte.student_mgmt.repo.RoleRepo;
import com.hokte.student_mgmt.repo.StudentRepo;
import com.hokte.student_mgmt.repo.UserRepo;
import com.hokte.student_mgmt.security.JwtUtil;
import com.hokte.student_mgmt.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest registerRequest) {

        // check if email already in use
        if(userRepo.findByEmail(registerRequest.getEmail()).isPresent()){
            throw new RuntimeException("Email already in use");
        }

        Role role = roleRepo.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Default role STUDENT not found"));

        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .build();

        userRepo.save(user);

        Student student = studentRepo.findByUser(user).orElse(null);

        UserPrincipal principal = UserPrincipal.fromUserEntity(user,student);

        String token = jwtUtil.generateToken(principal);

        return new AuthResponse(token);

    }

    public AuthResponse login(LoginRequest loginRequest) {

        // When you pass the mail and password to authentication manager, so spring automatically authenticates it
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));

        // If no exception Authentication successful
        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

        Student student = studentRepo.findByUser(user).orElse(null);

        UserPrincipal principal = UserPrincipal.fromUserEntity(user,student);

        String token = jwtUtil.generateToken(principal);
        return new AuthResponse(token);
    }
}
