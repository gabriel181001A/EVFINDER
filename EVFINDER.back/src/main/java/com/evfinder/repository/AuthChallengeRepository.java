package com.evfinder.repository;

import com.evfinder.entity.AuthChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthChallengeRepository extends JpaRepository<AuthChallenge, Long> {

    Optional<AuthChallenge> findByNonce(String nonce);

    // Limpa desafios que foram pedidos e nunca assinados
    void deleteByExpiresAtBefore(LocalDateTime now);

}
