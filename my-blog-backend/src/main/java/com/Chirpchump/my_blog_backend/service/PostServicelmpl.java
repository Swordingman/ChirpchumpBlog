package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.*;
import com.Chirpchump.my_blog_backend.exception.ResourceNotFoundException;
import com.Chirpchump.my_blog_backend.model.*;
import com.Chirpchump.my_blog_backend.repository.PostRepository;
import com.Chirpchump.my_blog_backend.repository.UserRepository;
import com.Chirpchump.my_blog_backend.repository.TagRepository;
import com.Chirpchump.my_blog_backend.repository.CategoryRepository;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 引入 StringUtils

import jakarta.persistence.criteria.Join; // 确保是 jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.Predicate; // 确保是 jakarta.persistence.criteria.Predicate

import java.time.LocalDateTime;
import java.util.Collections; // 用于空集合
import java.util.HashSet;
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

    // 确保所有依赖都已注入
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
        if (!StringUtils.hasText(title)) { // 使用 StringUtils.hasText 进行更全面的空检查
            return ""; // 或者抛出异常，如果标题是必需的
        }
        return title.toLowerCase()
                .trim()
                .replaceAll("\\s+", "-") // 将一个或多个空格替换为连字符
                .replaceAll("[^a-z0-9\\-]", ""); // 移除非字母数字和连字符的字符
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
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContentMd(request.getContentMd());
        post.setContentHtml(convertMarkdownToHtml(request.getContentMd()));

        // 处理 Slug
        if (StringUtils.hasText(request.getSlug())) {
            post.setSlug(generateSlug(request.getSlug()));
        } else {
            post.setSlug(generateSlug(request.getTitle()));
        }
        // 确保生成的 slug 不为空，如果 title 和 request.slug 都为空或无效，可能会导致问题
        if (!StringUtils.hasText(post.getSlug())) {
            // 可以设置一个默认slug或者抛出异常
            // post.setSlug("untitled-post-" + System.currentTimeMillis());
            throw new IllegalArgumentException("文章标题和自定义Slug不能都为空或无效，无法生成Slug");
        }


        // 设置作者
        // !!! 重要: 确保ID为1L的用户存在于数据库中，否则这里会抛出ResourceNotFoundException
        // 你应该先在数据库中手动插入一个用户，或者在程序启动时通过 CommandLineRunner 创建
        User author = userRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("用户", "id", 1L));
        post.setAuthor(author);

        // 处理分类关联
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = request.getCategoryIds().stream()
                    .map(id -> categoryRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id)))
                    .collect(Collectors.toSet());
            post.setCategories(categories);
        } else {
            post.setCategories(Collections.emptySet()); // 使用空集合而不是null
        }

        // 处理标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = request.getTagIds().stream()
                    .map(id -> tagRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("标签", "id", id)))
                    .collect(Collectors.toSet());
            post.setTags(tags);
        } else {
            post.setTags(Collections.emptySet()); // 使用空集合而不是null
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
        PostStatus queryStatus = (status == null) ? PostStatus.PUBLISHED : status;
        Page<Post> postsPage;
        // 如果你的 PostRepository 中有 findByStatus(PostStatus status, Pageable pageable) 方法
        postsPage = postRepository.findByStatus(queryStatus, pageable);
        // 否则，如果你想查询所有状态（如果status为null），或者特定状态
        // if (status == null) {
        // postsPage = postRepository.findAll(pageable); // 这会获取所有状态的文章，可能不是期望的
        // } else {
        // postsPage = postRepository.findByStatus(status, pageable);
        // }
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

        // 假设你已经在 PostRepository 中定义了按分类ID和状态查询的方法
        // 例如: Page<Post> findByCategories_IdAndStatus(Long categoryId, PostStatus status, Pageable pageable);
        // 或者 Page<Post> findByCategoriesContainingAndStatus(Category category, PostStatus status, Pageable pageable);
        // 如果是按slug直接查，可能需要自定义@Query
        // Page<Post> postsPage = postRepository.findByCategories_SlugAndStatus(categorySlug, queryStatus, pageable);

        // 如果使用 Repository 中已有的方法，例如 (假设你创建了这样的方法):
        // Page<Post> postsPage = postRepository.findByCategoryIdAndStatus(category.getId(), queryStatus, pageable);
        // 或者，如果Repository方法是基于Category对象的:
        // Page<Post> postsPage = postRepository.findByCategoriesContainingAndStatus(category, queryStatus, pageable);

        // 使用Criteria API (如果Repository中没有现成方法)
        // 注意: 确保 Post 实体中 categories 字段的 @ManyToMany 关联配置正确
        Page<Post> postsPage = postRepository.findAll((root, query, cb) -> {
            Join<Post, Category> categoryJoin = root.join("categories", jakarta.persistence.criteria.JoinType.INNER);
            Predicate categoryPredicate = cb.equal(categoryJoin.get("slug"), categorySlug);
            Predicate statusPredicate = cb.equal(root.get("status"), queryStatus);
            query.where(cb.and(categoryPredicate, statusPredicate));
            query.distinct(true); // 多对多查询时建议去重
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

        // 类似于按分类查询，这里也需要 PostRepository 中有相应的方法
        // 例如: Page<Post> findByTags_IdAndStatus(Long tagId, PostStatus status, Pageable pageable);
        // 或者 Page<Post> findByTags_SlugAndStatus(String tagSlug, PostStatus status, Pageable pageable);
        // Page<Post> postsPage = postRepository.findByTags_SlugAndStatus(tagSlug, queryStatus, pageable);

        // 使用Criteria API (如果Repository中没有现成方法)
        Page<Post> postsPage = postRepository.findAll((root, query, cb) -> {
            Join<Post, Tag> tagJoin = root.join("tags", jakarta.persistence.criteria.JoinType.INNER); // 确保 Post.tags 字段存在且关联正确
            Predicate tagPredicate = cb.equal(tagJoin.get("slug"), tagSlug);
            Predicate statusPredicate = cb.equal(root.get("status"), queryStatus);
            query.where(cb.and(tagPredicate, statusPredicate));
            query.distinct(true);
            return query.getRestriction();
        }, pageable);

        return postsPage.map(this::convertToDto);
    }

    @Override
    @Transactional
    public PostResponse updatePost(PostUpdateRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("要更新的文章ID不能为空");
        }
        Post post = postRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("文章", "id", request.getId()));

        post.setTitle(request.getTitle());
        post.setContentMd(request.getContentMd());
        post.setContentHtml(convertMarkdownToHtml(request.getContentMd()));

        if (StringUtils.hasText(request.getSlug())) {
            post.setSlug(generateSlug(request.getSlug()));
        } else if (!post.getTitle().equalsIgnoreCase(request.getTitle())) { // 如果标题变了，且没有提供新slug，则根据新标题生成
            post.setSlug(generateSlug(request.getTitle()));
        }
        // 再次确保slug不为空
        if (!StringUtils.hasText(post.getSlug())) {
            throw new IllegalArgumentException("文章标题和自定义Slug不能都为空或无效，无法生成Slug");
        }


        // 更新分类关联
        if (request.getCategoryIds() != null) { // 即使是空Set，也应该处理
            if (request.getCategoryIds().isEmpty()) {
                post.getCategories().clear(); // 如果传入空Set，则清空分类
            } else {
                Set<Category> categories = request.getCategoryIds().stream()
                        .map(id -> categoryRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id)))
                        .collect(Collectors.toSet());
                post.setCategories(categories);
            }
        } // 如果 request.getCategoryIds() 为 null，则不修改分类

        // 更新标签关联
        if (request.getTagIds() != null) { // 即使是空Set，也应该处理
            if (request.getTagIds().isEmpty()) {
                post.getTags().clear(); // 如果传入空Set，则清空标签
            } else {
                Set<Tag> tags = request.getTagIds().stream()
                        .map(id -> tagRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("标签", "id", id)))
                        .collect(Collectors.toSet());
                post.setTags(tags);
            }
        } // 如果 request.getTagIds() 为 null，则不修改标签


        PostStatus oldStatus = post.getStatus();
        PostStatus newStatus = request.getStatus();

        if (newStatus != null) { // 只有当请求中明确给出了status才更新
            post.setStatus(newStatus);
            if (oldStatus != PostStatus.PUBLISHED && newStatus == PostStatus.PUBLISHED) {
                post.setPublishedAt(LocalDateTime.now());
            } else if (newStatus != PostStatus.PUBLISHED && post.getPublishedAt() != null) { // 从已发布变为其他状态，且之前有发布时间
                post.setPublishedAt(null);
            }
        }

        Post updatedPost = postRepository.save(post);
        return convertToDto(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) { // 先检查是否存在
            throw new ResourceNotFoundException("文章", "id", id);
        }
        postRepository.deleteById(id); // 直接使用 deleteById，如果不存在会静默失败，所以先检查
    }

    // 辅助方法：将Post实体转换为PostResponse DTO
    private PostResponse convertToDto(Post post) {
        if (post == null) return null;
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setTitle(post.getTitle());
        postResponse.setContentMd(post.getContentMd()); // 通常前端展示用 contentHtml
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