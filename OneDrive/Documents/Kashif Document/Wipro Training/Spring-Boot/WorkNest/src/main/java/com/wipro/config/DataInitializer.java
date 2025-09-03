package com.wipro.config;

import com.wipro.entity.User;
import com.wipro.entity.Task;
import com.wipro.repository.UserRepository;
import com.wipro.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (userRepository.findByEmail("admin@worknest.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@worknest.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
        
        // Create regular user if not exists
        if (userRepository.findByEmail("user@worknest.com").isEmpty()) {
            User user = new User();
            user.setName("Regular User");
            user.setEmail("user@worknest.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            userRepository.save(user);
        }
        
        // Create sample tasks if none exist
        if (taskRepository.count() == 0) {
            User admin = userRepository.findByEmail("admin@worknest.com").get();
            User user = userRepository.findByEmail("user@worknest.com").get();
            
            Task task1 = new Task();
            task1.setTitle("Setup Development Environment");
            task1.setDescription("Configure the development environment for the WorkNest project");
            task1.setStatus("COMPLETED");
            task1.setStartDate(new Date());
            task1.setDueDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)); // 7 days from now
            task1.setAssignedBy(admin);
            task1.setAssignedUsers(new HashSet<>());
            task1.getAssignedUsers().add(user);
            taskRepository.save(task1);
            
            Task task2 = new Task();
            task2.setTitle("Implement User Authentication");
            task2.setDescription("Complete the JWT-based authentication system");
            task2.setStatus("IN_PROGRESS");
            task2.setStartDate(new Date());
            task2.setDueDate(new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L)); // 3 days from now
            task2.setAssignedBy(admin);
            task2.setAssignedUsers(new HashSet<>());
            task2.getAssignedUsers().add(user);
            taskRepository.save(task2);
            
            Task task3 = new Task();
            task3.setTitle("Create Dashboard UI");
            task3.setDescription("Design and implement user and admin dashboards");
            task3.setStatus("PENDING");
            task3.setStartDate(new Date());
            task3.setDueDate(new Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L)); // 5 days from now
            task3.setAssignedBy(admin);
            task3.setAssignedUsers(new HashSet<>());
            task3.getAssignedUsers().add(user);
            taskRepository.save(task3);
        }
    }
}
