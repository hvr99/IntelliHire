package com.jobrec.recommendation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.Duration;

@Configuration
public class RestClientFactory {
    @Bean
    SimpleClientHttpRequestFactory downstreamRequestFactory(@Value("${recommendation.clients.connect-timeout-ms:1000}") int connectTimeout,
                                                             @Value("${recommendation.clients.read-timeout-ms:2000}") int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return factory;
    }
}
