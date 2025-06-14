import apiClient from './axiosInstance'

export const fetchPosts = (params) => { // params 可以是 { page, size, sort, status }
    return apiClient.get('/posts', { params })
}

export const fetchPostBySlug = (slug) => {
    return apiClient.get(`/posts/slug/${slug}`)
}

export const fetchPostsByCategory = (categorySlug, params) => {
    return apiClient.get(`/posts/category/${categorySlug}`, { params })
}

export const fetchPostsByTag = (tagSlug, params) => {
    return apiClient.get(`/posts/tag/${tagSlug}`, { params })
}

// 后台管理 API
export const createPostAdmin = (postData) => {
    return apiClient.post('/posts', postData) // 假设后台创建文章接口也是 /posts，权限由后端控制
}

export const fetchPostByIdAdmin = (id) => {
    return apiClient.get(`/posts/${id}`) // 假设后台获取文章接口也是 /posts/:id
}

export const updatePostAdmin = (id, postData) => {
    return apiClient.put(`/posts/${id}`, postData)
}

export const deletePostAdmin = (id) => {
    return apiClient.delete(`/posts/${id}`)
}

export const fetchArchives = () => {
    return apiClient.get('/posts/archives'); // 假设后端有此接口
};