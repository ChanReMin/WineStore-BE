package com.example.demo.controllers;

import com.example.demo.dtos.auth.RegisterRequestDto;
import com.example.demo.dtos.auth.RegisterResponseDto;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<RegisterResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        var data = authService.register(request);
        SuccessResponse<RegisterResponseDto> response = SuccessResponse.<RegisterResponseDto>builder()
                .success(true)
                .message("Đăng ký thành công")
                .data(data)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
