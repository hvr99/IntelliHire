package com.jobrec.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"AUTH_SERVICE_URL=http://127.0.0.1:1", "USER_SERVICE_URL=http://127.0.0.1:1",
                "JOB_SERVICE_URL=http://127.0.0.1:1", "APPLICATION_SERVICE_URL=http://127.0.0.1:1",
                "RECOMMENDATION_SERVICE_URL=http://127.0.0.1:1", "NOTIFICATION_SERVICE_URL=http://127.0.0.1:1"})
class GatewayRoutingTest {
    @LocalServerPort int port;
    @Autowired WebTestClient client;

    @Test void everyPublicPrefixIsRoutedWithoutStartingDownstreams() {
        Stream.of("/api/auth/login", "/api/profile/me", "/api/candidates/me/profile", "/api/jobs", "/api/applications",
                "/api/recommendations", "/api/recruiter/jobs", "/api/notifications")
                .forEach(path -> client.get().uri("http://localhost:" + port + path).exchange().expectStatus().value(status -> assertNotEquals(404, status)));
    }

    @Test void invalidRouteReturns404() {
        client.get().uri("http://localhost:" + port + "/api/does-not-exist").exchange().expectStatus().isNotFound();
    }

    @Test void internalRoutesAreBlocked() {
        client.get().uri("http://localhost:" + port + "/internal/jobs/1").exchange().expectStatus().isForbidden();
    }

    @Test void corsPreflightIsHandledAtGateway() {
        client.options().uri("http://localhost:" + port + "/api/jobs")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange().expectStatus().isOk().expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000");
    }
}
