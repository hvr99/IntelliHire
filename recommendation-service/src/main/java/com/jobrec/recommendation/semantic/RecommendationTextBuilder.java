package com.jobrec.recommendation.semantic;

import com.jobrec.recommendation.client.JobData;
import com.jobrec.recommendation.domain.CandidateProfile;
import com.jobrec.recommendation.domain.JobRequirements;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public final class RecommendationTextBuilder {
    private RecommendationTextBuilder() {}

    public static String candidateText(CandidateProfile profile) {
        if (profile == null) return "";
        return join(profile.getSkills(), profile.getExperienceTitles(), profile.getSummary());
    }

    public static String jobText(JobData job) {
        if (job == null) return "";
        JobRequirements requirements = job.requirements();
        return join(job.title(), requirements == null ? null : requirements.getDescription(),
                requirements == null ? null : requirements.getRequiredSkills(),
                requirements == null ? null : requirements.getPreferredSkills());
    }

    private static String join(Object... values) {
        return java.util.Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(value -> value instanceof Collection<?> collection
                        ? collection.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(", "))
                        : value.toString())
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" | "));
    }
}
