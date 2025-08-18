package com.taskmanagement.repository;

import com.taskmanagement.model.Remark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemarkRepository extends JpaRepository<Remark, Long> {
    
    /**
     * Find all remarks for a specific activity, ordered by creation date
     */
    List<Remark> findByActivityIdOrderByCreatedAtDesc(Long activityId);
    
    /**
     * Find remarks by activity ID and type
     */
    List<Remark> findByActivityIdAndTypeOrderByCreatedAtDesc(Long activityId, Remark.RemarkType type);
    
    /**
     * Find remarks created by a specific user for an activity
     */
    List<Remark> findByActivityIdAndUserIdOrderByCreatedAtDesc(Long activityId, Long userId);
    
    /**
     * Count total remarks for an activity
     */
    @Query("SELECT COUNT(r) FROM Remark r WHERE r.activityId = :activityId")
    long countByActivityId(@Param("activityId") Long activityId);
}
