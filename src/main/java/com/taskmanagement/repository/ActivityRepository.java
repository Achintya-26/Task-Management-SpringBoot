package com.taskmanagement.repository;

import com.taskmanagement.model.Activity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

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
    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.creator LEFT JOIN FETCH a.team LEFT JOIN FETCH a.assignedMembers WHERE a.id = :id")
    Optional<Activity> findByIdWithCreator(@Param("id") Long id);
    
    /**
     * Find all activities with creator information eagerly loaded
     */
    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.creator LEFT JOIN FETCH a.team LEFT JOIN FETCH a.assignedMembers ORDER BY a.createdAt DESC")
    List<Activity> findAllWithCreator();
    
    /**
     * Find activities by team ID with creator information eagerly loaded
     * Using two-step approach to avoid duplicates from collection joins
     */
    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.creator LEFT JOIN FETCH a.team WHERE a.team.id = :teamId ORDER BY a.createdAt DESC")
    List<Activity> findByTeamIdWithCreator(@Param("teamId") Long teamId);
    
    /**
     * Find activities by team ID with all relationships loaded (alternative approach)
     */
    @Query("SELECT a FROM Activity a WHERE a.team.id = :teamId ORDER BY a.createdAt DESC")
    List<Activity> findByTeamIdSimple(@Param("teamId") Long teamId);
    
    /**
     * Delete all activities for a specific team
     * Note: This will cascade to delete related remarks, attachments, etc. due to JPA cascade settings
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Activity a WHERE a.team.id = :teamId")
    int deleteByTeamId(@Param("teamId") Long teamId);
}