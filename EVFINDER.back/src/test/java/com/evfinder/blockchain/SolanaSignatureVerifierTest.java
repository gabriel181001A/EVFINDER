package com.evfinder.blockchain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SolanaSignatureVerifierTest {

    private final SolanaSignatureVerifier verifier = new SolanaSignatureVerifier();

    @Test
    void aceitaAssinaturaFeitaPelaCarteira() {
        TestWallet wallet = TestWallet.create();

        assertThat(verifier.verify(wallet.address(), "Mensagem com acentuação", wallet.sign("Mensagem com acentuação")))
                .isTrue();
    }

    @Test
    void recusaMensagemAlterada() {
        TestWallet wallet = TestWallet.create();

        assertThat(verifier.verify(wallet.address(), "Nonce: 2", wallet.sign("Nonce: 1"))).isFalse();
    }

    @Test
    void recusaAssinaturaDeOutraCarteira() {
        TestWallet wallet = TestWallet.create();
        TestWallet attacker = TestWallet.create();

        assertThat(verifier.verify(wallet.address(), "mensagem", attacker.sign("mensagem"))).isFalse();
    }

    @Test
    void recusaAssinaturaMalformada() {
        TestWallet wallet = TestWallet.create();

        assertThat(verifier.verify(wallet.address(), "mensagem", "não é base58")).isFalse();
        assertThat(verifier.verify(wallet.address(), "mensagem", "2NEpo7TZRRrLZSi2U")).isFalse(); // tamanho errado
        assertThat(verifier.verify(wallet.address(), "mensagem", null)).isFalse();
        assertThat(verifier.verify("abc", "mensagem", wallet.sign("mensagem"))).isFalse();
    }

    @Test
    void validaEnderecosDeCarteira() {
        assertThat(verifier.isValidAddress(TestWallet.create().address())).isTrue();
        assertThat(verifier.isValidAddress("So11111111111111111111111111111111111111112")).isTrue();
        assertThat(verifier.isValidAddress("A1B2C3D4E5")).isFalse(); // decodifica, mas não tem 32 bytes
        assertThat(verifier.isValidAddress("0xabc")).isFalse();      // caracteres fora do Base58
        assertThat(verifier.isValidAddress(null)).isFalse();
    }
}
