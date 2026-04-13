import React from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from './MainPage.module.css';
import services from '../../../img/services.svg';
import doctor from '../../../img/doctor.svg';

function MainPage() {
    return (
        <AuthenticatedLayout variant="main">
            <div className={styles.mainPageMessages}></div>
            <div className={styles.mainPageContentContainer}>
                <div className={styles.mainPageLeftSection}>
                    <div className={styles.mainPageHealth}>
                        <h1>
                            Your HEALTH,
                            <br />
                            simplified.
                        </h1>
                        <p>Get the care you need, when you need it</p>
                    </div>
                    <div className={styles.mainPageServices}>
                        <div className={styles.mainPageButtonContainer}>
                            <button
                                className={styles.mainPageBookButton}
                                onClick={() => (window.location.href = 'booking')}
                            >
                                BOOK NOW
                            </button>
                            <button className={styles.mainPageContactButton}>
                                CONTACT US
                            </button>
                        </div>
                        <h1>Services</h1>
                        <img src={services} alt="Services" />
                    </div>
                </div>
                <div className={styles.mainPageRightSection}>
                    <img src={doctor} alt="Doctor" />
                </div>
            </div>
        </AuthenticatedLayout>
    );
}

export default MainPage;
