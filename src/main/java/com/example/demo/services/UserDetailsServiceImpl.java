package com.example.demo.services;

import com.example.demo.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return accountRepository.findByEmail(email)
                .map(account -> new User(
                        account.getEmail(),
                        account.getPasswordHash(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (account.getRole() != null ? account.getRole().name() : "UNKNOWN")))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with email: " + email));
    }
}
