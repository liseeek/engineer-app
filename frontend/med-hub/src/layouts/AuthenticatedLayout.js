import React from 'react';
import { Helmet } from 'react-helmet';
import AppNav from '../components/AppNav';
import AiAssistantWidget from '../components/ai/AiAssistantWidget';
import { getUserRole } from '../helpers/axiosHelper';
import { ROLES } from '../helpers/roles';
import formStyles from '../components/Adding.module.css';
import mainStyles from '../pages/user/mainpage/MainPage.module.css';
import logo from '../img/logo.svg';

/**
 * Shared shell for logged-in pages: viewport meta, header, main.
 * @param {'form'|'main'} variant — 'main' uses MainPage hero layout; 'form' uses Adding.module.css shell.
 * @param {React.ReactNode|null} headerCenter - Optional middle header slot for main variant.
 * @param {React.ReactNode|null} headerRight - Optional right header slot for main variant.
 */
function AuthenticatedLayout({ variant = 'form', headerCenter = null, headerRight = null, children }) {
    const isPatient = getUserRole() === ROLES.PATIENT;

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
                        <div className={mainStyles.mainPageHeaderCenter}>{headerCenter}</div>
                        <div className={mainStyles.mainPageHeaderRight}>{headerRight ?? <AppNav />}</div>
                    </header>
                    <main className={mainStyles.mainPageMain}>{children}</main>
                </div>
                {isPatient && <AiAssistantWidget />}
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
            {isPatient && <AiAssistantWidget />}
        </div>
    );
}

export default AuthenticatedLayout;
