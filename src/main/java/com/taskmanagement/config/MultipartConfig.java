package com.taskmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        // Set to true to defer parsing until we're ready
        resolver.setStrictServletCompliance(true);
        return resolver;
    }
}
