import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import GlobalSearchBar from '../../../components/GlobalSearchBar';
import styles from './MainPage.module.css';
import services from '../../../img/services.svg';
import doctor from '../../../img/doctor.svg';
import { getUserRole, request, unwrapPage } from '../../../helpers/axiosHelper';
import { ROLES } from '../../../helpers/roles';

function MainPage() {
    const navigate = useNavigate();
    const role = getUserRole();
    const canUseSearch = role === ROLES.PATIENT || role === ROLES.WORKER;
    const [locations, setLocations] = useState([]);

    useEffect(() => {
        if (!canUseSearch) return;
        let active = true;

        const prefetchSearchData = async () => {
            try {
                const locationsResponse = await request('get', '/v1/locations?size=500');
                if (!active) return;
                setLocations(unwrapPage(locationsResponse?.data));
            } catch {
                if (!active) return;
                setLocations([]);
            }
        };

        prefetchSearchData();
        return () => {
            active = false;
        };
    }, [canUseSearch]);

    return (
        <AuthenticatedLayout
            variant="main"
            headerCenter={
                canUseSearch ? <GlobalSearchBar locations={locations} /> : null
            }
        >
            <div className={styles.mainPageMessages}></div>
            <section className={styles.mainPageHero}>
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
                                onClick={() => navigate('/booking')}
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
            </section>
        </AuthenticatedLayout>
    );
}

export default MainPage;
