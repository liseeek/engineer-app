import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import styles from './AppNav.module.css';
import permissions from '../helpers/permissions';
import { useAuth } from '../context/AuthContext';

const AppNav = () => {
    const navigate = useNavigate();
    const { role, logout } = useAuth();
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    const handleLogout = () => {
        if (window.confirm('Are you sure you want to log out?')) {
            logout();
            navigate('/');
        }
    };

    const links = permissions[role] || permissions.DEFAULT || [];

    const toggleSidebar = () => setIsSidebarOpen((open) => !open);
    const closeSidebar = () => setIsSidebarOpen(false);

    return (
        <div>
            <div className={`${styles.sidebar} ${isSidebarOpen ? styles.open : ''}`}>
                <button type="button" className={styles.closeBtn} onClick={closeSidebar}>
                    &times;
                </button>
                <ul className={styles.navList}>
                    {links.map((link) => (
                        <li key={link.path} className={styles.navItem}>
                            <NavLink
                                to={link.path}
                                className={({ isActive }) =>
                                    isActive ? styles.navActiveLink : styles.navLink
                                }
                                onClick={closeSidebar}
                            >
                                <i className={`${link.icon} ${styles.navIcon}`}></i> {link.label}
                            </NavLink>
                        </li>
                    ))}
                    <li className={styles.navItem}>
                        <button type="button" onClick={handleLogout} className={styles.navButton}>
                            <i className={`fa-solid fa-right-from-bracket ${styles.navIcon}`}></i> Logout
                        </button>
                    </li>
                </ul>
            </div>

            <button type="button" className={styles.openBtn} onClick={toggleSidebar} aria-label="Open menu">
                &#9776;
            </button>
        </div>
    );
};

export default AppNav;
