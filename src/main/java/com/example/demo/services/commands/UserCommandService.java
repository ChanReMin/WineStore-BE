package com.example.demo.services.commands;

import com.example.demo.entities.User;
import com.example.demo.repositories.commands.UserCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCommandService {
    private final UserCommandRepository userCommandRepository;

    @Transactional(transactionManager = "writeTransactionManager")
    public User createUser(User user) {
        return userCommandRepository.save(user);
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public User updateUser(Long id, User userDetails) {
        User user = userCommandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(userDetails.getFirstName());
        user.setPhoneNumber(userDetails.getPhoneNumber());

        return userCommandRepository.save(user);
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public void deleteUser(Long id) {
        userCommandRepository.deleteById(id);
    }
}
