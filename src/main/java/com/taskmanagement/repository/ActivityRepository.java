package com.taskmanagement.repository;

import com.taskmanagement.model.Activity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

	
	List<Activity> findByTeamId(Long teamId);
    
    /**
     * Find activities created by a specific user
     */
    List<Activity> findByCreatedBy(Long createdBy);
    
    /**
     * Find activities with custom query (if needed)
     */
    @Query("SELECT a FROM Activity a WHERE a.team.id = :teamId ORDER BY a.createdAt DESC")
    List<Activity> findByTeamIdOrderByCreatedAtDesc(@Param("teamId") Long teamId);
}