package com.jobrec.recommendation.semantic;

import com.jobrec.recommendation.client.JobData;
import com.jobrec.recommendation.domain.CandidateProfile;
import com.jobrec.recommendation.domain.JobRequirements;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SemanticMatcherTest {
    @Test void noOpReturnsEmptyResult() {
        assertTrue(new NoOpSemanticMatcher().match(new CandidateProfile(), List.of()).isEmpty());
    }

    @Test void mlSuccessMapsJobIdsAndScores() throws Exception {
        UUID id = UUID.randomUUID();
        try (TestServer server = new TestServer(200, "{\"matches\":[{\"jobId\":\"" + id + "\",\"similarity\":0.82}]}", 0)) {
            Map<UUID, Double> result = new MlSemanticMatcher("http://localhost:" + server.port(), 500)
                    .match(new CandidateProfile(), List.of(job(id)));
            assertEquals(0.82, result.get(id));
        }
    }

    @Test void mlUnavailableReturnsEmptyResult() throws Exception {
        try (TestServer server = new TestServer(503, "unavailable", 0)) {
            assertTrue(new MlSemanticMatcher("http://localhost:" + server.port(), 500)
                    .match(new CandidateProfile(), List.of(job(UUID.randomUUID()))).isEmpty());
        }
    }

    @Test void mlTimeoutReturnsEmptyResult() throws Exception {
        try (TestServer server = new TestServer(200, "{}", 300)) {
            assertTrue(new MlSemanticMatcher("http://localhost:" + server.port(), 25)
                    .match(new CandidateProfile(), List.of(job(UUID.randomUUID()))).isEmpty());
        }
    }

    private static JobData job(UUID id) { return new JobData(id, "Backend", "Acme", "Remote", new JobRequirements()); }

    private static final class TestServer implements AutoCloseable {
        private final HttpServer server;
        TestServer(int status, String body, long delayMs) throws IOException {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/similarity", exchange -> respond(exchange, status, body, delayMs));
            server.start();
        }
        int port() { return server.getAddress().getPort(); }
        @Override public void close() { server.stop(0); }
        private static void respond(HttpExchange exchange, int status, String body, long delayMs) throws IOException {
            exchange.getRequestBody().readAllBytes();
            try { Thread.sleep(delayMs); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        }
    }
}
