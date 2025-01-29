import React, {useEffect, useState} from 'react';
import {Helmet} from "react-helmet";
import axios from 'axios';
import styles from '../../../components/Adding.module.css';
import NavRespo from '../../../components/NavRespo';
import logo from '../../../img/logo.svg';
import {Autocomplete, Box, TextField} from "@mui/material";
import {toast, ToastContainer} from "react-toastify";
import {getAuthToken} from "../../../helpers/axiosHelper";

const DeleteDoctor = () => {

    const [doctorFromWorkerLocation, setDoctorFromWorkerLocation] = useState([]);
    const [selectDoctors, setSelectDoctors] = useState(null);

    const fetchDoctorsFromWorkerLocation = async () => {
        const token = getAuthToken();
        if (!token) {
            console.error('No token found');
            toast.error('You are not authenticated. Please log in.');
            return;
        }

        try {
            const response = await axios.get('/v1/workers/currentWorker/doctors', {
                headers: {Authorization: `Bearer ${token}`},
            });
            if (response.status === 200) {
                const data = response.data.map((doc) => ({
                    doctorId: doc.doctorId,
                    fullName: `${doc.name} ${doc.surname}`,
                }));
                setDoctorFromWorkerLocation(data);
                console.log('Locations fetched successfully!');
            }
        } catch (error) {
            console.error('Failed to fetch locations!', error);
        }
    };
    useEffect(() => {
        fetchDoctorsFromWorkerLocation();
    }, []);

    const handleDoctorFromWorkerChange = (event, newValue) => {
        setSelectDoctors(newValue);
    };

    const handleDelete = async (doctorId) => {
        const token = getAuthToken();
        if (!token) {
            console.error('No token found');
            toast.error('You are not authenticated. Please log in.');
            return null;
        }
        try {
            await axios.delete(`/v1/doctors/${doctorId}`, {
                headers: {Authorization: `Bearer ${token}`},
            });
            toast.success("Doctor deleted successfully.");

            await fetchDoctorsFromWorkerLocation();
        } catch (error) {
            toast.error("Failed to delete the doctor. Please try again!");
        }
    };

    return (
        <div>
            <Helmet>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
            </Helmet>
            <div className={styles.addingBaseContainer}>
                <header className={styles.addingHeader}>
                    <div className={styles.addingLogo}>
                        <img src={logo} alt="Logo"/>
                    </div>
                    <NavRespo/>
                </header>
            </div>
            <main className={styles.addingMain}>
                <div className={styles.addingContainer}>
                    <Box
                        sx={{
                            width: '90%',
                            maxWidth: '600px',
                            padding: '20px',
                            backgroundColor: '#fff',
                            borderRadius: '8px',
                            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                            margin: '0 auto',
                        }}
                    >
                        <h1 className={styles.addingHeader}>Delete Doctor from Your Facility</h1>
                        <form>

                            <Autocomplete
                                options={doctorFromWorkerLocation}
                                getOptionLabel={(option) => option.fullName}
                                value={selectDoctors}
                                onChange={handleDoctorFromWorkerChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search to Delete"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />

                            <button onClick={() => handleDelete(selectDoctors.doctorId)}
                                    className={styles.deleteButton}>SUBMIT
                            </button>
                        </form>

                        <ToastContainer position={"top-center"} autoClose={4000}/>
                    </Box>
                </div>
            </main>
        </div>
    );
};

export default DeleteDoctor;