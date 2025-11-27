package com.example.demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
public class OAuth2RedirectController {
    @GetMapping("/redirect")
    public ResponseEntity<String> handleRedirect(@RequestParam("token") String token) {
        return ResponseEntity.ok("Authentication successful. Token: " + token);
    }
}