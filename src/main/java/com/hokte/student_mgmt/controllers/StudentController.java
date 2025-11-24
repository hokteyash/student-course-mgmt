package com.hokte.student_mgmt.controllers;

import com.hokte.student_mgmt.dto.ChangePasswordDto;
import com.hokte.student_mgmt.dto.StudentDto;
import com.hokte.student_mgmt.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/")
    public ResponseEntity<List<StudentDto>> getAllStudents(){
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @PostMapping("/createStudent")
    public ResponseEntity<StudentDto> createStudent(@RequestBody StudentDto studentDto){
        return ResponseEntity.ok(studentService.createStudent(studentDto));
    }

    // STUDENT or ADMIN: get profile
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id){
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    // changing the students password
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable Long id, @RequestBody ChangePasswordDto changePasswordDto){
        studentService.changePassword(id,changePasswordDto);
        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StudentDto> deleteStudentById(@PathVariable Long id){
        return ResponseEntity.ok(studentService.deleteStudent(id));
    }


}
