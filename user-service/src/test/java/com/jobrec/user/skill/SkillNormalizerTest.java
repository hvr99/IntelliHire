package com.jobrec.user.skill;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillNormalizerTest {
    @Test void sharedNormalizationFixtures() {
        var result = new SkillNormalizer().normalize(List.of("JS", " Python 3.12 ", "react.js", "JS", "Unknown Tool", "C++"));
        assertEquals(List.of("javascript", "python", "react", "unknown tool", "cpp"), result.stream().map(SkillNormalizer.NormalizedSkill::skill).toList());
        assertEquals(List.of(true, true, true, false, true), result.stream().map(SkillNormalizer.NormalizedSkill::matched).toList());
    }
}
