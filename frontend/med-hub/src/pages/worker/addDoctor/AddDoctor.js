import React, { useEffect, useState } from 'react';
import { Helmet } from "react-helmet";

import styles from '../../../components/Adding.module.css';
import NavRespo from '../../../components/NavRespo';
import logo from '../../../img/logo.svg';
import { Autocomplete, Box, TextField } from "@mui/material";
import { toast, ToastContainer } from "react-toastify";
import { request } from "../../../helpers/axiosHelper";

const AddDoctor = () => {
    const [doctor, setDoctor] = useState({
        name: '',
        surname: '',
        specializationId: '',
    });

    const [specialization, setSpecializations] = useState([]);
    const [workerLocation, setWorkerLocation] = useState([]);

    useEffect(() => {
        const fetchWorkerLocation = async () => {
            try {
                const response = await request('get', '/v1/workers/currentWorker/location');
                if (response.status === 200) {
                    const data = response.data
                    setWorkerLocation(data);
                }

            } catch (error) {
                console.error('Failed to fetch specializations!', error);
            }
        };

        fetchWorkerLocation();
    }, []);

    useEffect(() => {
        const fetchSpecializations = async () => {
            try {
                const response = await request('get', '/v1/specializations');
                if (response.status === 200) {
                    const data = response.data
                    setSpecializations(data);
                }
            } catch (error) {
                console.error('Failed to fetch specializations!', error);
            }
        };

        fetchSpecializations();
    }, []);

    const handleSpecializationChange = (event, newValue) => {
        if (newValue) {
            setDoctor({ ...doctor, specializationId: newValue.specializationId });
        } else {
            setDoctor({ ...doctor, specializationId: '' });
        }
    };

    const handleChange = (e) => {
        setDoctor({ ...doctor, [e.target.name]: e.target.value });
    };

    const doctorData = {
        ...doctor, locationName: workerLocation.locationName,
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await request('post', '/v1/doctors', doctorData);
            if (response.status === 201) {
                toast.success('Doctor added successfully!');
            }
        } catch (error) {
            toast.error('Failed to add doctor!');
        }
    };

    return (
        <div>
            <Helmet>
                <meta name="viewport" content="width=device-width, initial-scale=1" />
            </Helmet>
            <div className={styles.addingBaseContainer}>
                <header className={styles.addingHeader}>
                    <div className={styles.addingLogo}>
                        <img src={logo} alt="Logo" />
                    </div>
                    <NavRespo />
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
                        <h1 className={styles.addingHeader}>Add Doctor to Your Facility</h1>
                        <form onSubmit={handleSubmit}>
                            <TextField
                                label="Name"
                                name="name"
                                fullWidth
                                margin="normal"
                                value={doctor.name}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="Surname"
                                name="surname"
                                fullWidth
                                margin="normal"
                                value={doctor.surname}
                                onChange={handleChange}
                                required
                            />

                            <Autocomplete
                                options={specialization}
                                getOptionLabel={(option) => option.specializationName}
                                value={
                                    specialization.find(
                                        (specialization) => specialization.specializationId === doctor.specializationId
                                    ) || null
                                }
                                onChange={handleSpecializationChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search Specialization or Service"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />

                            <button type="submit" className={styles.addingButton}>
                                SUBMIT
                            </button>
                        </form>

                        <ToastContainer position={"top-center"} autoClose={4000} />
                    </Box>
                </div>
            </main>
        </div>
    );
};

export default AddDoctor;