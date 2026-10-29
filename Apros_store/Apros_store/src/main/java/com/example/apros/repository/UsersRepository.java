package com.example.apros.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apros.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Integer id);
}
