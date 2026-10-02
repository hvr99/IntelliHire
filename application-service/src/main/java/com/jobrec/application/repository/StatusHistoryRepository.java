package com.jobrec.application.repository;
import com.jobrec.application.domain.StatusHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {}
