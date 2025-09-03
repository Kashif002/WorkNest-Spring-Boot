package com.wipro.service;

import com.wipro.dto.UserDto;
import com.wipro.entity.User;
import com.wipro.entity.Task;
import com.wipro.entity.Comment;
import com.wipro.entity.Group;
import com.wipro.repository.UserRepository;
import com.wipro.repository.TaskRepository;
import com.wipro.repository.CommentRepository;
import com.wipro.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getIsActive()))
                .collect(Collectors.toList());
    }
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public User findUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        // Normalize email
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }
        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    // Enhanced CRUD operations
    
    /**
     * Create a new user (for admin)
     */
    public User createUser(User user) {
        // Validate email uniqueness
        String normalizedEmail = user.getEmail() == null ? null : user.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Set default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        // Normalize email before save
        user.setEmail(normalizedEmail);
        return save(user);
    }
    
    /**
     * Update user details (without password)
     */
    public User updateUser(Integer id, User updatedUser) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            if (updatedUser.getEmail() != null) {
                existingUser.setEmail(updatedUser.getEmail().trim().toLowerCase());
            }
            existingUser.setRole(updatedUser.getRole());
            // Don't update password here - use separate method
            return userRepository.save(existingUser);
        }
        return null;
    }
    
    /**
     * Update user password
     */
    public User updateUserPassword(Integer id, String newPassword) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        }
        return null;
    }
    
    /**
     * Delete a user with all associated records (cascade delete)
     */
    @Transactional
    public boolean deleteUser(Integer id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return false;
            }

            // Prevent deleting the main admin user
            if ("ADMIN".equals(user.getRole()) && "admin@worknest.com".equals(user.getEmail())) {
                throw new RuntimeException("Cannot delete the main administrator account");
            }

            System.out.println("Starting cascade delete for user: " + user.getName() + " (ID: " + id + ")");

            // 1. Delete all comments by this user
            List<Comment> userComments = commentRepository.findByUser_Id(id);
            if (!userComments.isEmpty()) {
                System.out.println("Deleting " + userComments.size() + " comments by user");
                commentRepository.deleteAll(userComments);
            }

            // 2. Remove user from all groups
            List<Group> userGroups = groupRepository.findByMembersId(id);
            for (Group group : userGroups) {
                System.out.println("Removing user from group: " + group.getName());
                group.getMembers().remove(user);
                groupRepository.save(group);
            }

            // 3. Remove user from all task assignments
            List<Task> assignedTasks = taskRepository.findByAssignedUsers_Id(id);
            for (Task task : assignedTasks) {
                System.out.println("Removing user from task assignment: " + task.getTitle());
                task.getAssignedUsers().remove(user);
                taskRepository.save(task);
            }

            // 4. Handle tasks assigned by this user (set assignedBy to null or delete if no other assignees)
            List<Task> tasksAssignedByUser = taskRepository.findByAssignedBy_Id(id);
            for (Task task : tasksAssignedByUser) {
                System.out.println("Handling task assigned by user: " + task.getTitle());
                if (task.getAssignedUsers().isEmpty()) {
                    // If no users are assigned, delete the task
                    taskRepository.delete(task);
                    System.out.println("Deleted task with no assignees: " + task.getTitle());
                } else {
                    // Set assignedBy to null
                    task.setAssignedBy(null);
                    taskRepository.save(task);
                    System.out.println("Set assignedBy to null for task: " + task.getTitle());
                }
            }

            // 5. Finally delete the user
            System.out.println("Deleting user: " + user.getName());
            userRepository.delete(user);
            
            System.out.println("Successfully deleted user and all associated records");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error deleting user with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
    
    /**
     * Find users by role
     */
    public List<User> findUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Find team members for a user
     */
    public List<User> findTeamMembersForUser(Integer userId) {
        return userRepository.findTeamMatesForUser(userId);
    }
    
    /**
     * Find users in a specific group
     */
    public List<User> findUsersByGroupId(Integer groupId) {
        return userRepository.findByGroupId(groupId);
    }
    
    /**
     * Search users by name
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Get user statistics
     */
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) userRepository.findAll().size());
        stats.put("admins", userRepository.countByRole("ADMIN"));
        stats.put("users", userRepository.countByRole("USER"));
        return stats;
    }

    /**
     * Check if a user can be deleted
     */
    public boolean canDeleteUser(Integer id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return false;
        }
        
        // Prevent deleting the main admin user
        if ("ADMIN".equals(user.getRole()) && "admin@worknest.com".equals(user.getEmail())) {
            return false;
        }
        
        return true;
    }

    /**
     * Get deletion info for a user (what will be deleted)
     */
    public Map<String, Object> getDeletionInfo(Integer id) {
        Map<String, Object> info = new HashMap<>();
        User user = userRepository.findById(id).orElse(null);
        
        if (user == null) {
            info.put("canDelete", false);
            info.put("reason", "User not found");
            return info;
        }
        
        // Check if it's the main admin
        if ("ADMIN".equals(user.getRole()) && "admin@worknest.com".equals(user.getEmail())) {
            info.put("canDelete", false);
            info.put("reason", "Cannot delete the main administrator account");
            return info;
        }
        
        info.put("canDelete", true);
        info.put("userName", user.getName());
        info.put("userEmail", user.getEmail());
        
        // Count associated records
        List<Comment> comments = commentRepository.findByUser_Id(id);
        List<Group> groups = groupRepository.findByMembersId(id);
        List<Task> assignedTasks = taskRepository.findByAssignedUsers_Id(id);
        List<Task> tasksAssignedBy = taskRepository.findByAssignedBy_Id(id);
        
        info.put("commentsCount", comments.size());
        info.put("groupsCount", groups.size());
        info.put("assignedTasksCount", assignedTasks.size());
        info.put("tasksAssignedByCount", tasksAssignedBy.size());
        
        return info;
    }
}
