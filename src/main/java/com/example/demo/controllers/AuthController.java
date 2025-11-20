package com.example.demo.controllers;

import com.example.demo.dtos.commands.auth.LoginRequestDto;
import com.example.demo.dtos.responses.auth.LoginResponseDto;
import com.example.demo.dtos.commands.auth.RefreshTokenRequestDto;
import com.example.demo.dtos.responses.auth.RefreshTokenResponseDto;
import com.example.demo.dtos.commands.auth.RegisterRequestDto;
import com.example.demo.dtos.responses.auth.RegisterResponseDto;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        var data = authService.login(request);
        SuccessResponse<LoginResponseDto> response = SuccessResponse.<LoginResponseDto>builder()
                .success(true)
                .message("Đăng nhập thành công")
                .data(data)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<RefreshTokenResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        var data = authService.refreshToken(request);
        SuccessResponse<RefreshTokenResponseDto> response = SuccessResponse.<RefreshTokenResponseDto>builder()
                .success(true)
                .message("Làm mới token thành công")
                .data(data)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.logout(userEmail);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .success(true)
                .message("Đăng xuất thành công")
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
