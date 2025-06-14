import apiClient from './axiosInstance';

export const loginUser = (credentials) => {
    return apiClient.post('/auth/login', credentials);
};

export const registerUser = (userInfo) => {
    return apiClient.post('/auth/register', userInfo);
};

// 可以在这里添加其他认证相关API，如获取当前用户信息、修改密码等
export const updatePassword = (passwords) => {
    // 假设后端修改密码的API是 /api/v1/users/change-password
    return apiClient.put('/users/change-password', passwords);
}
