package com.jobrec.recommendation.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/** Catalog-backed normalization. The catalog is packaged from the repository's shared/skills/skills.json. */
public final class SkillNormalizer {
    private static final Pattern VERSION_SUFFIX = Pattern.compile("\\s+v?\\d+(?:\\.\\d+)*$");
    private final Map<String, String> aliases;

    public SkillNormalizer() { aliases = loadCatalog(); }

    public NormalizedSkill normalizeOne(String value) {
        String cleaned = clean(value);
        String canonical = aliases.get(cleaned);
        if (canonical != null) return new NormalizedSkill(canonical, true);
        String versionless = VERSION_SUFFIX.matcher(cleaned).replaceFirst("").trim();
        canonical = aliases.get(versionless);
        return new NormalizedSkill(canonical == null ? cleaned : canonical, canonical != null);
    }

    public List<NormalizedSkill> normalize(Collection<String> values) {
        LinkedHashMap<String, NormalizedSkill> unique = new LinkedHashMap<>();
        if (values != null) for (String value : values) {
            if (value == null) continue;
            NormalizedSkill result = normalizeOne(value);
            if (!result.skill().isEmpty()) unique.putIfAbsent(result.skill(), result);
        }
        return List.copyOf(unique.values());
    }

    public Set<String> normalizeToSet(Collection<String> values) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (NormalizedSkill skill : normalize(values)) result.add(skill.skill());
        return result;
    }

    public record NormalizedSkill(String skill, boolean matched) {}

    private static String clean(String value) {
        if (value == null) return "";
        String lower = value.toLowerCase(Locale.ROOT);
        lower = lower.replaceAll("(?<=[a-z0-9])[./]+(?=[a-z0-9])", "");
        StringBuilder result = new StringBuilder();
        for (char ch : lower.toCharArray()) result.append(Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || ch == '#' || ch == '+' ? ch : ' ');
        return result.toString().trim().replaceAll("\\s+", " ");
    }

    private static Map<String, String> loadCatalog() {
        try (InputStream stream = SkillNormalizer.class.getResourceAsStream("/skills/skills.json")) {
            if (stream == null) throw new IllegalStateException("Shared skills catalog is missing from the classpath");
            JsonNode root = new ObjectMapper().readTree(stream);
            Map<String, String> result = new HashMap<>();
            for (JsonNode item : root) {
                String canonical = item.get("name").asText();
                result.put(clean(canonical), canonical);
                for (JsonNode alias : item.get("aliases")) result.put(clean(alias.asText()), canonical);
            }
            return Map.copyOf(result);
        } catch (Exception exception) { throw new IllegalStateException("Unable to load shared skills catalog", exception); }
    }
}
