package com.taskmanagement.repository;

import com.taskmanagement.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
    
    List<Notification> findByUserIdAndRelatedTeamIdOrderByCreatedAtDesc(Long userId, Long teamId);
    
    List<Notification> findByUserIdAndRelatedActivityIdOrderByCreatedAtDesc(Long userId, Long activityId);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndIsReadFalse(Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.relatedActivityId = :activityId")
    int deleteByRelatedActivityId(@Param("activityId") Long activityId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.relatedTeamId = :teamId")
    int deleteByRelatedTeamId(@Param("teamId") Long teamId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}