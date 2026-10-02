package com.jobrec.user.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ConfirmSkillSuggestionsRequest(@NotNull List<UUID> suggestionIds) {}
