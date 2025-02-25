package com.upsilon.TCMP.repository;

import com.upsilon.TCMP.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserIdAndType(Integer userId, VerificationToken.TokenType type);
    void deleteByUserIdAndType(Integer userId, VerificationToken.TokenType type);
}