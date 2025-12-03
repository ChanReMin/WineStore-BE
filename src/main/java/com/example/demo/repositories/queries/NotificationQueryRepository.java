package com.example.demo.repositories.queries;

import com.example.demo.commons.enums.NotificationStatus;
import com.example.demo.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationQueryRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(Long userId,
                                                                       Pageable pageable);

    long countByUser_IdAndIsReadFalse(Long userId);

    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

    Page<Notification> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId,
                                                                  NotificationStatus status,
                                                                  Pageable pageable);
}