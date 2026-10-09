package com.evfinder.controller;

import com.evfinder.dto.ChallengeResponse;
import com.evfinder.dto.SessionResponse;
import com.evfinder.dto.VerifyRequest;
import com.evfinder.exception.GlobalExceptionHandler;
import com.evfinder.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Valida as rotas de login com a carteira (/api/v1/auth)
class AuthControllerTest {

    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 10, 9, 12, 0);

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void devolveDesafioParaACarteira() throws Exception {
        when(authService.createChallenge("WALLET1"))
                .thenReturn(new ChallengeResponse("WALLET1", "NONCE1", "mensagem para assinar", EXPIRES_AT));

        mockMvc.perform(post("/api/v1/auth/challenge").contentType(MediaType.APPLICATION_JSON).content("""
                        {"walletAddress": "WALLET1"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nonce").value("NONCE1"))
                .andExpect(jsonPath("$.message").value("mensagem para assinar"));
    }

    @Test
    void retorna400QuandoFaltaACarteiraNoDesafio() throws Exception {
        mockMvc.perform(post("/api/v1/auth/challenge").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Campos inválidos: walletAddress é obrigatório"));

        verifyNoInteractions(authService);
    }

    @Test
    void devolveTokenQuandoAssinaturaEhValida() throws Exception {
        when(authService.verify(new VerifyRequest("WALLET1", "NONCE1", "ASSINATURA")))
                .thenReturn(new SessionResponse("token-novo", "WALLET1", EXPIRES_AT));

        mockMvc.perform(post("/api/v1/auth/verify").contentType(MediaType.APPLICATION_JSON).content("""
                        {"walletAddress": "WALLET1", "nonce": "NONCE1", "signature": "ASSINATURA"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-novo"))
                .andExpect(jsonPath("$.walletAddress").value("WALLET1"));
    }

    @Test
    void retorna400QuandoFaltamCamposNaVerificacao() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Campos inválidos: nonce é obrigatório; signature é obrigatório; walletAddress é obrigatório"));

        verifyNoInteractions(authService);
    }

    @Test
    void retorna401QuandoAssinaturaEhInvalida() throws Exception {
        when(authService.verify(any())).thenThrow(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura inválida para esta carteira."));

        mockMvc.perform(post("/api/v1/auth/verify").contentType(MediaType.APPLICATION_JSON).content("""
                        {"walletAddress": "WALLET1", "nonce": "NONCE1", "signature": "ERRADA"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Assinatura inválida para esta carteira."));
    }

    @Test
    void logoutEncerraASessaoDoToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isNoContent());

        verify(authService).logout("token-valido");
    }

    @Test
    void retorna401NoLogoutSemToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }
}
