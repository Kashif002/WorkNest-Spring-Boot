package com.wipro.service;

import com.wipro.dto.CommentDto;
import com.wipro.entity.Comment;
import com.wipro.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Finds all comments and converts them to DTOs.
     * @return A list of all CommentDto objects.
     */
    public List<CommentDto> findAll() {
        return commentRepository.findAll()
                .stream()
                .map(CommentDto::new) // Convert each Comment entity to a CommentDto
                .collect(Collectors.toList());
    }

    /**
     * Finds all comments for a specific task and converts them to DTOs.
     * @param taskId The ID of the task.
     * @return A list of CommentDto objects for the given task.
     */
    public List<CommentDto> findByTaskId(Integer taskId) {
        return commentRepository.findByTask_Id(taskId)
                .stream()
                .map(CommentDto::new) // Convert each Comment entity to a CommentDto
                .collect(Collectors.toList());
    }
    
    /**
     * Finds all comments for a specific task as entities.
     * @param taskId The ID of the task.
     * @return A list of Comment entities for the given task.
     */
    public List<Comment> findCommentsByTaskId(Integer taskId) {
        return commentRepository.findByTask_Id(taskId);
    }

    /**
     * Finds all comments for a specific user.
     * @param userId The ID of the user.
     * @return A list of Comment entities for the given user.
     */
    public List<Comment> findCommentsByUserId(Integer userId) {
        return commentRepository.findByUser_Id(userId);
    }

    /**
     * Saves a new comment to the database.
     * @param comment The Comment entity to save.
     * @return The saved Comment entity.
     */
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }
}

