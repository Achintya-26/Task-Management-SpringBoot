package com.taskmanagement.repository;

import com.taskmanagement.model.ActivityLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ActivityLinkRepository extends JpaRepository<ActivityLink, Long> {
    List<ActivityLink> findByActivityId(Long activityId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ActivityLink al WHERE al.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);
}
