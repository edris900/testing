package com.example.springcashier;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderModelRepository extends CrudRepository<Order, Long> {
    List<Order> findByRegister(String register);
}