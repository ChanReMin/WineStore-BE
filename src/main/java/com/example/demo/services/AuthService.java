package com.example.demo.services;

import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.dtos.auth.RegisterRequestDto;
import com.example.demo.dtos.auth.RegisterResponseDto;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.RoleRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.exceptions.DuplicateResourceException;
import java.time.LocalDateTime;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("email", "Email này đã được đăng ký");
        }
        var customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Customer role not found"));

        var newAccount = Account.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(customerRole)
                .status(AccountStatus.ACTIVE)
                .build();
        //add DateTime for  account
        newAccount.setCreatedAt(LocalDateTime.now());

        var savedAccount = accountRepository.save(newAccount);

        var newUser = User.builder()
                .account(savedAccount)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null)
                .gender(request.getGender() != null ? request.getGender().getValue() : null)
                .build();

        var savedUser = userRepository.save(newUser);

        return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(customerRole.getCode())
                .build();
    }
}
