package com.taskmanagement.repository;

import com.taskmanagement.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByActivityId(Long activityId);
    
    @Modifying
    @Transactional
    void deleteByActivityId(Long activityId);
}
