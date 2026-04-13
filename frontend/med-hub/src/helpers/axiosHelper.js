import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

export const getAuthToken = () => {
    return window.localStorage.getItem('auth_token');
};

export const setAuthHeader = (data) => {
    if (data && data.jwtToken) {
        window.localStorage.setItem("auth_token", data.jwtToken);
        window.localStorage.setItem("user_role", data.authority);
    } else {
        window.localStorage.removeItem("auth_token");
        window.localStorage.removeItem("user_role");
    }
};

export const decodeToken = () => {
    const token = getAuthToken();
    if (!token) return null;

    try {
        return jwtDecode(token);
    } catch {
        return null;
    }
};

export const getUserRole = () => {
    return window.localStorage.getItem("user_role");
};

export const clearAuthStorage = () => {
    window.localStorage.removeItem('auth_token');
    window.localStorage.removeItem('user_role');
};

/** Spring Data Page JSON uses `content`; legacy APIs returned a bare array. */
export const unwrapPage = (data) => {
    if (data && Array.isArray(data.content)) {
        return data.content;
    }
    if (Array.isArray(data)) {
        return data;
    }
    return [];
};

axios.defaults.baseURL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
axios.defaults.headers.post['Content-Type'] = 'application/json';

axios.interceptors.response.use(
    (response) => response,
    (error) => {
        const isLoginRequest = error.config?.url?.includes('/v1/signin');
        if (error.response?.status === 401 && !isLoginRequest) {
            clearAuthStorage();
            window.location.href = '/';
        }
        return Promise.reject(error);
    }
);

export const request = (method, url, data) => {
    const token = getAuthToken();
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    return axios({
        method: method,
        url: url,
        headers: headers,
        data: data,
    });
};
