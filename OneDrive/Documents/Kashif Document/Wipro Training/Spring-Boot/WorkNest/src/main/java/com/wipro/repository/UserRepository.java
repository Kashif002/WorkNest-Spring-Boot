package com.wipro.repository;

import com.wipro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    
    // Find users by role
    List<User> findByRole(String role);
    
    // Find users in a specific group (team members)
    @Query("SELECT u FROM User u JOIN u.groups g WHERE g.id = :groupId")
    List<User> findByGroupId(@Param("groupId") Integer groupId);
    
    // Find team members for a user (users who share at least one group)
    @Query("SELECT DISTINCT u FROM User u JOIN u.groups g WHERE g IN (SELECT g2 FROM Group g2 JOIN g2.members m WHERE m.id = :userId) AND u.id != :userId")
    List<User> findTeamMatesForUser(@Param("userId") Integer userId);
    
    // Find users who are not in any group
    @Query("SELECT u FROM User u WHERE u.groups IS EMPTY")
    List<User> findUsersWithoutGroup();
    
    // Find users by name pattern (for search functionality)
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Count users by role
    Long countByRole(String role);
}
