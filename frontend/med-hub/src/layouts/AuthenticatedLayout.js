import React from 'react';
import { Helmet } from 'react-helmet';
import AppNav from '../components/AppNav';
import formStyles from '../components/Adding.module.css';
import mainStyles from '../pages/user/mainpage/MainPage.module.css';
import logo from '../img/logo.svg';

/**
 * Shared shell for logged-in pages: viewport meta, header (logo + nav), main.
 * @param {'form'|'main'} variant — 'main' uses MainPage hero layout; 'form' uses Adding.module.css shell.
 */
function AuthenticatedLayout({ variant = 'form', children }) {
    if (variant === 'main') {
        return (
            <div>
                <Helmet>
                    <meta name="viewport" content="" />
                </Helmet>
                <div className={mainStyles.mainPageBaseContainer}>
                    <header className={mainStyles.mainPageHeader}>
                        <div className={mainStyles.mainPageLogo}>
                            <img src={logo} alt="Logo" />
                        </div>
                        <AppNav />
                    </header>
                    <main className={mainStyles.mainPageMain}>{children}</main>
                </div>
            </div>
        );
    }

    return (
        <div className={formStyles.addingBaseContainer}>
            <Helmet>
                <meta name="viewport" content="width=device-width, initial-scale=1" />
            </Helmet>
            <header className={formStyles.addingHeader}>
                <div className={formStyles.addingLogo}>
                    <img src={logo} alt="Logo" />
                </div>
                <AppNav />
            </header>
            <main className={formStyles.addingMain}>{children}</main>
        </div>
    );
}

export default AuthenticatedLayout;
