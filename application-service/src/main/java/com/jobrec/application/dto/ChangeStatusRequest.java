package com.jobrec.application.dto;
import com.jobrec.application.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
public record ChangeStatusRequest(@NotNull ApplicationStatus status) {}
