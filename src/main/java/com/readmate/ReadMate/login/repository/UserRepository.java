package com.readmate.ReadMate.login.repository;

import com.readmate.ReadMate.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(Long userId);
    User findByNickname(String nickname);
    boolean existsByNickname(String nickname);
}
