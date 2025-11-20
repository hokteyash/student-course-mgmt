package com.hokte.student_mgmt.controllers;

import com.hokte.student_mgmt.dto.CourseDto;
import com.hokte.student_mgmt.services.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping("/createCourse")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto dto){
        return ResponseEntity.ok(courseService.createCourse(dto));
    }


    @GetMapping("/")
    public ResponseEntity<List<CourseDto>> getAllCourses(){
        return ResponseEntity.ok(courseService.getAllCourses());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<CourseDto> deleteCourse(@PathVariable Long id){
        return ResponseEntity.ok(courseService.deleteCourse(id));
    }
}
