package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.entities.User;
import com.example.demo.repositories.queries.UserServiceRepository;
import com.example.demo.services.queries.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {
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
