package com.wipro.dto;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import com.wipro.entity.Task;

public class TaskDto {
    private Integer id;
    private String title;
    private String description;
    private String status;
    private Date startDate;
    private Date dueDate;
    private UserDto assignedBy;
    private Set<UserDto> assignedUsers;
    private Boolean completedByAdmin;
    private Date completedAt;

    public TaskDto(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.startDate = task.getStartDate();
        this.dueDate = task.getDueDate();
        if (task.getAssignedBy() != null) {
            this.assignedBy = new UserDto(task.getAssignedBy().getId(), task.getAssignedBy().getName(), task.getAssignedBy().getEmail(), task.getAssignedBy().getRole(), task.getAssignedBy().getIsActive());
        }
        this.assignedUsers = task.getAssignedUsers().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getIsActive()))
                .collect(Collectors.toSet());
        this.completedByAdmin = task.getCompletedByAdmin();
        this.completedAt = task.getCompletedAt();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public UserDto getAssignedBy() { return assignedBy; }
    public void setAssignedBy(UserDto assignedBy) { this.assignedBy = assignedBy; }
    public Set<UserDto> getAssignedUsers() { return assignedUsers; }
    public void setAssignedUsers(Set<UserDto> assignedUsers) { this.assignedUsers = assignedUsers; }
    public Boolean getCompletedByAdmin() { return completedByAdmin; }
    public void setCompletedByAdmin(Boolean completedByAdmin) { this.completedByAdmin = completedByAdmin; }
    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
}
