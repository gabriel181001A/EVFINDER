package com.evfinder.config;

import com.evfinder.security.AuthenticatedWalletResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticatedWalletResolver authenticatedWalletResolver;

    public WebConfig(AuthenticatedWalletResolver authenticatedWalletResolver) {
        this.authenticatedWalletResolver = authenticatedWalletResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica para todas as rotas da API
                .allowedOrigins("*") // Permite bater do localhost:3000 ou 5173 do seu React
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    // Habilita o @AuthenticatedWallet nos controllers
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedWalletResolver);
    }
}
