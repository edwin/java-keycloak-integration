package com.edw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * <pre>
 *     com.edw.config.RestTemplateConfig
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 18 Agt 2020 21:46
 */
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
