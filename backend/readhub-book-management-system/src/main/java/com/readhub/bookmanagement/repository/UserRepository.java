package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Role;
import com.readhub.bookmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // We keep this available, but UserService is now using findAll() + Filter
    // which is safer for debugging.
    List<User> findByRole(Role role);

    long countByRole(Role role);
}
