package com.Chirpchump.my_blog_backend.service;
import com.Chirpchump.my_blog_backend.model.Tag;
import com.Chirpchump.my_blog_backend.dto.TagRequest;
import java.util.List;

public interface TagService {
    List<Tag> getAllTags();

    Tag getTagById(Long id);

    Tag createTag(TagRequest tagRequest);

    Tag updateTag(Long id, TagRequest tagRequest);

    void deleteTag(Long id);
}