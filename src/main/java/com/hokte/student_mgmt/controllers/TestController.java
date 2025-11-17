package com.hokte.student_mgmt.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("")
    public String greet(){
        return "Hello World";
    }
}
