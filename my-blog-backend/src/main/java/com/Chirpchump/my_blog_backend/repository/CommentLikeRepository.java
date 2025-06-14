package com.Chirpchump.my_blog_backend.repository;

import com.Chirpchump.my_blog_backend.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
    void deleteByCommentId(Long commentId);
}