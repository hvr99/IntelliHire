package com.jobrec.notification.dto;

import com.jobrec.notification.domain.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateNotificationRequest(@NotNull UUID userId, @NotNull NotificationType type,
                                        @NotBlank String title, @NotBlank String message, UUID refId) {}
