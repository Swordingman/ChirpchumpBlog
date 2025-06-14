import apiClient from './axiosInstance';

export const fetchAllCategoriesAdmin = () => {
    return apiClient.get('/admin/categories');
};

export const createCategoryAdmin = (data) => {
    return apiClient.post('/admin/categories', data);
};

export const updateCategoryAdmin = (id, data) => {
    return apiClient.put(`/admin/categories/${id}`, data)
};

export const deleteCategoryAdmin = (id) => {
    return apiClient.delete(`/admin/categories/${id}`);
};

export const fetchAllTagsAdmin = () => {
    return apiClient.get('/admin/tags');
};

export const createTagAdmin = (data) => {
    return apiClient.post('/admin/tags', data);
};

export const updateTagAdmin = (id, data) => {
    return apiClient.put(`/admin/tags/${id}`, data);
};

export const deleteTagAdmin = (id) => {
    return apiClient.delete(`/admin/tags/${id}`);
};