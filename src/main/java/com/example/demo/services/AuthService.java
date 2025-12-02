package com.example.demo.services;
import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.configs.jwt.JwtService;
import com.example.demo.dtos.commands.auth.LoginRequestDto;
import com.example.demo.dtos.commands.auth.RefreshTokenRequestDto;
import com.example.demo.dtos.commands.auth.RegisterRequestDto;
import com.example.demo.dtos.responses.auth.LoginResponseDto;
import com.example.demo.dtos.responses.auth.RefreshTokenResponseDto;
import com.example.demo.dtos.responses.auth.RegisterResponseDto;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.repositories.commands.AccountCommandRepository;
import com.example.demo.repositories.commands.UserCommandRepository;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.repositories.queries.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserQueryRepository userServiceRepository;
    private final UserCommandRepository userCommandRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional(transactionManager = "writeTransactionManager")
    public RegisterResponseDto register(RegisterRequestDto request) {

        if (accountQueryRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("email", "Email is already in use");
        }

        LocalDate dob = request.getDateOfBirth();
        if (dob == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }

        if (!dob.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be today or in the future");
        }

        var newAccount = Account.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(AccountRole.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .build();

        var savedAccount = accountCommandRepository.save(newAccount);

        var newUser = User.builder()
                .account(savedAccount)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(dob)
                .gender(request.getGender())
                .build();

        var savedUser = userCommandRepository.save(newUser);

        return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .email(savedAccount.getEmail())
                .role(savedAccount.getRole().name())
                .build();
    }


    @Transactional(transactionManager = "writeTransactionManager")
    public LoginResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var account = accountCommandRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        var user = userServiceRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User details not found"));

        var jwtToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                account.getEmail(),
                account.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
        ));
        var refreshToken = jwtService.generateRefreshToken(new org.springframework.security.core.userdetails.User(
                account.getEmail(),
                account.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
        ));

        // Update refresh token in database
        account.setRefreshToken(refreshToken);
        accountCommandRepository.save(account);

        return LoginResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                // Assuming access token expiration is 1 hour (3600 seconds)
                .expiresIn(3600L)
                .user(LoginResponseDto.UserLoginResponseDto.builder()
                        .id(user.getId())
                        .email(account.getEmail())
                        .username(user.getFirstName() + " " + user.getLastName()) // Combine first and last name for username
                        .role(account.getRole().name())
                        .build())
                .build();
    }

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        final String refreshToken = request.getRefreshToken();
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new IllegalArgumentException("Invalid refresh token: User email not found in token");
        }

        var account = accountCommandRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with email: " + userEmail));

        if (!jwtService.validateRefreshToken(refreshToken) || !refreshToken.equals(account.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Generate a new access token
        var newAccessToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                account.getEmail(),
                account.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
        ));

        return RefreshTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000) // Convert ms to seconds
                .build();
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public void logout(String userEmail) {
        var account = accountCommandRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with email: " + userEmail));
        account.setRefreshToken(null);
        accountCommandRepository.save(account);
    }
}
