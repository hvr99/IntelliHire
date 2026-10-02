package com.jobrec.recommendation.semantic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Central configuration for blending the weighted and semantic scores. */
@Component
public final class SemanticScoringConfig {
    private final double blendFactor;

    public SemanticScoringConfig(@Value("${recommendation.semantic.blend:0.20}") double blendFactor) {
        if (!Double.isFinite(blendFactor) || blendFactor < 0.0 || blendFactor > 1.0) {
            throw new IllegalArgumentException("recommendation.semantic.blend must be between 0 and 1");
        }
        this.blendFactor = blendFactor;
    }

    public double blendFactor() { return blendFactor; }
}
