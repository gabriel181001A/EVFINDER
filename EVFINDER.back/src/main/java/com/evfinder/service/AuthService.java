package com.evfinder.service;

import com.evfinder.blockchain.Base58;
import com.evfinder.blockchain.SolanaSignatureVerifier;
import com.evfinder.config.AuthProperties;
import com.evfinder.dto.ChallengeResponse;
import com.evfinder.dto.SessionResponse;
import com.evfinder.dto.VerifyRequest;
import com.evfinder.entity.AuthChallenge;
import com.evfinder.entity.AuthSession;
import com.evfinder.repository.AuthChallengeRepository;
import com.evfinder.repository.AuthSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

// Login com a carteira Solana: o usuário assina uma mensagem com um nonce aleatório,
// o que prova que ele tem a chave privada da carteira, e recebe um token de sessão
@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthChallengeRepository challengeRepository;
    private final AuthSessionRepository sessionRepository;
    private final SolanaSignatureVerifier signatureVerifier;
    private final AuthProperties properties;

    public AuthService(AuthChallengeRepository challengeRepository, AuthSessionRepository sessionRepository,
                       SolanaSignatureVerifier signatureVerifier, AuthProperties properties) {
        this.challengeRepository = challengeRepository;
        this.sessionRepository = sessionRepository;
        this.signatureVerifier = signatureVerifier;
        this.properties = properties;
    }

    @Transactional
    public ChallengeResponse createChallenge(String walletAddress) {
        if (!signatureVerifier.isValidAddress(walletAddress)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "walletAddress não é um endereço de carteira Solana válido.");
        }

        LocalDateTime now = LocalDateTime.now();
        challengeRepository.deleteByExpiresAtBefore(now);

        String nonce = Base58.encode(randomBytes(16));
        String message = "EVFinder: assine esta mensagem para entrar com a sua carteira.\n\n"
                + "Carteira: " + walletAddress + "\n"
                + "Nonce: " + nonce;

        AuthChallenge challenge = new AuthChallenge();
        challenge.setWalletAddress(walletAddress);
        challenge.setNonce(nonce);
        challenge.setMessage(message);
        challenge.setExpiresAt(now.plus(properties.challengeDuration()));
        challengeRepository.save(challenge);

        return new ChallengeResponse(walletAddress, nonce, message, challenge.getExpiresAt());
    }

    @Transactional
    public SessionResponse verify(VerifyRequest request) {
        LocalDateTime now = LocalDateTime.now();

        AuthChallenge challenge = challengeRepository.findByNonce(request.nonce())
                .filter(c -> c.getWalletAddress().equals(request.walletAddress()))
                .filter(c -> c.getExpiresAt().isAfter(now))
                .orElseThrow(() -> unauthorized("Desafio inválido ou expirado. Peça um novo em /api/v1/auth/challenge."));

        if (!signatureVerifier.verify(challenge.getWalletAddress(), challenge.getMessage(), request.signature())) {
            throw unauthorized("Assinatura inválida para esta carteira.");
        }

        // Cada desafio só pode ser usado uma vez
        challengeRepository.delete(challenge);
        sessionRepository.deleteByExpiresAtBefore(now);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(32));

        AuthSession session = new AuthSession();
        session.setWalletAddress(challenge.getWalletAddress());
        session.setTokenHash(sha256(token));
        session.setCreatedAt(now);
        session.setExpiresAt(now.plus(properties.sessionDuration()));
        sessionRepository.save(session);

        return new SessionResponse(token, session.getWalletAddress(), session.getExpiresAt());
    }

    // Devolve a carteira dona do token, ou 401 se o token não existir ou estiver vencido
    @Transactional(readOnly = true)
    public String authenticate(String token) {
        return sessionRepository.findByTokenHash(sha256(token))
                .filter(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(AuthSession::getWalletAddress)
                .orElseThrow(() -> unauthorized("Sessão inválida ou expirada. Entre novamente com a sua carteira."));
    }

    @Transactional
    public void logout(String token) {
        sessionRepository.deleteByTokenHash(sha256(token));
    }

    static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Todo Java tem SHA-256, então isso não acontece na prática
            throw new IllegalStateException(e);
        }
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
