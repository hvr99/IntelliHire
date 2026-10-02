package com.jobrec.recommendation.matching;

/** Single source of truth for matching weights. Values must sum to 1.0. */
public final class MatchingWeights {
    public static final double DEFAULT_SKILLS = 0.40;
    public static final double DEFAULT_EXPERIENCE = 0.20;
    public static final double DEFAULT_CERTIFICATION = 0.15;
    public static final double DEFAULT_PREFERENCES = 0.10;
    public static final double DEFAULT_EDUCATION = 0.10;
    public static final double DEFAULT_OTHER = 0.05;
    private static final double EPSILON = 0.000001;

    private final double skills;
    private final double experience;
    private final double certification;
    private final double preferences;
    private final double education;
    private final double other;

    public MatchingWeights(double skills, double experience, double certification, double preferences, double education, double other) {
        this.skills = skills; this.experience = experience; this.certification = certification;
        this.preferences = preferences; this.education = education; this.other = other;
        validate();
    }
    public static MatchingWeights defaults() { return new MatchingWeights(DEFAULT_SKILLS, DEFAULT_EXPERIENCE, DEFAULT_CERTIFICATION, DEFAULT_PREFERENCES, DEFAULT_EDUCATION, DEFAULT_OTHER); }
    public void validate() {
        double total = skills + experience + certification + preferences + education + other;
        if (Double.isNaN(total) || Math.abs(total - 1.0) > EPSILON || skills < 0 || experience < 0 || certification < 0 || preferences < 0 || education < 0 || other < 0)
            throw new IllegalArgumentException("Matching weights must be non-negative and total exactly 1.0");
    }
    public double skills() { return skills; } public double experience() { return experience; }
    public double certification() { return certification; } public double preferences() { return preferences; }
    public double education() { return education; } public double other() { return other; }
}
