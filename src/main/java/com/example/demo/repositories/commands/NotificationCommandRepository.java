package com.example.demo.repositories.commands;

import com.example.demo.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationCommandRepository extends JpaRepository<Notification, Long> {
}
