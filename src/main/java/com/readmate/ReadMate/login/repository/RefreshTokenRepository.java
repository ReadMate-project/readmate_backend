package com.readmate.ReadMate.login.repository;

import com.readmate.ReadMate.login.entity.RefreshToken;
import com.readmate.ReadMate.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    RefreshToken findByUser(User user);

}