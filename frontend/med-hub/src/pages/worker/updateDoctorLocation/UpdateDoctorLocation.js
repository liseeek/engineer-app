import React, { useEffect, useState } from 'react';
import { Helmet } from "react-helmet";
import logo from '../../../img/logo.svg';
import NavRespo from "../../../components/NavRespo";
import styles from '../../../components/Adding.module.css';
import { Autocomplete, Box, MenuItem, TextField } from '@mui/material';

import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { request } from "../../../helpers/axiosHelper";

const UpdateDoctorLocation = () => {

    const [doctors, setDoctors] = useState([]);
    const [selectDoctors, setSelectDoctors] = useState(null);
    const [operationType, setOperationType] = useState("");
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
                console.error('Failed to fetch location!', error);
            }
        };

        fetchWorkerLocation();
    }, []);

    const fetchDoctors = async () => {
        try {
            const response = await request('get', '/v1/doctors');
            if (response.status === 200) {
                const data = response.data.map((doc) => ({
                    doctorId: doc.doctorId,
                    fullName: `${doc.name} ${doc.surname}`,
                }));
                setDoctors(data);
            }
        } catch (error) {
            console.error('Failed to fetch locations!', error);
        }
    };
    useEffect(() => {
        fetchDoctors();
    }, []);

    const handleDoctorFromWorkerChange = (event, newValue) => {
        setSelectDoctors(newValue);
    };

    const handleChange = (event) => {
        setOperationType(event.target.value);
    };

    const handleUpdate = async () => {
        const payload = {
            operationType,
            locationId: workerLocation.locationId,
        };

        try {
            await request('patch', `/v1/doctors/${selectDoctors.doctorId}`, payload);
            toast.success("Doctor location updated successfully.");

        } catch (error) {
            toast.error("Failed to update the location. Please try again!");
        }
    };

    return (
        <div className={styles.addingBaseContainer}>
            <Helmet>
                <meta name="viewport" content="width=device-width, initial-scale=1" />
            </Helmet>
            <header className={styles.addingHeader}>
                <div className={styles.addingLogo}>
                    <img src={logo} alt="Logo" />
                </div>
                <NavRespo />
            </header>
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
                        <h1 className={styles.addingHeader}>Update Doctor in Your Facility</h1>
                        <form>
                            <Autocomplete
                                options={doctors}
                                getOptionLabel={(option) => option.fullName}
                                value={selectDoctors}
                                onChange={handleDoctorFromWorkerChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search to Update"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />

                            <TextField
                                select
                                label="Operation"
                                name="operationType"
                                value={operationType}
                                onChange={handleChange}
                                fullWidth
                                margin="normal"
                                required
                            >
                                <MenuItem value="ADD">ADD</MenuItem>
                                <MenuItem value="REMOVE">REMOVE</MenuItem>
                            </TextField>

                            <button
                                type="button"
                                onClick={handleUpdate}
                                className={styles.deleteButton}
                            >
                                Update Location
                            </button>
                        </form>

                        <ToastContainer position={"top-center"} autoClose={4000} />
                    </Box>
                </div>
            </main>
        </div>
    );
};

export default UpdateDoctorLocation;
