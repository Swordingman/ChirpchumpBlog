package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.TagRequest;
import com.Chirpchump.my_blog_backend.exception.ResourceNotFoundException;
import com.Chirpchump.my_blog_backend.model.Tag;
import com.Chirpchump.my_blog_backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    // 借用 PostServiceImpl 中的 slug 生成方法
    private String generateSlug(String name) {
        if (!StringUtils.hasText(name)) return "";
        return name.toLowerCase().trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id));
    }

    @Override
    @Transactional
    public Tag createTag(TagRequest tagRequest) {
        Tag tag = new Tag();
        tag.setName(tagRequest.getName());
        if (StringUtils.hasText(tagRequest.getSlug())) {
            tag.setSlug(generateSlug(tagRequest.getSlug()));
        } else {
            tag.setSlug(generateSlug(tagRequest.getName()));
        }
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public Tag updateTag(Long id, TagRequest tagRequest) {
        Tag tag = getTagById(id); // 复用查询方法
        tag.setName(tagRequest.getName());
        if (StringUtils.hasText(tagRequest.getSlug())) {
            tag.setSlug(generateSlug(tagRequest.getSlug()));
        } else if (!tag.getName().equalsIgnoreCase(tagRequest.getName())) {
            // 如果名称变了，且没有提供slug，则重新生成slug
            tag.setSlug(generateSlug(tagRequest.getName()));
        }
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("分类", "id", id);
        }
        // 注意：删除分类时，需要考虑与文章的关联。
        // 一个简单的策略是直接删除，但这可能导致文章的分类信息丢失。
        // 更好的策略是先解除所有文章与该分类的关联。
        // 为了简单起见，我们先直接删除。
        tagRepository.deleteById(id);
    }
}