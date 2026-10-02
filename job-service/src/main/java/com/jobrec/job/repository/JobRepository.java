package com.jobrec.job.repository;

import com.jobrec.job.domain.entity.Job;
import com.jobrec.job.domain.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    Page<Job> findByRecruiterId(UUID recruiterId, Pageable pageable);

    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN j.skills s WHERE j.status = :status " +
           "AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%',:location,'%'))) " +
           "AND (:workMode IS NULL OR j.workMode = :workMode) " +
           "AND (:salaryMin IS NULL OR j.salaryMax >= :salaryMin) " +
           "AND (:skill IS NULL OR LOWER(s.name) = LOWER(:skill))")
    Page<Job> searchJobs(@Param("status") JobStatus status,
                         @Param("keyword") String keyword,
                         @Param("location") String location,
                         @Param("workMode") String workMode,
                         @Param("salaryMin") BigDecimal salaryMin,
                         @Param("skill") String skill,
                         Pageable pageable);

    List<Job> findAllByIdIn(List<UUID> ids);
    List<Job> findByStatus(JobStatus status);
}
