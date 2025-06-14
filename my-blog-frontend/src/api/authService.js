import apiClient from './axiosInstance';

export const loginUser = (credentials) => {
    return apiClient.post('/auth/login', credentials);
};

export const registerUser = (userInfo) => {
    return apiClient.post('/auth/register', userInfo);
};

export const updatePassword = (passwords) => {
    return apiClient.put('/users/change-password', passwords);
}
