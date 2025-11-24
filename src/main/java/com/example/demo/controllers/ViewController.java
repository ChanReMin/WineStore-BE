package com.example.demo.controllers;

import com.example.demo.services.queries.UserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    
    @Autowired
    private UserQueryService userQueryService;
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("users", userQueryService.getAllUsers());
        return "index";
    }
}