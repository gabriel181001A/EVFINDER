package com.evfinder.service;

import com.evfinder.blockchain.SolanaSignatureVerifier;
import com.evfinder.blockchain.TestWallet;
import com.evfinder.config.AuthProperties;
import com.evfinder.dto.ChallengeResponse;
import com.evfinder.dto.SessionResponse;
import com.evfinder.dto.VerifyRequest;
import com.evfinder.entity.AuthChallenge;
import com.evfinder.entity.AuthSession;
import com.evfinder.repository.AuthChallengeRepository;
import com.evfinder.repository.AuthSessionRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Login com a carteira, usando assinaturas Ed25519 reais geradas pela TestWallet
class AuthServiceTest {

    private static final String INVALID_CHALLENGE = "Desafio inválido ou expirado. Peça um novo em /api/v1/auth/challenge.";
    private static final String INVALID_SESSION = "Sessão inválida ou expirada. Entre novamente com a sua carteira.";

    private AuthChallengeRepository challengeRepository;
    private AuthSessionRepository sessionRepository;
    private AuthService service;
    private TestWallet wallet;

    @BeforeEach
    void setUp() {
        challengeRepository = mock(AuthChallengeRepository.class);
        sessionRepository = mock(AuthSessionRepository.class);
        service = new AuthService(challengeRepository, sessionRepository, new SolanaSignatureVerifier(),
                new AuthProperties(Duration.ofMinutes(5), Duration.ofDays(7)));
        wallet = TestWallet.create();
    }

    @Test
    void criaDesafioComCarteiraENonceNaMensagem() {
        ChallengeResponse response = service.createChallenge(wallet.address());

        assertThat(response.message())
                .contains("Carteira: " + wallet.address())
                .contains("Nonce: " + response.nonce());
        assertThat(response.expiresAt()).isBetween(LocalDateTime.now().plusMinutes(4), LocalDateTime.now().plusMinutes(5));

        ArgumentCaptor<AuthChallenge> saved = ArgumentCaptor.forClass(AuthChallenge.class);
        verify(challengeRepository).save(saved.capture());
        assertThat(saved.getValue().getNonce()).isEqualTo(response.nonce());
        assertThat(saved.getValue().getMessage()).isEqualTo(response.message());
        verify(challengeRepository).deleteByExpiresAtBefore(any());
    }

    @Test
    void geraNoncesDiferentesACadaDesafio() {
        String first = service.createChallenge(wallet.address()).nonce();
        String second = service.createChallenge(wallet.address()).nonce();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void recusaDesafioParaEnderecoInvalido() {
        assertThatThrownBy(() -> service.createChallenge("A1B2C3D4E5"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(challengeRepository, never()).save(any());
    }

    @Test
    void abreSessaoQuandoAssinaturaEhValida() {
        AuthChallenge challenge = challengeFor(wallet.address(), LocalDateTime.now().plusMinutes(5));

        SessionResponse response = service.verify(
                new VerifyRequest(wallet.address(), "NONCE1", wallet.sign(challenge.getMessage())));

        assertThat(response.walletAddress()).isEqualTo(wallet.address());
        assertThat(response.token()).isNotBlank();
        assertThat(response.expiresAt()).isAfter(LocalDateTime.now().plusDays(6));
        verify(challengeRepository).delete(challenge);

        // O banco guarda só o hash do token, nunca o token
        ArgumentCaptor<AuthSession> saved = ArgumentCaptor.forClass(AuthSession.class);
        verify(sessionRepository).save(saved.capture());
        assertThat(saved.getValue().getWalletAddress()).isEqualTo(wallet.address());
        assertThat(saved.getValue().getTokenHash())
                .isEqualTo(AuthService.sha256(response.token()))
                .isNotEqualTo(response.token());
    }

    @Test
    void recusaAssinaturaDeOutraCarteira() {
        AuthChallenge challenge = challengeFor(wallet.address(), LocalDateTime.now().plusMinutes(5));
        String attackerSignature = TestWallet.create().sign(challenge.getMessage());

        assertUnauthorized(() -> service.verify(new VerifyRequest(wallet.address(), "NONCE1", attackerSignature)),
                "Assinatura inválida para esta carteira.");

        verify(challengeRepository, never()).delete(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void recusaDesafioExpirado() {
        AuthChallenge challenge = challengeFor(wallet.address(), LocalDateTime.now().minusSeconds(1));

        assertUnauthorized(() -> service.verify(
                new VerifyRequest(wallet.address(), "NONCE1", wallet.sign(challenge.getMessage()))), INVALID_CHALLENGE);
    }

    @Test
    void recusaDesafioPedidoPorOutraCarteira() {
        // Outra carteira tenta usar o nonce que foi emitido para "wallet"
        AuthChallenge challenge = challengeFor(wallet.address(), LocalDateTime.now().plusMinutes(5));
        TestWallet other = TestWallet.create();

        assertUnauthorized(() -> service.verify(
                new VerifyRequest(other.address(), "NONCE1", other.sign(challenge.getMessage()))), INVALID_CHALLENGE);
    }

    @Test
    void recusaNonceDesconhecido() {
        when(challengeRepository.findByNonce("NAO_EXISTE")).thenReturn(Optional.empty());

        assertUnauthorized(() -> service.verify(
                new VerifyRequest(wallet.address(), "NAO_EXISTE", wallet.sign("x"))), INVALID_CHALLENGE);
    }

    @Test
    void autenticaTokenValido() {
        sessionFor("token-valido", LocalDateTime.now().plusDays(1));

        assertThat(service.authenticate("token-valido")).isEqualTo(wallet.address());
    }

    @Test
    void recusaTokenVencido() {
        sessionFor("token-vencido", LocalDateTime.now().minusSeconds(1));

        assertUnauthorized(() -> service.authenticate("token-vencido"), INVALID_SESSION);
    }

    @Test
    void recusaTokenDesconhecido() {
        when(sessionRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertUnauthorized(() -> service.authenticate("token-inventado"), INVALID_SESSION);
    }

    @Test
    void logoutApagaASessaoDoToken() {
        service.logout("token-valido");

        verify(sessionRepository).deleteByTokenHash(AuthService.sha256("token-valido"));
    }

    private AuthChallenge challengeFor(String walletAddress, LocalDateTime expiresAt) {
        AuthChallenge challenge = new AuthChallenge();
        challenge.setWalletAddress(walletAddress);
        challenge.setNonce("NONCE1");
        challenge.setMessage("EVFinder: assine esta mensagem para entrar com a sua carteira.\n\n"
                + "Carteira: " + walletAddress + "\nNonce: NONCE1");
        challenge.setExpiresAt(expiresAt);
        when(challengeRepository.findByNonce("NONCE1")).thenReturn(Optional.of(challenge));
        return challenge;
    }

    private void sessionFor(String token, LocalDateTime expiresAt) {
        AuthSession session = new AuthSession();
        session.setWalletAddress(wallet.address());
        session.setTokenHash(AuthService.sha256(token));
        session.setExpiresAt(expiresAt);
        when(sessionRepository.findByTokenHash(AuthService.sha256(token))).thenReturn(Optional.of(session));
    }

    private static void assertUnauthorized(ThrowingCallable call, String message) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getReason()).isEqualTo(message);
        });
    }
}
