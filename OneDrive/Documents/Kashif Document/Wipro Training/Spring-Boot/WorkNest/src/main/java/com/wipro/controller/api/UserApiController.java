package com.wipro.controller.api;

import com.wipro.entity.Comment;
import com.wipro.entity.Task;
import com.wipro.entity.User;
import com.wipro.repository.UserRepository;
import com.wipro.service.CommentService;
import com.wipro.service.TaskService;
import com.wipro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired private TaskService taskService;
    @Autowired private CommentService commentService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;

    // Task Management
    @GetMapping("/tasks")
    public ResponseEntity<?> getMyTasks(Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(taskService.findTasksForUser(currentUser.getId()));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTaskDetails(@PathVariable Integer id, Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            Task task = taskService.findTaskById(id);
            if (task != null) {
                // Check if user has access to this task
                boolean hasAccess = task.getAssignedUsers().stream()
                        .anyMatch(user -> user.getId().equals(currentUser.getId()));
                
                if (hasAccess) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("task", taskService.findById(id).orElse(null));
                    data.put("comments", commentService.findByTaskId(id));
                    return ResponseEntity.ok(data);
                }
            }
            return ResponseEntity.status(403).body("Access denied");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Task Status Updates
    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Integer id, @RequestParam String status, Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            Task task = taskService.findTaskById(id);
            if (task != null) {
                // Check if user is assigned to this task
                boolean isAssigned = task.getAssignedUsers().stream()
                        .anyMatch(user -> user.getId().equals(currentUser.getId()));
                
                if (isAssigned) {
                    // Check if task was completed by admin
                    if (task.getCompletedByAdmin() != null && task.getCompletedByAdmin()) {
                        return ResponseEntity.status(403).body("Task has been completed by admin and cannot be modified");
                    }
                    
                    Task updatedTask = taskService.updateTaskStatus(id, status, currentUser);
                    if (updatedTask != null) {
                        return ResponseEntity.ok("Task status updated successfully");
                    }
                }
                return ResponseEntity.status(403).body("Access denied");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update task status: " + e.getMessage());
        }
    }
    
    // Comments Management
    @PostMapping("/tasks/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Integer id, @RequestBody Comment comment, Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            Task task = taskService.findTaskById(id);
            if (task != null) {
                // Check if user is assigned to this task or is a team member with access
                boolean hasAccess = task.getAssignedUsers().stream()
                        .anyMatch(user -> user.getId().equals(currentUser.getId()));
                
                if (hasAccess) {
                    comment.setTask(task);
                    comment.setUser(currentUser);
                    comment.setCreatedAt(new Date());
                    commentService.save(comment);
                    return ResponseEntity.ok("Comment added successfully");
                }
                return ResponseEntity.status(403).body("Access denied");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add comment: " + e.getMessage());
        }
    }
    
    // Team Management
    @GetMapping("/team-members")
    public ResponseEntity<?> getTeamMembers(Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            List<User> teamMembers = userService.findTeamMembersForUser(currentUser.getId());
            return ResponseEntity.ok(teamMembers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Internal Task Assignment (within team)
    @PostMapping("/tasks/{id}/assign")
    public ResponseEntity<?> assignTaskInternally(@PathVariable Integer id, @RequestParam List<Integer> userIds, Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            Task assignedTask = taskService.assignTaskInternally(id, userIds, currentUser);
            if (assignedTask != null) {
                return ResponseEntity.ok("Task assigned to team members successfully");
            }
            return ResponseEntity.status(403).body("You don't have permission to assign this task");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to assign task: " + e.getMessage());
        }
    }
    
    // Get comments made by team members on tasks
    @GetMapping("/tasks/{id}/team-comments")
    public ResponseEntity<?> getTeamTaskComments(@PathVariable Integer id, Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            Task task = taskService.findTaskById(id);
            if (task != null) {
                // Check if user has access to this task
                boolean hasAccess = task.getAssignedUsers().stream()
                        .anyMatch(user -> user.getId().equals(currentUser.getId()));
                
                if (hasAccess) {
                    return ResponseEntity.ok(commentService.findByTaskId(id));
                }
                return ResponseEntity.status(403).body("Access denied");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Dashboard data for user
    @GetMapping("/dashboard-data")
    public ResponseEntity<?> getUserDashboardData(Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            Map<String, Object> data = new HashMap<>();
            data.put("myTasks", taskService.findTasksForUser(currentUser.getId()));
            data.put("teamMembers", userService.findTeamMembersForUser(currentUser.getId()));
            
            // Task statistics for current user
            List<Task> userTasks = taskService.findTaskEntitiesForUser(currentUser.getId());
            Map<String, Long> userTaskStats = new HashMap<>();
            userTaskStats.put("total", (long) userTasks.size());
            userTaskStats.put("pending", userTasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count());
            userTaskStats.put("inProgress", userTasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count());
            userTaskStats.put("completed", userTasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count());
            userTaskStats.put("delayed", userTasks.stream().filter(t -> "DELAYED".equals(t.getStatus())).count());
            data.put("taskStats", userTaskStats);
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
