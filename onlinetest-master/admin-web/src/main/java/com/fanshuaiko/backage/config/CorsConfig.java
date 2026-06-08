package com.fanshuaiko.backage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 1. 允许任何源
        corsConfiguration.addAllowedHeader("*"); // 2. 允许任何头
        corsConfiguration.addAllowedMethod("*"); // 3. 允许任何方法（GET, POST, etc）
        source.registerCorsConfiguration("/**", corsConfiguration); // 4. 对所有接口生效
        return new CorsFilter(source);
    }
}
