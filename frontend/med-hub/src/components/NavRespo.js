import React, {useEffect, useState} from 'react';
import {NavLink, useNavigate} from 'react-router-dom';
import styles from './NavRespo.module.css';
import {getUserRole} from "../helpers/axiosHelper";
import permissions from "../helpers/permissions";

const NavRespo = () => {
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

    const openSidebar = () => setIsSidebarOpen(true);
    const closeSidebar = () => setIsSidebarOpen(false);

    return (
        <div>
            <div className={`${styles.sidebarContainer} ${isSidebarOpen ? styles.open : ''}`}>
                <button className={styles.sidebarCloseBtn} onClick={closeSidebar}>&times;</button>
                <ul className={styles.sidebarList}>
                    {links.map((link) => (
                        <li key={link.path} className={styles.sidebarItem}>
                            <NavLink
                                to={link.path}
                                className={({isActive}) =>
                                    isActive ? styles.sidebarActiveLink : styles.sidebarLink
                                }
                                onClick={closeSidebar}
                            >
                                <i className={`${link.icon} ${styles.sidebarIcon}`}></i> {link.label}
                            </NavLink>
                        </li>
                    ))}
                    <li className={styles.sidebarItem}>
                        <button onClick={handleLogout} className={styles.sidebarButton}>
                            <i className={`fa-solid fa-right-from-bracket ${styles.sidebarIcon}`}></i> Logout
                        </button>
                    </li>
                </ul>
            </div>

            <button className={styles.sidebarOpenBtn} onClick={openSidebar}>
                &#9776;
            </button>
        </div>
    );
};

export default NavRespo;
