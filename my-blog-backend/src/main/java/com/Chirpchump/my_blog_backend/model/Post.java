package com.Chirpchump.my_blog_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // 大文本字段
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentMd; // Markdown原文

    @Lob
    @Column(columnDefinition = "TEXT")
    private String contentHtml; // 渲染后的HTML (可选，可以前端渲染或后端渲染)

    @Column(unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY) // 多篇文章可以属于一个用户
    @JoinColumn(name = "user_id") // 外键
    private User author;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_categories",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @Enumerated(EnumType.STRING) // 枚举类型存储为字符串
    private PostStatus status = PostStatus.DRAFT;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = title.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", ""); // 简易 slug 生成
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = title.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", "");
        }
    }
}