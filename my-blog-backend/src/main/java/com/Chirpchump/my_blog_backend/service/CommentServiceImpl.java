package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.AuthorDto;
import com.Chirpchump.my_blog_backend.dto.CommentRequest;
import com.Chirpchump.my_blog_backend.dto.CommentResponse;
import com.Chirpchump.my_blog_backend.exception.ResourceNotFoundException;
import com.Chirpchump.my_blog_backend.model.*;
import com.Chirpchump.my_blog_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired private CommentRepository commentRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommentLikeRepository commentLikeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        User currentUser = getCurrentUser().orElse(null);

        List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        List<CommentResponse> allDtos = allComments.stream()
                .map(comment -> convertToDto(comment, currentUser, false))
                .collect(Collectors.toList());

        Map<Long, CommentResponse> dtoMap = allDtos.stream()
                .collect(Collectors.toMap(CommentResponse::getId, Function.identity()));

        List<CommentResponse> rootComments = new ArrayList<>();
        for (CommentResponse dto : allDtos) {
            if (dto.getParentId() != null) {
                CommentResponse parentDto = dtoMap.get(dto.getParentId());
                if (parentDto != null) {
                    if (parentDto.getChildren() == null) {
                        parentDto.setChildren(new ArrayList<>());
                    }
                    parentDto.getChildren().add(dto);
                }
            } else {
                rootComments.add(dto);
            }
        }
        return rootComments;
    }


    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest commentRequest) {
        User author = getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("评论需要登录。"));

        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("文章", "id", commentRequest.getPostId()));

        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setAuthor(author);
        comment.setPost(post);

        if (commentRequest.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentRequest.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父评论", "id", commentRequest.getParentId()));
            comment.setParent(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment, author, false);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("删除评论需要登录。"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论", "id", commentId));

        boolean isAdmin = currentUser.getRole().contains("ROLE_ADMIN");

        if (!isAdmin && !comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("权限不足：您只能删除自己的评论。");
        }

        commentLikeRepository.deleteByCommentId(commentId);

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public Map<String, Object> likeComment(Long commentId) {
        User currentUser = getCurrentUser().orElseThrow(() -> new AccessDeniedException("点赞需要登录。"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论", "id", commentId));

        if (commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId).isPresent()) {
            return Map.of("likeCount", comment.getLikeCount(), "liked", true);
        }

        CommentLike like = new CommentLike(currentUser, comment);
        commentLikeRepository.save(like);

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);

        return Map.of("likeCount", comment.getLikeCount(), "liked", true);
    }

    @Override
    @Transactional
    public Map<String, Object> unlikeComment(Long commentId) {
        User currentUser = getCurrentUser().orElseThrow(() -> new AccessDeniedException("取消点赞需要登录。"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论", "id", commentId));

        commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId)
                .ifPresent(like -> {
                    commentLikeRepository.delete(like);
                    comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                    commentRepository.save(comment);
                });

        return Map.of("likeCount", comment.getLikeCount(), "liked", false);
    }

    private java.util.Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return java.util.Optional.empty();
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }

    private CommentResponse convertToDto(Comment comment, User currentUser, boolean recursive) {
        if (comment == null) return null;

        CommentResponse dto = new CommentResponse();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getAuthor() != null) {
            AuthorDto authorDto = new AuthorDto();
            authorDto.setId(comment.getAuthor().getId());
            authorDto.setUsername(comment.getAuthor().getUsername());
            dto.setAuthor(authorDto);
        }

        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }

        dto.setLikeCount(comment.getLikeCount());
        if (currentUser != null) {
            boolean liked = commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), comment.getId()).isPresent();
            dto.setLikedByCurrentUser(liked);
        } else {
            dto.setLikedByCurrentUser(false);
        }

        if (recursive && comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            dto.setChildren(comment.getChildren().stream()
                    .map(child -> convertToDto(child, currentUser, true))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private CommentResponse convertToDto(Comment comment, User currentUser) {
        return convertToDto(comment, currentUser, true);
    }
}