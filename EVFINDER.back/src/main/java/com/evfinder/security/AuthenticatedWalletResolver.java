package com.evfinder.security;

import com.evfinder.service.AuthService;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// Preenche os parâmetros @AuthenticatedWallet com a carteira dona do token da requisição
@Component
public class AuthenticatedWalletResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    public AuthenticatedWalletResolver(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedWallet.class)
                && parameter.getParameterType() == String.class;
    }

    @Override
    public String resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String token = BearerToken.from(webRequest.getHeader(HttpHeaders.AUTHORIZATION));
        return authService.authenticate(token);
    }
}
