import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';
import { Autocomplete, Box, TextField } from '@mui/material';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { request, unwrapPage } from "../../../helpers/axiosHelper";

const AddWorker = () => {
    const [user, setUser] = useState({
        name: '',
        surname: '',
        email: '',
        password: '',
        passwordConfirmation: '',
        locationName: '',
        phoneNumber: '',
    });

    const [location, setLocation] = useState([]);

    useEffect(() => {
        const fetchLocations = async () => {
            try {
                const response = await request('get', '/v1/locations?size=500');
                setLocation(unwrapPage(response.data));
            } catch (error) {
                if (error.response && error.response.status === 401) {
                    toast.error('You are not authenticated. Please log in.');
                }
            }
        };

        fetchLocations();
    }, []);

    const handleChange = (e) => {
        setUser({ ...user, [e.target.name]: e.target.value });
    };

    const handleLocationChange = (event, newValue) => {
        if (newValue) {
            setUser({ ...user, locationName: newValue.locationName });
        } else {
            setUser({ ...user, locationName: '' });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (user.password !== user.passwordConfirmation) {
            toast.error('Passwords do not match');
            return;
        }
        try {
            const response = await request('post', '/v1/workers/signup', user);
            if (response.status === 201) {
                toast.success('Worker registered successfully');
            }
        } catch (error) {
            toast.error(error.response?.data.message || 'Error during registration');
        }
    };

    return (
        <AuthenticatedLayout>
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
                        <h1 className={styles.addingHeader}>Register Worker</h1>
                        <form onSubmit={handleSubmit}>

                            <TextField
                                label="Name"
                                name="name"
                                fullWidth
                                margin="normal"
                                value={user.name}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="Surname"
                                name="surname"
                                fullWidth
                                margin="normal"
                                value={user.surname}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="Email"
                                name="email"
                                type="email"
                                fullWidth
                                margin="normal"
                                value={user.email}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="Password"
                                name="password"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={user.password}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="Confirm Password"
                                name="passwordConfirmation"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={user.passwordConfirmation}
                                onChange={handleChange}
                                required
                            />

                            <Autocomplete
                                options={location}
                                getOptionLabel={(option) => option.locationName}
                                value={
                                    location.find(
                                        (loc) => loc.locationName === user.locationName
                                    ) || null
                                }
                                onChange={handleLocationChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search Location"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />

                            <TextField
                                label="Phone Number"
                                name="phoneNumber"
                                type="text"
                                fullWidth
                                margin="normal"
                                value={user.phoneNumber}
                                onChange={handleChange}
                                required
                            />

                            <button type="submit" className={styles.addingButton}>
                                Register
                            </button>
                        </form>
                        <ToastContainer position={"top-center"} autoClose={4000} />
                    </Box>
                </div>
        </AuthenticatedLayout>
    );
};

export default AddWorker;
