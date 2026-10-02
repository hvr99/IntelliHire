package com.jobrec.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InternalRouteBlockerTest {
    @Test void internalRoutesAreBlocked() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/internal/jobs/1").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        new InternalRouteBlocker().filter(exchange, chain).block();
        assertEquals(403, exchange.getResponse().getStatusCode().value()); verifyNoInteractions(chain);
    }
    @Test void publicRoutesContinue() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/jobs").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class); when(chain.filter(exchange)).thenReturn(Mono.empty());
        new InternalRouteBlocker().filter(exchange, chain).block(); verify(chain).filter(exchange);
    }
}
