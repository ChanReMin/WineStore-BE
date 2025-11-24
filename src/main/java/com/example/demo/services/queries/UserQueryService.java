package com.example.demo.services.queries;

import com.example.demo.entities.User;
import com.example.demo.repositories.queries.UserServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserServiceRepository userServiceRepository;

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public List<User> getAllUsers() {
        return userServiceRepository.findAll();
    }

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userServiceRepository.findById(id);
    }


    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public List<User> searchUsers(String name) {
        return userServiceRepository.findByFirstNameContaining(name);
    }
}
