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
    } catch (error) {
        console.error('Invalid token:', error);
        return null;
    }
};

export const getUserRole = () => {
    return window.localStorage.getItem("user_role");
};

axios.defaults.baseURL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
axios.defaults.headers.post['Content-Type'] = 'application/json';

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
