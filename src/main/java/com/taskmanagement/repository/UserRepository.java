package com.taskmanagement.repository;

import com.taskmanagement.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);

	Optional<User> findByEmpId(String id);
}
