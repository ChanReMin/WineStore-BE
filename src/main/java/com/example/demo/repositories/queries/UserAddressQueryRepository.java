package com.example.demo.repositories.queries;

import com.example.demo.entities.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAddressQueryRepository extends JpaRepository<UserAddress, Long> {
}
