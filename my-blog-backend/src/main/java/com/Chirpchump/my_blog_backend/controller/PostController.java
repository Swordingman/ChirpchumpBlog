package com.Chirpchump.my_blog_backend.controller;

import com.Chirpchump.my_blog_backend.dto.PostCreateRequest;
import com.Chirpchump.my_blog_backend.dto.PostResponse;
import com.Chirpchump.my_blog_backend.dto.PostUpdateRequest;
import com.Chirpchump.my_blog_backend.model.PostStatus;
import com.Chirpchump.my_blog_backend.service.PostService;
import jakarta.validation.Valid; // 用于开启对DTO的校验
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault; // 用于设置默认分页参数
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import javax.validation.Valid; // Spring Boot 2.x

@RestController // 标记这是一个RESTful控制器，方法返回的都是JSON/XML等数据
@RequestMapping("/api/v1/posts") // 所有请求的基础路径为 /api/v1/posts
// @CrossOrigin(origins = "http://localhost:5173") // 临时允许前端端口(如Vue默认的5173)访问，后续应配置全局CORS
public class PostController {

    private final PostService postService;

    @Autowired // 自动注入PostService实例
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 创建新文章
    // HTTP方法: POST, 路径: /api/v1/posts
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest postCreateRequest) {
        // @Valid注解会触发PostCreateRequest中定义的校验规则
        // @RequestBody注解表示从请求体中获取数据并转换为PostCreateRequest对象
        PostResponse createdPost = postService.createPost(postCreateRequest);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED); // 返回201 Created状态码和创建的文章数据
    }

    // 获取所有文章 (支持分页和按状态过滤，默认获取已发布的文章)
    // HTTP方法: GET, 路径: /api/v1/posts?page=0&size=10&sort=createdAt,desc&status=PUBLISHED
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            // @PageableDefault 设置默认分页参数：每页10条，按创建时间降序排序
            @RequestParam(required = false) PostStatus status) { // status是可选参数
        Page<PostResponse> posts = postService.getAllPosts(pageable, status);
        return ResponseEntity.ok(posts); // 返回200 OK状态码和分页的文章数据
    }

    // 根据文章ID获取单篇文章
    // HTTP方法: GET, 路径: /api/v1/posts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) { // @PathVariable从路径中提取id
        PostResponse post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    // 根据文章Slug获取单篇文章
    // HTTP方法: GET, 路径: /api/v1/posts/slug/{slug}
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug) {
        PostResponse post = postService.getPostBySlug(slug);
        return ResponseEntity.ok(post);
    }

    // 根据分类Slug获取文章列表 (分页，默认获取已发布的)
    // HTTP方法: GET, 路径: /api/v1/posts/category/{categorySlug}?page=0&size=10&status=PUBLISHED
    @GetMapping("/category/{categorySlug}")
    public ResponseEntity<Page<PostResponse>> getPostsByCategory(
            @PathVariable String categorySlug,
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "PUBLISHED") PostStatus status) { // 默认查询已发布的
        Page<PostResponse> posts = postService.getPostsByCategorySlug(categorySlug, pageable, status);
        return ResponseEntity.ok(posts);
    }

    // 根据标签Slug获取文章列表 (分页，默认获取已发布的)
    // HTTP方法: GET, 路径: /api/v1/posts/tag/{tagSlug}?page=0&size=10&status=PUBLISHED
    @GetMapping("/tag/{tagSlug}")
    public ResponseEntity<Page<PostResponse>> getPostsByTag(
            @PathVariable String tagSlug,
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "PUBLISHED") PostStatus status) {
        Page<PostResponse> posts = postService.getPostsByTagSlug(tagSlug, pageable, status);
        return ResponseEntity.ok(posts);
    }

    // 更新指定ID的文章
    // HTTP方法: PUT, 路径: /api/v1/posts/{id}
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest postUpdateRequest) {
        // 实践中，你可能想确保路径中的id和请求体中的id一致
        if (postUpdateRequest.getId() == null) {
            postUpdateRequest.setId(id); // 如果请求体中没有id，使用路径中的
        } else if (!id.equals(postUpdateRequest.getId())) {
            // 如果不一致，可以抛出异常或以一个为准，这里以路径中的id为准
            // return new ResponseEntity<>("路径ID和请求体ID不匹配", HttpStatus.BAD_REQUEST);
            postUpdateRequest.setId(id);
        }
        PostResponse updatedPost = postService.updatePost(postUpdateRequest);
        return ResponseEntity.ok(updatedPost);
    }

    // 删除指定ID的文章
    // HTTP方法: DELETE, 路径: /api/v1/posts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build(); // 返回204 No Content状态码，表示成功处理但无返回内容
    }
}