package com.example.demo.repositories;

import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByFirstNameContaining(String name);

    Optional<User> findByAccountId(Long accountId);

}
