package com.wipro.dto;

import com.wipro.entity.Comment;
import java.util.Date;

public class CommentDto {
    private Integer id;
    private String content;
    private Date createdAt;
    private UserDto user;
    private Integer taskId;

    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        if (comment.getUser() != null) {
            this.user = new UserDto(comment.getUser().getId(), comment.getUser().getName(), comment.getUser().getEmail(), comment.getUser().getRole());
        }
        if (comment.getTask() != null) {
            this.taskId = comment.getTask().getId();
        }
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
}
