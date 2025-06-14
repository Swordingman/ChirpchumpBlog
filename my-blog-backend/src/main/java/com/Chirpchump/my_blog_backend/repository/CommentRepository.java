package com.Chirpchump.my_blog_backend.repository;

import com.Chirpchump.my_blog_backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 根据文章ID查找所有评论，并按创建时间升序排序
    // 我们将获取所有评论，然后在 Service 层构建树状结构
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}