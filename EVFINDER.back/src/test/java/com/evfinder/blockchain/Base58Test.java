package com.evfinder.blockchain;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Vetores de teste da especificação do Base58 (draft-msporny-base58)
class Base58Test {

    @Test
    void codificaVetoresConhecidos() {
        assertThat(Base58.encode("Hello World!".getBytes(StandardCharsets.UTF_8))).isEqualTo("2NEpo7TZRRrLZSi2U");
        assertThat(Base58.encode("The quick brown fox jumps over the lazy dog.".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo("USm3fpXnKG5EUBx2ndxBDMPVciP5hGey2Jh4NDv6gmeo1LkMeiKrLJUUBk6Z");
        assertThat(Base58.encode(HexFormat.of().parseHex("0000287fb4cd"))).isEqualTo("11233QC4");
    }

    @Test
    void decodificaVetoresConhecidos() {
        assertThat(new String(Base58.decode("2NEpo7TZRRrLZSi2U"), StandardCharsets.UTF_8)).isEqualTo("Hello World!");
        assertThat(HexFormat.of().formatHex(Base58.decode("11233QC4"))).isEqualTo("0000287fb4cd");
    }

    @Test
    void preservaBytesZeroNoInicio() {
        // Endereço do System Program da Solana: 32 bytes zero
        assertThat(Base58.decode("11111111111111111111111111111111")).hasSize(32).containsOnly(0);
        assertThat(Base58.encode(new byte[32])).isEqualTo("11111111111111111111111111111111");
    }

    @Test
    void recusaCaracteresForaDoAlfabeto() {
        // 0, O, I e l ficam fora do Base58 por serem fáceis de confundir
        assertThatThrownBy(() -> Base58.decode("abc0")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Base58.decode("abcO")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Base58.decode("ação")).isInstanceOf(IllegalArgumentException.class);
    }
}
