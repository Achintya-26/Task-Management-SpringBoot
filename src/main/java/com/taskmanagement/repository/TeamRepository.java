package com.taskmanagement.repository;

import com.taskmanagement.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    // Find teams where the user is a member
    @Query("SELECT DISTINCT t FROM Team t JOIN t.members m WHERE m.id = :userId")
    List<Team> findTeamsByUserId(@Param("userId") Long userId);
    
    // Additional query methods can be defined here if needed
}