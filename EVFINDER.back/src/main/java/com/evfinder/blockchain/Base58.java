package com.evfinder.blockchain;

import java.math.BigInteger;
import java.util.Arrays;

// Codificação Base58 usada pela Solana em endereços de carteira e assinaturas
public final class Base58 {

    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(58);
    private static final int[] INDEXES = new int[128];

    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length(); i++) {
            INDEXES[ALPHABET.charAt(i)] = i;
        }
    }

    private Base58() {}

    public static String encode(byte[] input) {
        BigInteger value = new BigInteger(1, input);
        StringBuilder result = new StringBuilder();
        while (value.signum() > 0) {
            BigInteger[] quotientAndRemainder = value.divideAndRemainder(BASE);
            result.append(ALPHABET.charAt(quotientAndRemainder[1].intValue()));
            value = quotientAndRemainder[0];
        }
        // Cada byte zero no início vira um "1"
        for (int i = 0; i < input.length && input[i] == 0; i++) {
            result.append(ALPHABET.charAt(0));
        }
        return result.reverse().toString();
    }

    public static byte[] decode(String input) {
        BigInteger value = BigInteger.ZERO;
        for (char c : input.toCharArray()) {
            int digit = c < 128 ? INDEXES[c] : -1;
            if (digit < 0) {
                throw new IllegalArgumentException("Caractere inválido em Base58: " + c);
            }
            value = value.multiply(BASE).add(BigInteger.valueOf(digit));
        }

        byte[] bytes = value.signum() == 0 ? new byte[0] : value.toByteArray();
        // O BigInteger pode acrescentar um byte zero de sinal, que não faz parte do valor
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }

        int leadingZeros = 0;
        while (leadingZeros < input.length() && input.charAt(leadingZeros) == ALPHABET.charAt(0)) {
            leadingZeros++;
        }

        byte[] result = new byte[leadingZeros + bytes.length];
        System.arraycopy(bytes, 0, result, leadingZeros, bytes.length);
        return result;
    }
}
