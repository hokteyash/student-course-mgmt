package com.hokte.student_mgmt.services;

import com.hokte.student_mgmt.dto.StudentDto;
import com.hokte.student_mgmt.models.Course;
import com.hokte.student_mgmt.models.Role;
import com.hokte.student_mgmt.models.Student;
import com.hokte.student_mgmt.models.User;
import com.hokte.student_mgmt.repo.CourseRepo;
import com.hokte.student_mgmt.repo.RoleRepo;
import com.hokte.student_mgmt.repo.StudentRepo;
import com.hokte.student_mgmt.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private StudentDto toDto(Student s) {
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setFirstName(s.getFirstName());
        dto.setLastName(s.getLastName());
        dto.setEmail(s.getUser() != null ? s.getUser().getEmail() : null);
        if (s.getCourses() != null) dto.setCourseIds(s.getCourses().stream().map(Course::getId).collect(Collectors.toSet()));
        return dto;
    }

    // ADMIN only: create a student (and user)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudentDto createStudent(StudentDto studentDto) {

        // check for duplicate student
        if(userRepo.findByEmail(studentDto.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists");
        }

        // fetching the role to store in the user
        Role studentRole = roleRepo.findByName("STUDENT").orElseThrow(() -> new RuntimeException("Student Role Not Found"));

        // creating the User because Student is a User, Student <-> User
        User user = User.builder()
                .email(studentDto.getEmail())
                .password(passwordEncoder.encode("password"))   // default password, we will ask user to change password
                .role(studentRole)
                .build();

        // saving the user
        user =  userRepo.save(user);

        // since Student is having many-to-many relationship with course so will create course set
        Set<Course> courses = new HashSet<>();
        if(studentDto.getCourseIds() != null){
            courses = studentDto.getCourseIds()
                    .stream().map(id -> courseRepo.findById(id).orElseThrow(() -> new RuntimeException("Course not found: " + id)))
                    .collect(Collectors.toSet());
        }

        // Filling the student object
        Student student = Student.builder()
                .firstName(studentDto.getFirstName())
                .lastName(studentDto.getLastName())
                .user(user)
                .courses(courses)
                .build();

        // saving the student in the db
        student = studentRepo.save(student);

        studentDto.setId(student.getId());

        return studentDto;
    }

    // Restrict to Admin only
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudentDto> getAllStudents() {
        return studentRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    // Admin only Delete student and it's user
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudentDto deleteStudent(Long id) {
        Student student = studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        // user also needs to delete
        User user = student.getUser();
        studentRepo.delete(student);
        if(user!=null) userRepo.delete(user);
        return toDto(student);
    }

    // Admin only Delete student and it's user
    @PreAuthorize("hasAnyRole('ADMIN') or #id == authentication.principal.studentId")
    public StudentDto getStudentById(Long id) {
        Student student = studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        return toDto(student);
    }
}
