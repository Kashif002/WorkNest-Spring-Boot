package com.wipro.service;

import com.wipro.dto.TaskDto;
import com.wipro.entity.Task;
import com.wipro.entity.User;
import com.wipro.repository.TaskRepository;
import com.wipro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<TaskDto> findAll() {
        return taskRepository.findAll().stream().map(TaskDto::new).collect(Collectors.toList());
    }
    
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<TaskDto> findById(Integer id) {
        return taskRepository.findById(id).map(TaskDto::new);
    }

    public List<TaskDto> findTasksForUser(Integer userId) {
        return taskRepository.findByAssignedUsers_Id(userId).stream().map(TaskDto::new).collect(Collectors.toList());
    }
    
    public List<Task> findTaskEntitiesForUser(Integer userId) {
        return taskRepository.findByAssignedUsers_Id(userId);
    }
    
    public Task findTaskById(Integer id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }
    
    // Enhanced CRUD operations
    
    /**
     * Create a new task with assigned users
     */
    public Task createTask(Task task, List<Integer> userIds, User assignedBy) {
        task.setAssignedBy(assignedBy);
        
        if (userIds != null && !userIds.isEmpty()) {
            Set<User> assignedUsers = new HashSet<>();
            for (Integer userId : userIds) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    assignedUsers.add(user);
                }
            }
            task.setAssignedUsers(assignedUsers);
        }
        
        return taskRepository.save(task);
    }
    
    /**
     * Update task details
     */
    public Task updateTask(Integer id, Task updatedTask) {
        Task existingTask = taskRepository.findById(id).orElse(null);
        if (existingTask != null) {
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setStartDate(updatedTask.getStartDate());
            existingTask.setDueDate(updatedTask.getDueDate());
            return taskRepository.save(existingTask);
        }
        return null;
    }
    
    /**
     * Update task status (with admin completion tracking)
     */
    public Task updateTaskStatus(Integer id, String status, User updater) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            // Check if task was already completed by admin
            if (task.getCompletedByAdmin() != null && task.getCompletedByAdmin() && !"ADMIN".equals(updater.getRole())) {
                throw new IllegalStateException("Task has been completed by admin and cannot be modified by users");
            }
            
            task.setStatus(status);
            
            // If admin is marking as completed, set the flag
            if ("COMPLETED".equals(status) && "ADMIN".equals(updater.getRole())) {
                task.setCompletedByAdmin(true);
                task.setCompletedAt(new Date());
            }
            
            return taskRepository.save(task);
        }
        return null;
    }
    
    /**
     * Reassign task to different users
     */
    public Task reassignTask(Integer id, List<Integer> newUserIds) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            Set<User> newAssignedUsers = new HashSet<>();
            for (Integer userId : newUserIds) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    newAssignedUsers.add(user);
                }
            }
            task.setAssignedUsers(newAssignedUsers);
            return taskRepository.save(task);
        }
        return null;
    }
    
    /**
     * Delete a task
     */
    public boolean deleteTask(Integer id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Find tasks by status
     */
    public List<Task> findTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }
    
    /**
     * Get dashboard statistics
     */
    public Map<String, Long> getTaskStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) taskRepository.findAll().size());
        stats.put("pending", taskRepository.countByStatus("PENDING"));
        stats.put("inProgress", taskRepository.countByStatus("IN_PROGRESS"));
        stats.put("completed", taskRepository.countByStatus("COMPLETED"));
        stats.put("delayed", taskRepository.countByStatus("DELAYED"));
        return stats;
    }
    
    /**
     * Find tasks for a team (group)
     */
    public List<Task> findTasksByGroupId(Integer groupId) {
        return taskRepository.findTasksByGroupId(groupId);
    }
    
    /**
     * Find team members who can be assigned tasks (for internal assignment)
     */
    public List<User> findTeamMembersForUser(Integer userId) {
        return userRepository.findTeamMatesForUser(userId);
    }
    
    /**
     * Assign task internally within team
     */
    public Task assignTaskInternally(Integer taskId, List<Integer> userIds, User assignedBy) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            // Check if assignedBy is part of the task's assigned users or is admin
            boolean canAssign = "ADMIN".equals(assignedBy.getRole()) || 
                               task.getAssignedUsers().stream().anyMatch(u -> u.getId().equals(assignedBy.getId()));
            
            if (canAssign) {
                // Add new users to existing assignments (don't replace)
                Set<User> currentUsers = task.getAssignedUsers();
                for (Integer userId : userIds) {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        currentUsers.add(user);
                    }
                }
                task.setAssignedUsers(currentUsers);
                return taskRepository.save(task);
            }
        }
        return null;
    }
}
