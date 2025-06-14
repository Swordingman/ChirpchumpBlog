package com.Chirpchump.my_blog_backend.controller;

import com.Chirpchump.my_blog_backend.dto.PostCreateRequest;
import com.Chirpchump.my_blog_backend.dto.PostResponse;
import com.Chirpchump.my_blog_backend.dto.PostUpdateRequest;
import com.Chirpchump.my_blog_backend.model.PostStatus;
import com.Chirpchump.my_blog_backend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest postCreateRequest) {
        PostResponse createdPost = postService.createPost(postCreateRequest);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String authorUsername,
            @RequestParam(required = false) PostStatus status) {
        Page<PostResponse> posts;
        if (StringUtils.hasText(authorUsername)) {
            posts = postService.findPostsByAuthor(authorUsername, pageable);
        } else {
            posts = postService.getAllPosts(pageable, status);
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug) {
        PostResponse post = postService.getPostBySlug(slug);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/category/{categorySlug}")
    public ResponseEntity<Page<PostResponse>> getPostsByCategory(
            @PathVariable String categorySlug,
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "PUBLISHED") PostStatus status) {
        Page<PostResponse> posts = postService.getPostsByCategorySlug(categorySlug, pageable, status);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/tag/{tagSlug}")
    public ResponseEntity<Page<PostResponse>> getPostsByTag(
            @PathVariable String tagSlug,
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "PUBLISHED") PostStatus status) {
        Page<PostResponse> posts = postService.getPostsByTagSlug(tagSlug, pageable, status);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id,
                                                   @Valid @RequestBody PostUpdateRequest postUpdateRequest) {
        if (postUpdateRequest.getId() == null) {
            postUpdateRequest.setId(id);
        } else if (!id.equals(postUpdateRequest.getId())) {
            postUpdateRequest.setId(id);
        }
        PostResponse updatedPost = postService.updatePost(postUpdateRequest);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}