package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.*;
import com.Chirpchump.my_blog_backend.exception.ResourceNotFoundException;
import com.Chirpchump.my_blog_backend.model.*;
import com.Chirpchump.my_blog_backend.repository.*;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 引入 StringUtils
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.criteria.Join; // 确保是 jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate; // 确保是 jakarta.persistence.criteria.Predicate

import java.time.LocalDateTime;
import java.util.Collections; // 用于空集合
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.markdownParser = Parser.builder().build();
        this.htmlRenderer = HtmlRenderer.builder().build();
    }

    private String generateSlug(String title) {
        if (!StringUtils.hasText(title)) {
            return "";
        }
        return title.toLowerCase()
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }

    private String convertMarkdownToHtml(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return "";
        }
        Node document = markdownParser.parse(markdown);
        return htmlRenderer.render(document);
    }

    @Override
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User author =
                userRepository.findByUsername(currentUsername).orElseThrow(() ->
                        new ResourceNotFoundException("用户", "username", currentUsername));
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContentMd(request.getContentMd());
        post.setContentHtml(convertMarkdownToHtml(request.getContentMd()));

        if (StringUtils.hasText(request.getSlug())) {
            post.setSlug(generateSlug(request.getSlug()));
        } else {
            post.setSlug(generateSlug(request.getTitle()));
        }
        if (!StringUtils.hasText(post.getSlug())) {
            throw new IllegalArgumentException("文章标题和自定义Slug不能都为空或无效，无法生成Slug");
        }

        post.setAuthor(author);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = request.getCategoryIds().stream()
                    .map(id -> categoryRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id)))
                    .collect(Collectors.toSet());
            post.setCategories(categories);
        } else {
            post.setCategories(Collections.emptySet());
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = request.getTagIds().stream()
                    .map(id -> tagRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("标签", "id", id)))
                    .collect(Collectors.toSet());
            post.setTags(tags);
        } else {
            post.setTags(Collections.emptySet());
        }

        post.setStatus(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT);
        if (post.getStatus() == PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }

        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("文章", "id", id));
        return convertToDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostBySlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Slug不能为空");
        }
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("文章", "slug", slug));
        return convertToDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable, PostStatus status) {
        Page<Post> postsPage;
        if (status != null) {
            postsPage = postRepository.findByStatus(status, pageable);
        } else {
            postsPage = postRepository.findAll(pageable);
        }
        return postsPage.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCategorySlug(String categorySlug, Pageable pageable, PostStatus status) {
        if (!StringUtils.hasText(categorySlug)) {
            throw new IllegalArgumentException("分类Slug不能为空");
        }
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new ResourceNotFoundException("分类", "slug", categorySlug));

        PostStatus queryStatus = (status == null) ? PostStatus.PUBLISHED : status;

        Page<Post> postsPage = postRepository.findAll((root, query, cb) -> {
            Join<Post, Category> categoryJoin = root.join("categories", jakarta.persistence.criteria.JoinType.INNER);
            Predicate categoryPredicate = cb.equal(categoryJoin.get("slug"), categorySlug);
            Predicate statusPredicate = cb.equal(root.get("status"), queryStatus);
            query.where(cb.and(categoryPredicate, statusPredicate));
            query.distinct(true);
            return query.getRestriction();
        }, pageable);

        return postsPage.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByTagSlug(String tagSlug, Pageable pageable, PostStatus status) {
        if (!StringUtils.hasText(tagSlug)) {
            throw new IllegalArgumentException("标签Slug不能为空");
        }
        Tag tag = tagRepository.findBySlug(tagSlug)
                .orElseThrow(() -> new ResourceNotFoundException("标签", "slug", tagSlug));

        PostStatus queryStatus = (status == null) ? PostStatus.PUBLISHED : status;

        Page<Post> postsPage = postRepository.findAll((root, query, cb) -> {
            Join<Post, Tag> tagJoin = root.join("tags", jakarta.persistence.criteria.JoinType.INNER);
            Predicate tagPredicate = cb.equal(tagJoin.get("slug"), tagSlug);
            Predicate statusPredicate = cb.equal(root.get("status"), queryStatus);
            query.where(cb.and(tagPredicate, statusPredicate));
            query.distinct(true);
            return query.getRestriction();
        }, pageable);

        return postsPage.map(this::convertToDto);
    }

    @Transactional
    @Override
    public PostResponse updatePost(PostUpdateRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("要更新的文章ID不能为空");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        Post post = postRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("文章", "id", request.getId()));

        if (!isAdmin && !post.getAuthor().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("你没有权限修改此文章");
        }

        post.setTitle(request.getTitle());
        post.setContentMd(request.getContentMd());
        post.setContentHtml(convertMarkdownToHtml(request.getContentMd()));

        if (StringUtils.hasText(request.getSlug())) {
            post.setSlug(generateSlug(request.getSlug()));
        } else if (!post.getTitle().equalsIgnoreCase(request.getTitle())) {
            post.setSlug(generateSlug(request.getTitle()));
        }
        if (!StringUtils.hasText(post.getSlug())) {
            throw new IllegalArgumentException("文章标题和自定义Slug不能都为空或无效，无法生成Slug");
        }

        if (request.getCategoryIds() != null) {
            if (request.getCategoryIds().isEmpty()) {
                post.getCategories().clear();
            } else {
                Set<Category> categories = request.getCategoryIds().stream()
                        .map(id -> categoryRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id)))
                        .collect(Collectors.toSet());
                post.setCategories(categories);
            }
        }
        if (request.getTagIds() != null) {
            if (request.getTagIds().isEmpty()) {
                post.getTags().clear();
            } else {
                Set<Tag> tags = request.getTagIds().stream()
                        .map(id -> tagRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("标签", "id", id)))
                        .collect(Collectors.toSet());
                post.setTags(tags);
            }
        }

        PostStatus oldStatus = post.getStatus();
        PostStatus newStatus = request.getStatus();

        if (newStatus != null) {
            post.setStatus(newStatus);
            if (oldStatus != PostStatus.PUBLISHED && newStatus == PostStatus.PUBLISHED) {
                post.setPublishedAt(LocalDateTime.now());
            } else if (newStatus != PostStatus.PUBLISHED && post.getPublishedAt() != null) {
                post.setPublishedAt(null);
            }
        }

        Post updatedPost = postRepository.save(post);
        return convertToDto(updatedPost);
    }

    @Transactional
    @Override
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("文章", "id", id);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("文章", "id", id));

        if (!isAdmin && !post.getAuthor().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("你没有权限删除此文章");
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> findPostsByAuthor(String username, Pageable pageable) {
        Page<Post> posts = postRepository.findByAuthor_Username(username, pageable);
        return posts.map(this::convertToDto);
    }

    private PostResponse convertToDto(Post post) {
        if (post == null) return null;
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setTitle(post.getTitle());
        postResponse.setContentMd(post.getContentMd());
        postResponse.setContentHtml(post.getContentHtml());
        postResponse.setSlug(post.getSlug());

        if (post.getAuthor() != null) {
            AuthorDto authorDto = new AuthorDto();
            authorDto.setId(post.getAuthor().getId());
            authorDto.setUsername(post.getAuthor().getUsername());
            postResponse.setAuthor(authorDto);
        }

        if (post.getCategories() != null) {
            postResponse.setCategories(post.getCategories().stream().map(cat -> {
                CategoryDto dto = new CategoryDto();
                dto.setId(cat.getId());
                dto.setName(cat.getName());
                dto.setSlug(cat.getSlug());
                return dto;
            }).collect(Collectors.toSet()));
        } else {
            postResponse.setCategories(Collections.emptySet());
        }

        if (post.getTags() != null) {
            postResponse.setTags(post.getTags().stream().map(tag -> {
                TagDto dto = new TagDto();
                dto.setId(tag.getId());
                dto.setName(tag.getName());
                dto.setSlug(tag.getSlug());
                return dto;
            }).collect(Collectors.toSet()));
        } else {
            postResponse.setTags(Collections.emptySet());
        }

        postResponse.setStatus(post.getStatus());
        postResponse.setPublishedAt(post.getPublishedAt());
        postResponse.setCreatedAt(post.getCreatedAt());
        postResponse.setUpdatedAt(post.getUpdatedAt());

        return postResponse;
    }
}