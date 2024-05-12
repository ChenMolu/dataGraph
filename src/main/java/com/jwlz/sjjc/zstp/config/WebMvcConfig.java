package com.jwlz.sjjc.zstp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对跨域请求进行放行
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(false)
                .allowedMethods("POST","GET","DELETE","PUT","OPTIONS")
                .allowedOrigins("*")
                .allowedHeaders("*")
        ;
    }

}