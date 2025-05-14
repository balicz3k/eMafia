import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './Header.module.css';
import { decodeJwt } from '../../utils/decodeJwt';

const HamburgerIcon = () => (
    <svg className={styles.menuIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
    </svg>
);

const CloseIcon = () => (
    <svg className={styles.menuIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
    </svg>
);

const Header = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (token) {
            try {
                setCurrentUser(decodeJwt(token));
            } catch (e) {
                setCurrentUser(null);
            }
        } else {
            setCurrentUser(null);
        }
    }, []);

    const toggleMobileMenu = () => setIsMobileMenuOpen(!isMobileMenuOpen);
    const closeMobileMenu = () => setIsMobileMenuOpen(false);

    const handleLogout = () => {
        localStorage.removeItem("token");
        setCurrentUser(null);
        closeMobileMenu();
        navigate('/login');
    };

    const mainNavLinks = (
        <>
            <Link to="/dashboard" className={styles.navLink} onClick={closeMobileMenu}>Dashboard</Link>
            <Link to="/create-room" className={styles.navLink} onClick={closeMobileMenu}>Create Room</Link>
        </>
    );

    const userSpecificLinks = currentUser ? (
        <>
            <Link to="/profile" className={styles.navLink} onClick={closeMobileMenu}>Profile</Link>
            {currentUser.roles?.includes("ROLE_ADMIN") && (
                <Link to="/admin" className={styles.navLink} onClick={closeMobileMenu}>Admin</Link>
            )}
            <button onClick={handleLogout} className={`${styles.navLink} ${styles.navButton}`}>
                Logout
            </button>
        </>
    ) : (
        <>
            <Link to="/login" className={styles.navLink} onClick={closeMobileMenu}>Login</Link>
            <Link to="/register" className={styles.navLink} onClick={closeMobileMenu}>Register</Link>
        </>
    );

    return (
        <header className={styles.header}>
            <div className={styles.headerContent}>
                <button
                    className={styles.mobileMenuToggle}
                    onClick={toggleMobileMenu}
                    aria-label="Toggle menu"
                    aria-expanded={isMobileMenuOpen}
                    aria-controls="mobile-menu-container"
                >
                    {isMobileMenuOpen ? <CloseIcon /> : <HamburgerIcon />}
                </button>

                <Link to="/" className={styles.logo} onClick={closeMobileMenu}>eMafia</Link>

                <nav className={styles.desktopNav}>
                    {mainNavLinks}
                    <div className={styles.desktopUserActions}>
                        {userSpecificLinks}
                    </div>
                </nav>
            </div>

            {isMobileMenuOpen && (
                <div id="mobile-menu-container" className={styles.mobileMenu}>
                    <nav className={styles.mobileNavLinksContainer}>
                        {mainNavLinks}
                        {userSpecificLinks}
                    </nav>
                </div>
            )}
        </header>
    );
};

export default Header;