// src/api/metaService.js
import apiClient from './axiosInstance';

// 这些API需要后端实现，并受权限保护
export const fetchAllCategoriesAdmin = () => {
    // 假设后端接口 /api/v1/admin/categories 返回所有分类
    return apiClient.get('/admin/categories');
};

export const fetchAllTagsAdmin = () => {
    // 假设后端接口 /api/v1/admin/tags 返回所有标签
    return apiClient.get('/admin/tags');
};

// (可选) 如果前端需要主动创建新标签
export const createTagAdmin = (tagName) => {
    return apiClient.post('/admin/tags', { name: tagName });
};