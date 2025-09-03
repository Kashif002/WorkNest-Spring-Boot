package com.wipro.repository;

import com.wipro.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByTask_Id(Integer taskId);
    List<Comment> findByUser_Id(Integer userId);
}
