package com.jobrec.recommendation.semantic;

import com.jobrec.recommendation.domain.MatchResult;

public final class SemanticScoreBlender {
    private final SemanticScoringConfig config;

    public SemanticScoreBlender(SemanticScoringConfig config) { this.config = config; }

    public BlendedScore blend(MatchResult weighted, Double rawSemanticScore) {
        boolean used = rawSemanticScore != null && Double.isFinite(rawSemanticScore);
        if (!used) return new BlendedScore(round(weighted.totalScore()), null, false);
        double semantic = Math.max(0.0, Math.min(1.0, rawSemanticScore));
        double blended = weighted.totalScore() * (1.0 - config.blendFactor()) + semantic * 100.0 * config.blendFactor();
        return new BlendedScore(round(blended), semantic, true);
    }

    private static double round(double score) { return Math.round(Math.max(0.0, Math.min(100.0, score)) * 100.0) / 100.0; }
    public record BlendedScore(double score, Double semanticScore, boolean semanticUsed) {}
}
