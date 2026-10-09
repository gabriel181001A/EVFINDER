package com.evfinder.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Marca o parâmetro do controller que recebe a carteira dona do token
// enviado em "Authorization: Bearer <token>". Sem token válido, a resposta é 401.
// Quem preenche o parâmetro é o AuthenticatedWalletResolver.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedWallet {
}
