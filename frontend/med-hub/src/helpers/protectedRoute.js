import React, { useEffect } from "react";
import { Navigate, Outlet } from "react-router-dom";
import {
    clearAuthStorage,
    decodeToken,
    getAuthToken,
} from "./axiosHelper";
import { useAuth } from "../context/AuthContext";

const isTokenExpired = (decoded) => {
    if (!decoded || typeof decoded.exp !== "number") return true;
    return decoded.exp * 1000 <= Date.now();
};

const ProtectedRoute = ({ requiredRoles }) => {
    const { role, logout } = useAuth();

    const token = getAuthToken();
    const decoded = token ? decodeToken() : null;
    const expired = isTokenExpired(decoded);
    const authenticated = Boolean(token) && !expired;

    useEffect(() => {
        if (token && expired) {
            clearAuthStorage();
            logout();
        }
    }, [token, expired, logout]);

    if (!authenticated) {
        return <Navigate to="/unauthorized" replace />;
    }

    const hasRole = !requiredRoles || requiredRoles.includes(role);
    return hasRole ? <Outlet /> : <Navigate to="/unauthorized" replace />;
};

export default ProtectedRoute;
