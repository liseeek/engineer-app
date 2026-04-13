import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { clearAuthStorage, getUserRole, setAuthHeader } from '../helpers/axiosHelper';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [role, setRole] = useState(() => getUserRole());

    const login = useCallback((authPayload) => {
        setAuthHeader(authPayload);
        setRole(authPayload.authority ?? getUserRole());
    }, []);

    const logout = useCallback(() => {
        clearAuthStorage();
        setRole(null);
    }, []);

    const refreshRole = useCallback(() => {
        setRole(getUserRole());
    }, []);

    const value = useMemo(
        () => ({ role, login, logout, refreshRole }),
        [role, login, logout, refreshRole]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return ctx;
}
