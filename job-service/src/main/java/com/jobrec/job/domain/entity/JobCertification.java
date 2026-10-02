package com.jobrec.job.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "job_certifications")
public class JobCertification {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "job_id", nullable = false) private Job job;
    @Column(nullable = false) private String name;

    public JobCertification() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
