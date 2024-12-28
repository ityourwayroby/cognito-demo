package com.sgl.sglscholar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
public class DashboardController {

    @GetMapping("/administrators")
    public String administratorsDashboard() {
        return "administrators"; // Returns administrators.html
    }

    @GetMapping("/teachers")
    public String teachersDashboard() {
        return "teachers"; // Returns teachers.html
    }

    @GetMapping("/students")
    public String studentsDashboard() {
        return "students"; // Returns students.html
    }
}

