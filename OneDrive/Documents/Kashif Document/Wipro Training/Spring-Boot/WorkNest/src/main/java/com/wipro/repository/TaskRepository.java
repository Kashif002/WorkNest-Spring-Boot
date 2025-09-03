package com.wipro.repository;

import com.wipro.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAssignedUsers_Id(Integer userId);
    List<Task> findByAssignedBy_Id(Integer userId);
    
    // Find tasks by status
    List<Task> findByStatus(String status);
    
    // Find overdue tasks that are not completed or delayed
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status NOT IN ('COMPLETED', 'DELAYED')")
    List<Task> findOverdueTasks(@Param("currentDate") Date currentDate);
    
    // Find tasks assigned to users in a specific group
    @Query("SELECT DISTINCT t FROM Task t JOIN t.assignedUsers u JOIN u.groups g WHERE g.id = :groupId")
    List<Task> findTasksByGroupId(@Param("groupId") Integer groupId);
    
    // Find tasks by multiple assignees (for team task assignment)
    @Query("SELECT t FROM Task t JOIN t.assignedUsers u WHERE u.id IN :userIds")
    List<Task> findTasksByAssignedUserIds(@Param("userIds") List<Integer> userIds);
    
    // Find tasks created within date range
    @Query("SELECT t FROM Task t WHERE t.startDate BETWEEN :startDate AND :endDate")
    List<Task> findTasksInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Count tasks by status
    Long countByStatus(String status);
    
    // Find tasks that can be reassigned (not completed by admin)
    @Query("SELECT t FROM Task t WHERE t.completedByAdmin = false OR t.completedByAdmin IS NULL")
    List<Task> findReassignableTasks();
}
