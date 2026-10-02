package com.jobrec.user.ml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
public class MlClientConfig {
    @Bean
    @ConditionalOnProperty(name = "ml.client.mode", havingValue = "rest")
    MlClient restMlClient(@Value("${ml-service.base-url}") String baseUrl, @Value("${ml-service.connect-timeout-ms:1000}") long connectTimeout, @Value("${ml-service.read-timeout-ms:5000}") long readTimeout) { return new RestMlClient(baseUrl, connectTimeout, readTimeout); }

    @Bean
    @ConditionalOnMissingBean(MlClient.class)
    MlClient stubMlClient() { return new StubMlClient(); }
}
