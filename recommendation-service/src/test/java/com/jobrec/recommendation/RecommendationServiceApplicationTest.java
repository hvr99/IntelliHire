package com.jobrec.recommendation;

import com.jobrec.recommendation.client.JobClient;
import com.jobrec.recommendation.client.ProfileClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "recommendation.clients.mode=stub")
class RecommendationServiceApplicationTest {
    @Autowired ProfileClient profileClient;
    @Autowired JobClient jobClient;
    @Test void startsIndependentlyInStubMode() { assertNotNull(profileClient); assertNotNull(jobClient); }
}
