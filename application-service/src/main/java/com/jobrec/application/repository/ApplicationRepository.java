package com.jobrec.application.repository;
import com.jobrec.application.domain.Application;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId);
    Optional<Application> findByIdAndCandidateId(UUID id, UUID candidateId);
    List<Application> findByCandidateIdOrderByCreatedAtDesc(UUID candidateId);
    List<Application> findByJobIdOrderByCreatedAtDesc(UUID jobId);
}
