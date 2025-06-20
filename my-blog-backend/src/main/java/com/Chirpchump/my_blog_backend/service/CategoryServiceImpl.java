package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.CategoryRequest;
import com.Chirpchump.my_blog_backend.exception.ResourceNotFoundException;
import com.Chirpchump.my_blog_backend.model.Category;
import com.Chirpchump.my_blog_backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    private String generateSlug(String name) {
        if (!StringUtils.hasText(name)) return "";
        return name.toLowerCase().trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类", "id", id));
    }

    @Override
    @Transactional
    public Category createCategory(CategoryRequest categoryRequest) {
        Category category = new Category();
        category.setName(categoryRequest.getName());
        if (StringUtils.hasText(categoryRequest.getSlug())) {
            category.setSlug(generateSlug(categoryRequest.getSlug()));
        } else {
            category.setSlug(generateSlug(categoryRequest.getName()));
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = getCategoryById(id);
        category.setName(categoryRequest.getName());
        if (StringUtils.hasText(categoryRequest.getSlug())) {
            category.setSlug(generateSlug(categoryRequest.getSlug()));
        } else if (!category.getName().equalsIgnoreCase(categoryRequest.getName())) {
            category.setSlug(generateSlug(categoryRequest.getName()));
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("分类", "id", id);
        }
        categoryRepository.deleteById(id);
    }
}