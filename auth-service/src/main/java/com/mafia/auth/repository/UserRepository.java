package com.mafia.auth.repository;

import com.mafia.auth.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  
  Optional<User> findByEmail(String email);
  
  Optional<User> findByUsername(String username);
  
  boolean existsByEmail(String email);
  
  boolean existsByUsername(String username);
  
  List<User> findByUsernameContainingIgnoreCase(String username);
  
  Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
