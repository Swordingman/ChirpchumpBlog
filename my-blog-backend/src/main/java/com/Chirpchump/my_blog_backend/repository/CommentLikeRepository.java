package com.Chirpchump.my_blog_backend.repository;

import com.Chirpchump.my_blog_backend.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 根据用户ID和评论ID查找点赞记录
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    // 根据评论ID删除所有点赞记录 (在删除评论时可能需要)
    void deleteByCommentId(Long commentId);
}