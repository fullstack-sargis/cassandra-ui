package com.example.cassandraui.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private static final String API_PATH_PATTERN = "/api/**";
  private static final String ANY_ORIGIN = "*";
  private static final String GET_METHOD = "GET";
  private static final String OPTIONS_METHOD = "OPTIONS";
  private static final String POST_METHOD = "POST";

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping(API_PATH_PATTERN)
        .allowedOriginPatterns(ANY_ORIGIN)
        .allowedMethods(GET_METHOD, POST_METHOD, OPTIONS_METHOD)
        .allowedHeaders(ANY_ORIGIN);
  }
}
