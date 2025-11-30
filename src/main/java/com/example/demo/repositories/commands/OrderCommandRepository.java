
package com.example.demo.repositories.commands;

import com.example.demo.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface OrderCommandRepository extends JpaRepository<Order, Long> {

}