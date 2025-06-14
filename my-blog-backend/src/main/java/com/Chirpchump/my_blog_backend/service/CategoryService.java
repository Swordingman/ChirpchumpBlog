package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.model.Category;
import com.Chirpchump.my_blog_backend.dto.CategoryRequest;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();

    Category getCategoryById(Long id);

    Category createCategory(CategoryRequest categoryRequest);

    Category updateCategory(Long id, CategoryRequest categoryRequest);

    void deleteCategory(Long id);
}