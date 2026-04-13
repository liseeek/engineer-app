import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { getAuthToken } from "./axiosHelper";
import { useAuth } from "../context/AuthContext";

const ProtectedRoute = ({ requiredRoles }) => {
    const { role } = useAuth();
    const isAuthenticated = getAuthToken();

    const hasAccess =
        isAuthenticated && (!requiredRoles || requiredRoles.includes(role));

    return hasAccess ? <Outlet /> : <Navigate to="/unauthorized" />;
};

export default ProtectedRoute;
