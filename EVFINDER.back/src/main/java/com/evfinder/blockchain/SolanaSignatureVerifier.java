package com.evfinder.blockchain;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.HexFormat;

// Confere assinaturas feitas por carteiras Solana (Ed25519), usando o suporte nativo do Java
@Component
public class SolanaSignatureVerifier {

    private static final int PUBLIC_KEY_LENGTH = 32;
    private static final int SIGNATURE_LENGTH = 64;

    // Cabeçalho X.509 de uma chave pública Ed25519. Somado aos 32 bytes do endereço,
    // forma a chave no formato que o KeyFactory do Java entende
    private static final byte[] ED25519_X509_PREFIX = HexFormat.of().parseHex("302a300506032b6570032100");

    // O endereço de uma carteira Solana é a própria chave pública: 32 bytes em Base58
    public boolean isValidAddress(String walletAddress) {
        return decode(walletAddress, PUBLIC_KEY_LENGTH) != null;
    }

    // true se a assinatura (Base58) da mensagem foi feita pela chave privada da carteira
    public boolean verify(String walletAddress, String message, String signature) {
        byte[] publicKeyBytes = decode(walletAddress, PUBLIC_KEY_LENGTH);
        byte[] signatureBytes = decode(signature, SIGNATURE_LENGTH);
        if (publicKeyBytes == null || signatureBytes == null) {
            return false;
        }

        byte[] encodedKey = new byte[ED25519_X509_PREFIX.length + PUBLIC_KEY_LENGTH];
        System.arraycopy(ED25519_X509_PREFIX, 0, encodedKey, 0, ED25519_X509_PREFIX.length);
        System.arraycopy(publicKeyBytes, 0, encodedKey, ED25519_X509_PREFIX.length, PUBLIC_KEY_LENGTH);

        try {
            PublicKey publicKey = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(encodedKey));
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(publicKey);
            verifier.update(message.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(signatureBytes);
        } catch (GeneralSecurityException e) {
            // Chave fora da curva ou assinatura malformada: não é uma assinatura válida
            return false;
        }
    }

    private static byte[] decode(String base58, int expectedLength) {
        if (base58 == null) {
            return null;
        }
        try {
            byte[] bytes = Base58.decode(base58);
            return bytes.length == expectedLength ? bytes : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
