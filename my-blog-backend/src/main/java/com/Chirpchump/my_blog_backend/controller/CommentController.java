package com.Chirpchump.my_blog_backend.controller;

import com.Chirpchump.my_blog_backend.dto.CommentRequest;
import com.Chirpchump.my_blog_backend.dto.CommentResponse;
import com.Chirpchump.my_blog_backend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest commentRequest) {
        return new ResponseEntity<>(commentService.createComment(commentRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> likeComment(@PathVariable Long commentId) {
        Map<String, Object> response = commentService.likeComment(commentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> unlikeComment(@PathVariable Long commentId) {
        Map<String, Object> response = commentService.unlikeComment(commentId);
        return ResponseEntity.ok(response);
    }
}