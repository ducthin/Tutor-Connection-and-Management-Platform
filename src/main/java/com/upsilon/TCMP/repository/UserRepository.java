package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.User;
import com.upsilon.TCMP.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByEmailContainingOrFullNameContaining(String email, String fullName);
    
    Long countByRoleAndActiveTrue(Role role);
    
    List<User> findTop10ByOrderByIdDesc();

    List<User> findByRole(Role role);

    List<User> findByActive(boolean active);

    Long countByRoleAndActive(Role role, boolean active);
}