import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { getAuthToken, getUserRole } from "./axiosHelper";

const ProtectedRoute = ({ requiredRoles }) => {
    const isAuthenticated = getAuthToken();
    const userRole = getUserRole();

    const hasAccess =
        isAuthenticated && (!requiredRoles || requiredRoles.includes(userRole));

    return hasAccess ? <Outlet /> : <Navigate to="/unauthorized" />;
};

export default ProtectedRoute;
