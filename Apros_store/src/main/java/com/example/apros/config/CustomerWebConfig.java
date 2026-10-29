package com.example.apros.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CustomerWebConfig implements WebMvcConfigurer {
    private final CustomerActiveInterceptor activeInterceptor;

    public CustomerWebConfig(CustomerActiveInterceptor activeInterceptor) {
        this.activeInterceptor = activeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(activeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/**", "/css/**", "/js/**", "/images/**", "/error");
    }
}
