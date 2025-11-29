package com.example.demo.services.commands;

import com.example.demo.entities.User;

public interface UserCommandService {
    User createUser(User user);
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
}
