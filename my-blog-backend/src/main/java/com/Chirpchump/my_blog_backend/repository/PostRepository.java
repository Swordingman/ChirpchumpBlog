package com.Chirpchump.my_blog_backend.repository;
import com.Chirpchump.my_blog_backend.model.Post;
import com.Chirpchump.my_blog_backend.model.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    Page<Post> findByStatus(PostStatus status, Pageable pageable);
    Page<Post> findByAuthorIdAndStatus(Long authorId, PostStatus status, Pageable pageable);
    Page<Post> findByCategories_SlugAndStatus(String categorySlug, PostStatus status, Pageable pageable);
    Page<Post> findByTags_SlugAndStatus(String tagSlug, PostStatus status, Pageable pageable);
}
