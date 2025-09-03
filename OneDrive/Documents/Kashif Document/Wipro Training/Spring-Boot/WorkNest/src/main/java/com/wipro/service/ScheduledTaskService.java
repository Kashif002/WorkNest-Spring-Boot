package com.wipro.service;

import com.wipro.entity.Task;
import com.wipro.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ScheduledTaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Runs every hour to check for overdue tasks and mark them as DELAYED
     * This method runs at the start of every hour (0 minutes, 0 seconds)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void markOverdueTasks() {
        Date now = new Date();
        List<Task> overdueTasks = taskRepository.findOverdueTasks(now);
        
        for (Task task : overdueTasks) {
            if (!task.getStatus().equals("COMPLETED") && !task.getStatus().equals("DELAYED")) {
                task.setStatus("DELAYED");
                taskRepository.save(task);
            }
        }
        
        if (!overdueTasks.isEmpty()) {
            System.out.println("Marked " + overdueTasks.size() + " tasks as DELAYED due to overdue dates.");
        }
    }
}
