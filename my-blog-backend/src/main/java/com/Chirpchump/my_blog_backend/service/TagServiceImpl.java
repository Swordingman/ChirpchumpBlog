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
        Tag tag = getTagById(id);
        tag.setName(tagRequest.getName());
        if (StringUtils.hasText(tagRequest.getSlug())) {
            tag.setSlug(generateSlug(tagRequest.getSlug()));
        } else if (!tag.getName().equalsIgnoreCase(tagRequest.getName())) {
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
        tagRepository.deleteById(id);
    }
}