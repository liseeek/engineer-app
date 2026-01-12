package com.example.medhub.repository;

import com.example.medhub.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByUserEmail(String userEmail);
    List<AuditLogEntity> findByResourceId(Long resourceId);
}
