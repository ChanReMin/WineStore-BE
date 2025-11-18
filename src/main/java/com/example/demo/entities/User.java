package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_deleted_at",
                        columnNames = {"email", "deleted_at"}
                ),
                @UniqueConstraint(
                        name = "uk_account_id_deleted_at",
                        columnNames = {"account_id", "deleted_at"}
                )
        },indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_account_id", columnList = "account_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(length = 500)
    private String avatar;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(columnDefinition = "SMALLINT")
    private Integer gender; // 0=unknown, 1=male, 2=female

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private java.util.List<UserAddress> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private java.util.List<Order> orders;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private java.util.List<Notification> notifications;
}