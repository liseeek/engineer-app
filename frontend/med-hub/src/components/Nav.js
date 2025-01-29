import React, {useEffect, useState} from 'react';
import {NavLink, useNavigate} from 'react-router-dom';
import styles from './Nav.module.css';
import {getUserRole} from "../helpers/axiosHelper";
import permissions from "../helpers/permissions";

const Nav = () => {
    const navigate = useNavigate();
    const [role, setRole] = useState(null);
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    useEffect(() => {
        setRole(getUserRole());
    }, []);

    const handleLogout = () => {
        if (window.confirm("Are you sure you want to log out?")) {
            localStorage.removeItem('auth_token');
            navigate('/');
        }
    };

    const links = permissions[role] || permissions.DEFAULT || [];

    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    return (
        <div>
            <div className={`${styles.sidebar} ${isSidebarOpen ? styles.open : ''}`}>
                <button className={styles.closeBtn} onClick={toggleSidebar}>&times;</button>
                <ul className={styles.navList}>
                    {links.map((link) => (
                        <li key={link.path} className={styles.navItem}>
                            <NavLink
                                to={link.path}
                                className={({isActive}) =>
                                    isActive ? styles.navActiveLink : styles.navLink
                                }
                                onClick={() => setIsSidebarOpen(false)}
                            >
                                <i className={`${link.icon} ${styles.navIcon}`}></i> {link.label}
                            </NavLink>
                        </li>
                    ))}
                    <li className={styles.navItem}>
                        <button onClick={handleLogout} className={styles.navButton}>
                            <i className={`fa-solid fa-right-from-bracket ${styles.navIcon}`}></i> Logout
                        </button>
                    </li>
                </ul>
            </div>

            <button className={styles.openBtn} onClick={toggleSidebar}>
                &#9776;
            </button>
        </div>
    );
};

export default Nav;
