package com.wipro.controller.api;

import com.wipro.entity.Task;
import com.wipro.entity.Group;
import com.wipro.entity.User;
import com.wipro.repository.UserRepository;
import com.wipro.service.CommentService;
import com.wipro.service.GroupService;
import com.wipro.service.TaskService;
import com.wipro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired private TaskService taskService;
    @Autowired private UserService userService;
    @Autowired private GroupService groupService;
    @Autowired private CommentService commentService;
    @Autowired private UserRepository userRepository;

    // Dashboard and Statistics
    @GetMapping("/dashboard-data")
    public ResponseEntity<?> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("taskStats", taskService.getTaskStatistics());
        data.put("userStats", userService.getUserStatistics());
        data.put("recentTasks", taskService.findAll());
        data.put("users", userService.findAll());
        return ResponseEntity.ok(data);
    }
    
    // User Management CRUD
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok("User created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create user: " + e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            if (updatedUser != null) {
                return ResponseEntity.ok("User updated successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update user: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> updateUserPassword(@PathVariable Integer id, @RequestParam String newPassword) {
        try {
            User updated = userService.updateUserPassword(id, newPassword);
            if (updated != null) {
                return ResponseEntity.ok("Password updated successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update password: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                return ResponseEntity.ok("User deleted successfully along with all associated records");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete user: " + e.getMessage());
        }
    }
    
    @GetMapping("/users/{id}/deletion-info")
    public ResponseEntity<?> getDeletionInfo(@PathVariable Integer id) {
        try {
            Map<String, Object> info = userService.getDeletionInfo(id);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get deletion info: " + e.getMessage());
        }
    }
    
    // Task Management
    @GetMapping("/tasks")
    public ResponseEntity<?> getAllTasks() {
        return ResponseEntity.ok(taskService.findAll());
    }
    
    @GetMapping("/tasks/status/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable String status) {
        return ResponseEntity.ok(taskService.findTasksByStatus(status));
    }
    
    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody Task task, @RequestParam List<Integer> userIds, Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Task createdTask = taskService.createTask(task, userIds, currentUser);
            return ResponseEntity.ok("Task created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create task: " + e.getMessage());
        }
    }
    
    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody Task task) {
        try {
            Task updatedTask = taskService.updateTask(id, task);
            if (updatedTask != null) {
                return ResponseEntity.ok("Task updated successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update task: " + e.getMessage());
        }
    }
    
    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Integer id, @RequestParam String status, Principal principal) {
        try {
            User currentUser = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Task updatedTask = taskService.updateTaskStatus(id, status, currentUser);
            if (updatedTask != null) {
                return ResponseEntity.ok("Task status updated successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update task status: " + e.getMessage());
        }
    }
    
    @PutMapping("/tasks/{id}/reassign")
    public ResponseEntity<?> reassignTask(@PathVariable Integer id, 
                                        @RequestParam(required = false) List<Integer> userIds,
                                        @RequestParam(required = false) List<Integer> groupIds) {
        try {
            Set<Integer> allUserIds = new HashSet<>();
            
            // Add directly selected users
            if (userIds != null && !userIds.isEmpty()) {
                allUserIds.addAll(userIds);
            }
            
            // Add users from selected groups
            if (groupIds != null && !groupIds.isEmpty()) {
                for (Integer groupId : groupIds) {
                    Group group = groupService.findGroupById(groupId);
                    if (group != null && group.getMembers() != null) {
                        group.getMembers().forEach(member -> allUserIds.add(member.getId()));
                    }
                }
            }
            
            Task reassignedTask = taskService.reassignTask(id, new ArrayList<>(allUserIds));
            if (reassignedTask != null) {
                return ResponseEntity.ok("Task reassigned successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reassign task: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) {
        try {
            boolean deleted = taskService.deleteTask(id);
            if (deleted) {
                return ResponseEntity.ok("Task deleted successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete task: " + e.getMessage());
        }
    }
    
    // Comments
    @GetMapping("/comments")
    public ResponseEntity<?> getAllComments() {
        return ResponseEntity.ok(commentService.findAll());
    }
    
    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> getTaskComments(@PathVariable Integer taskId) {
        return ResponseEntity.ok(commentService.findByTaskId(taskId));
    }
    
    // Advanced Analytics
    @GetMapping("/analytics/tasks")
    public ResponseEntity<?> getTaskAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("statistics", taskService.getTaskStatistics());
        analytics.put("pendingTasks", taskService.findTasksByStatus("PENDING"));
        analytics.put("inProgressTasks", taskService.findTasksByStatus("IN_PROGRESS"));
        analytics.put("completedTasks", taskService.findTasksByStatus("COMPLETED"));
        analytics.put("delayedTasks", taskService.findTasksByStatus("DELAYED"));
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/analytics/users")
    public ResponseEntity<?> getUserAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("statistics", userService.getUserStatistics());
        analytics.put("adminUsers", userService.findUsersByRole("ADMIN"));
        analytics.put("regularUsers", userService.findUsersByRole("USER"));
        return ResponseEntity.ok(analytics);
    }

    // Group Management CRUD (used by admin/groups.html)
    @GetMapping("/groups/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Integer id) {
        List<Group> groups = groupService.findAllGroups();
        Group g = groups.stream().filter(gr -> gr.getId().equals(id)).findFirst().orElse(null);
        if (g == null) return ResponseEntity.notFound().build();
        
        // Return a simple map to avoid circular reference issues
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("id", g.getId());
        groupData.put("name", g.getName());
        groupData.put("members", g.getMembers().stream()
            .map(member -> {
                Map<String, Object> memberData = new HashMap<>();
                memberData.put("id", member.getId());
                memberData.put("name", member.getName());
                memberData.put("email", member.getEmail());
                return memberData;
            })
            .collect(Collectors.toList()));
        
        return ResponseEntity.ok(groupData);
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        try {
            List<Group> groups = groupService.findAllGroups();
            Group existing = groups.stream().filter(gr -> gr.getId().equals(id)).findFirst().orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();

            if (body.containsKey("name")) {
                existing.setName(String.valueOf(body.get("name")));
            }
            if (body.containsKey("memberIds")) {
                @SuppressWarnings("unchecked")
                List<Integer> memberIds = ((List<?>) body.get("memberIds")).stream()
                        .map(v -> Integer.valueOf(String.valueOf(v)))
                        .collect(Collectors.toList());
                existing.getMembers().clear();
                for (Integer uid : memberIds) {
                    User u = userService.findUserById(uid);
                    if (u != null) existing.getMembers().add(u);
                }
            }
            groupService.save(existing);
            return ResponseEntity.ok("Group updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update group: " + e.getMessage());
        }
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id) {
        try {
            Group existing = groupService.findById(id);
            if (existing == null) return ResponseEntity.notFound().build();
            groupService.deleteById(id);
            return ResponseEntity.ok("Group deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete group: " + e.getMessage());
        }
    }

    // Toggle user active/leave status
    @PutMapping("/users/{id}/active")
    public ResponseEntity<?> setUserActive(@PathVariable Integer id, @RequestParam boolean active) {
        try {
            User user = userService.findUserById(id);
            if (user == null) return ResponseEntity.notFound().build();
            user.setIsActive(active);
            userService.save(user);
            return ResponseEntity.ok("User status updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update user status: " + e.getMessage());
        }
    }
}
