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
        // 1. 获取当前登录用户信息
        User currentUser = getCurrentUser().orElse(null);

        // 2. 获取该文章下的所有评论（扁平列表）
        List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        List<CommentResponse> allDtos = allComments.stream()
                .map(comment -> convertToDto(comment, currentUser, false))
                .collect(Collectors.toList());

        // 4. 在内存中构建树状结构
        //    创建一个 Map，方便通过 ID 快速查找 DTO
        Map<Long, CommentResponse> dtoMap = allDtos.stream()
                .collect(Collectors.toMap(CommentResponse::getId, Function.identity()));

        List<CommentResponse> rootComments = new ArrayList<>();
        for (CommentResponse dto : allDtos) {
            if (dto.getParentId() != null) {
                // 如果是子评论，找到它的父评论 DTO
                CommentResponse parentDto = dtoMap.get(dto.getParentId());
                if (parentDto != null) {
                    // 初始化父评论的 children 列表（如果还没初始化）
                    if (parentDto.getChildren() == null) {
                        parentDto.setChildren(new ArrayList<>());
                    }
                    // 将当前 DTO 加入父评论的 children 列表
                    parentDto.getChildren().add(dto);
                }
            } else {
                // 如果是根评论，直接加入根列表
                rootComments.add(dto);
            }
        }
        return rootComments;
    }


    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest commentRequest) {
        // 获取当前登录用户，评论必须登录
        User author = getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("评论需要登录。"));

        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("文章", "id", commentRequest.getPostId()));

        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setAuthor(author);
        comment.setPost(post);

        // 如果是回复，设置父评论
        if (commentRequest.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentRequest.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父评论", "id", commentRequest.getParentId()));
            comment.setParent(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment, author, false); // 创建者本人肯定没有点赞自己刚发的评论
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("删除评论需要登录。"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论", "id", commentId));

        boolean isAdmin = currentUser.getRole().contains("ROLE_ADMIN");

        // 权限判断：必须是管理员或评论作者
        if (!isAdmin && !comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("权限不足：您只能删除自己的评论。");
        }

        // 删除评论前，先删除相关的点赞记录，避免外键约束问题
        commentLikeRepository.deleteByCommentId(commentId);

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public Map<String, Object> likeComment(Long commentId) {
        User currentUser = getCurrentUser().orElseThrow(() -> new AccessDeniedException("点赞需要登录。"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论", "id", commentId));

        // 检查是否已点赞
        if (commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId).isPresent()) {
            // 在严格模式下，可以抛出异常表示已点赞
            // throw new IllegalStateException("You have already liked this comment.");
            // 或者静默返回当前状态
            return Map.of("likeCount", comment.getLikeCount(), "liked", true);
        }

        // 创建点赞记录
        CommentLike like = new CommentLike(currentUser, comment);
        commentLikeRepository.save(like);

        // 更新评论的点赞数
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

        // 查找并删除点赞记录
        commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId)
                .ifPresent(like -> {
                    commentLikeRepository.delete(like);
                    // 更新评论的点赞数
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

        // [修改] 根据 recursive 参数决定是否处理子评论
        if (recursive && comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            dto.setChildren(comment.getChildren().stream()
                    .map(child -> convertToDto(child, currentUser, true)) // 递归调用
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // [重载] 创建一个方便调用的、默认不递归的 convertToDto 方法
    private CommentResponse convertToDto(Comment comment, User currentUser) {
        return convertToDto(comment, currentUser, true); // 默认进行递归
    }
}