package com.evfinder.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;

// Carteira Solana de teste: um par de chaves Ed25519 gerado na hora
public record TestWallet(String address, PrivateKey privateKey) {

    public static TestWallet create() {
        try {
            KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            byte[] x509 = keyPair.getPublic().getEncoded();
            // Os 32 últimos bytes do formato X.509 são a chave pública pura, que é o endereço da carteira
            byte[] publicKey = Arrays.copyOfRange(x509, x509.length - 32, x509.length);
            return new TestWallet(Base58.encode(publicKey), keyPair.getPrivate());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    // Faz o mesmo que o signMessage de uma carteira como a Phantom: assina os bytes UTF-8
    // da mensagem. O frontend então converte a assinatura para Base58
    public String sign(String message) {
        try {
            Signature signer = Signature.getInstance("Ed25519");
            signer.initSign(privateKey);
            signer.update(message.getBytes(StandardCharsets.UTF_8));
            return Base58.encode(signer.sign());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
