package com.hokte.student_mgmt.services;

import com.hokte.student_mgmt.dto.CourseDto;
import com.hokte.student_mgmt.models.Course;
import com.hokte.student_mgmt.repo.CourseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepo courseRepo;

    private CourseDto toDto(Course c) {
        CourseDto dto = new CourseDto();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        return dto;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CourseDto createCourse(CourseDto courseDto) {
        if(courseRepo.findByCode(courseDto.getCode()).isPresent()) {
            throw new RuntimeException("Code already exists");
        }
        Course course = Course.builder()
                .code(courseDto.getCode())
                .title(courseDto.getTitle())
                .description(courseDto.getDescription())
                .build();
        course = courseRepo.save(course);
        courseDto.setId(course.getId());
        return courseDto;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CourseDto deleteCourse(Long id){
        Course course =  courseRepo.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
        courseRepo.delete(course);
        return toDto(course);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public List<CourseDto> getAllCourses(){
        return courseRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }
}
