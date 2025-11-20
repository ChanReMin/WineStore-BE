package com.example.demo.services;

import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.dtos.auth.RegisterRequestDto;
import com.example.demo.dtos.auth.RegisterResponseDto;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("email", "Email này đã được đăng ký");
        }


        var newAccount = Account.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(AccountRole.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .build();

        var savedAccount = accountRepository.save(newAccount);

        var newUser = User.builder()
                .account(savedAccount)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth().toLocalDate() : null)
                .gender(request.getGender())
                .build();

        var savedUser = userRepository.save(newUser);

        return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .email(savedAccount.getEmail())
                .role(savedAccount.getRole().name())
                .build();
    }
}
