package com.wipro.controller;

import com.wipro.entity.User;
import com.wipro.entity.Task;
import com.wipro.entity.Group;
import com.wipro.entity.Comment;
import com.wipro.service.TaskService;
import com.wipro.service.UserService;
import com.wipro.service.GroupService;
import com.wipro.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

@Controller
public class ViewController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private CommentService commentService;

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            if ("ADMIN".equals(currentUser.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/dashboard";
            }
        }
        return "redirect:/login";
    }

    // Admin Views
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("tasks", taskService.findAllTasks());
        model.addAttribute("users", userService.findAllUsers());
        return "admin/dashboard";
    }

    @GetMapping("/admin/tasks")
    public String adminTasks(Model model) {
        try {
            List<Task> tasks = taskService.findAllTasks();
            List<User> users = userService.findAllUsers();
            
            // Add tasks and users to model
            model.addAttribute("tasks", tasks);
            model.addAttribute("users", users);
            model.addAttribute("task", new Task()); // For the form
            
            return "admin/tasks";
        } catch (Exception e) {
            System.err.println("Error in adminTasks: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load tasks. Please try again.");
            return "admin/tasks";
        }
    }
    
    @GetMapping("/admin/groups")
    public String adminGroups(Model model) {
        model.addAttribute("groups", groupService.findAllGroups());
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("group", new Group()); // For the form
        return "admin/groups";
    }

    @GetMapping("/admin/comments")
    public String adminComments(Model model) {
        // Admins can see all comments from all users
        model.addAttribute("comments", commentService.findAll());
        return "admin/comments";
    }
    
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("user", new User()); // For the form
        return "admin/users";
    }
    
    // Admin POST endpoints
    @GetMapping("/admin/tasks/add")
    public String addTaskForm() {
        return "redirect:/admin/tasks";
    }
    
    @PostMapping("/admin/tasks/add")
    public String addTask(@ModelAttribute Task task, @RequestParam(required = false) List<Integer> userIds, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User currentUser = (User) authentication.getPrincipal();
                task.setAssignedBy(currentUser);
                
                // Set assigned users
                if (userIds != null && !userIds.isEmpty()) {
                    task.setAssignedUsers(new HashSet<>());
                    for (Integer userId : userIds) {
                        User user = userService.findUserById(userId);
                        if (user != null) {
                            task.getAssignedUsers().add(user);
                        }
                    }
                }
                
                taskService.save(task);
                redirectAttributes.addFlashAttribute("success", "Task created successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create task: " + e.getMessage());
        }
        return "redirect:/admin/tasks";
    }
    
    @GetMapping("/admin/groups/add")
    public String addGroupForm() {
        return "redirect:/admin/groups";
    }
    
    @PostMapping("/admin/groups/add")
    public String addGroup(@ModelAttribute Group group, @RequestParam(required = false) List<Integer> memberIds, RedirectAttributes redirectAttributes) {
        try {
            // Set group members
            if (memberIds != null && !memberIds.isEmpty()) {
                group.setMembers(new HashSet<>());
                for (Integer memberId : memberIds) {
                    User user = userService.findUserById(memberId);
                    if (user != null) {
                        group.getMembers().add(user);
                    }
                }
            }
            
            groupService.save(group);
            redirectAttributes.addFlashAttribute("success", "Group created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create group: " + e.getMessage());
        }
        return "redirect:/admin/groups";
    }

    // User Views
    @GetMapping("/user/dashboard")
    public String userDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            model.addAttribute("user", currentUser);
            model.addAttribute("tasks", taskService.findTaskEntitiesForUser(currentUser.getId()));
            model.addAttribute("teamMembers", userService.findTeamMembersForUser(currentUser.getId()));
        }
        return "user/dashboard";
    }
    
    @GetMapping("/user/my-tasks")
    public String userTasks(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            model.addAttribute("user", currentUser);
            model.addAttribute("tasks", taskService.findTaskEntitiesForUser(currentUser.getId()));
            model.addAttribute("teamMembers", userService.findTeamMembersForUser(currentUser.getId()));
        }
        return "user/my-tasks";
    }
    
    @GetMapping("/user/my-team")
    public String userTeam(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            model.addAttribute("user", currentUser);
            model.addAttribute("teamMembers", userService.findTeamMembersForUser(currentUser.getId()));
        }
        return "user/my-team";
    }
    
    @GetMapping("/user/my-comments")
    public String userComments(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            model.addAttribute("user", currentUser);
            // Get all comments by current user
            model.addAttribute("comments", commentService.findCommentsByUserId(currentUser.getId()));
        }
        return "user/my-comments";
    }
    
    @GetMapping("/user/tasks/{id}")
    public String userTaskDetail(@PathVariable Integer id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            
            // Find the task
            Task task = taskService.findTaskById(id);
            if (task != null) {
                // Check if user is assigned to this task
                boolean isAssigned = task.getAssignedUsers().stream()
                    .anyMatch(user -> user.getId().equals(currentUser.getId()));
                
                if (isAssigned || "ADMIN".equals(currentUser.getRole())) {
                    model.addAttribute("task", task);
                    model.addAttribute("comments", commentService.findCommentsByTaskId(id));
                    model.addAttribute("newComment", new Comment());
                    model.addAttribute("teamMembers", userService.findTeamMembersForUser(currentUser.getId()));
                    return "user/tasks";
                }
            }
        }
        return "redirect:/user/dashboard";
    }
    
    // User POST endpoints
    @PostMapping("/user/tasks/{id}/updateStatus")
    public String updateTaskStatus(@PathVariable Integer id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User currentUser = (User) authentication.getPrincipal();
                
                Task task = taskService.findTaskById(id);
                if (task != null) {
                    // Check if user is assigned to this task
                    boolean isAssigned = task.getAssignedUsers().stream()
                        .anyMatch(user -> user.getId().equals(currentUser.getId()));
                    
                    if (isAssigned || "ADMIN".equals(currentUser.getRole())) {
                        // Check if task was completed by admin
                        if (task.getCompletedByAdmin() != null && task.getCompletedByAdmin()) {
                            redirectAttributes.addFlashAttribute("error", "Task has been completed by admin and cannot be modified.");
                        } else {
                            task.setStatus(status);
                            taskService.save(task);
                            redirectAttributes.addFlashAttribute("success", "Task status updated successfully!");
                        }
                    } else {
                        redirectAttributes.addFlashAttribute("error", "You are not authorized to update this task.");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "Task not found.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update task status: " + e.getMessage());
        }
        return "redirect:/user/tasks/" + id;
    }
    
    @PostMapping("/user/tasks/{id}/comments/add")
    public String addComment(@PathVariable Integer id, Comment comment, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User currentUser = (User) authentication.getPrincipal();
                
                Task task = taskService.findTaskById(id);
                if (task != null) {
                    // Check if user is assigned to this task
                    boolean isAssigned = task.getAssignedUsers().stream()
                        .anyMatch(user -> user.getId().equals(currentUser.getId()));
                    
                    if (isAssigned || "ADMIN".equals(currentUser.getRole())) {
                        comment.setTask(task);
                        comment.setUser(currentUser);
                        comment.setCreatedAt(new Date());
                        commentService.save(comment);
                        redirectAttributes.addFlashAttribute("success", "Comment added successfully!");
                    } else {
                        redirectAttributes.addFlashAttribute("error", "You are not authorized to comment on this task.");
                    }
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add comment: " + e.getMessage());
        }
        return "redirect:/user/tasks/" + id;
    }
}
