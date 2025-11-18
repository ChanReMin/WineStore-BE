package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // ADMIN, SELLER, CUSTOMER

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "role")
    private List<Account> accounts;
}