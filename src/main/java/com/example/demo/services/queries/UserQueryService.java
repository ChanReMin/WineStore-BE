package com.example.demo.services.queries;

import com.example.demo.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    List<User> searchUsers(String name);
}
