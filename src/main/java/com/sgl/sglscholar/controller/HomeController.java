package com.sgl.sglscholar.controller;

import com.sgl.sglscholar.dto.SchoolDTO;
import com.sgl.sglscholar.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private SchoolService schoolService;

    // Serve home.html
    @GetMapping("/")
    public String showHomePage() {
        return "home";
    }


}
