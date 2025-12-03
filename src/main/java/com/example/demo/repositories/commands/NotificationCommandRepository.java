package com.example.demo.repositories.commands;

import com.example.demo.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationCommandRepository extends JpaRepository<Notification, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Notification n
           SET n.isRead = true
           WHERE n.id = :notificationId
             AND n.user.id = :userId
           """)
    int markAsRead(@Param("userId") Long userId,
                   @Param("notificationId") Long notificationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Notification n
           SET n.isRead = true
           WHERE n.user.id = :userId
             AND n.isRead = false
           """)
    int markAllAsRead(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       DELETE FROM Notification n
       WHERE n.user.id = :userId
       """)
    void deleteByUserId(@Param("userId") Long userId);
}