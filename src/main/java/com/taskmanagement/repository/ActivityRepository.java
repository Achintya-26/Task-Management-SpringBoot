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
    
    /**
     * Find activity by ID with creator information eagerly loaded
     */
    @Query("SELECT a FROM Activity a LEFT JOIN FETCH a.creator LEFT JOIN FETCH a.team LEFT JOIN FETCH a.assignedMembers LEFT JOIN FETCH a.attachments LEFT JOIN FETCH a.links WHERE a.id = :id")
    Optional<Activity> findByIdWithCreator(@Param("id") Long id);
    
    /**
     * Find all activities with creator information eagerly loaded
     */
    @Query("SELECT a FROM Activity a LEFT JOIN FETCH a.creator LEFT JOIN FETCH a.team LEFT JOIN FETCH a.assignedMembers LEFT JOIN FETCH a.attachments LEFT JOIN FETCH a.links ORDER BY a.createdAt DESC")
    List<Activity> findAllWithCreator();
    
    /**
     * Find activities by team ID with creator information eagerly loaded
     */
    @Query("SELECT a FROM Activity a LEFT JOIN FETCH a.creator LEFT JOIN FETCH a.team LEFT JOIN FETCH a.assignedMembers LEFT JOIN FETCH a.attachments LEFT JOIN FETCH a.links WHERE a.team.id = :teamId ORDER BY a.createdAt DESC")
    List<Activity> findByTeamIdWithCreator(@Param("teamId") Long teamId);
}