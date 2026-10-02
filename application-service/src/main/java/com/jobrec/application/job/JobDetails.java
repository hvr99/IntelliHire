package com.jobrec.application.job;
import java.util.UUID;
public record JobDetails(UUID id, UUID recruiterId, JobStatus status) {}
