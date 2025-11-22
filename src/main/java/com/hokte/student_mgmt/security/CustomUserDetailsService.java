package com.hokte.student_mgmt.security;

import com.hokte.student_mgmt.models.Student;
import com.hokte.student_mgmt.models.User;
import com.hokte.student_mgmt.repo.StudentRepo;
import com.hokte.student_mgmt.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private StudentRepo studentRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with Email: " + email));
        Student student = studentRepo.findByUser(user).orElse(null);
        return UserPrincipal.fromUserEntity(user,student);
    }
}
