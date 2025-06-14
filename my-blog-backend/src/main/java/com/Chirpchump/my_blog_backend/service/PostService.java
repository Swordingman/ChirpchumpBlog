package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.CategoryDto;
import com.Chirpchump.my_blog_backend.dto.PostCreateRequest;
import com.Chirpchump.my_blog_backend.dto.PostResponse;
import com.Chirpchump.my_blog_backend.dto.PostUpdateRequest;
import com.Chirpchump.my_blog_backend.model.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

public interface PostService {
    PostResponse createPost(PostCreateRequest postCreateRequest);
    PostResponse getPostById(Long id);
    PostResponse getPostBySlug(String slug);
    Page<PostResponse> getAllPosts(Pageable pageable, PostStatus status);
    Page<PostResponse> getPostsByCategorySlug(String slug, Pageable pageable, PostStatus status);
    Page<PostResponse> getPostsByTagSlug(String slug, Pageable pageable, PostStatus status);
    Page<PostResponse> findPostsByAuthor(String username, Pageable pageable);

    @Transactional
    PostResponse updatePost(PostUpdateRequest request);

    @Transactional
    void deletePost(Long id);
}
