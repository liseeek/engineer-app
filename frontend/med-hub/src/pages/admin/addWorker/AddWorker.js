import React, {useEffect, useState} from 'react';
import {Helmet} from "react-helmet";
import axios from 'axios';
import logo from '../../../img/logo.svg';
import NavRespo from "../../../components/NavRespo";
import styles from '../../../components/Adding.module.css';
import {Autocomplete, Box, TextField} from '@mui/material';
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import {getAuthToken} from "../../../helpers/axiosHelper";

const AddWorker = () => {
    const [user, setUser] = useState({
        name: '',
        surname: '',
        email: '',
        password: '',
        confirmedPassword: '',
        locationName: '',
        phoneNumber: '',
    });

    const [location, setLocation] = useState([]);

    useEffect(() => {
        const fetchLocations = async () => {
            const token = getAuthToken();
            if (!token) {
                console.error('No token found');
                toast.error('You are not authenticated. Please log in.');
                return null;
            }
            try {
                const response = await axios.get('/v1/locations', {
                    headers: {Authorization: `Bearer ${token}`},
                });
                setLocation(response.data);
            } catch (error) {
                console.error('Failed to fetch locations', error);
            }
        };

        fetchLocations();
    }, []);

    const handleChange = (e) => {
        setUser({...user, [e.target.name]: e.target.value});
    };

    const handleLocationChange = (event, newValue) => {
        if (newValue) {
            setUser({...user, locationName: newValue.locationName});
        } else {
            setUser({...user, locationName: ''});
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (user.password !== user.confirmedPassword) {
            toast.error('Passwords do not match');
            return;
        }
        const token = getAuthToken();
        if (!token) {
            console.error('No token found');
            toast.error('You are not authenticated. Please log in.');
            return null;
        }
        try {
            const response = await axios.post('http://localhost:8080/v1/workers/signup', user, {
                headers: {Authorization: `Bearer ${token}`},
            });
            if (response.status === 201) {
                toast.success('Worker registered successfully');
            }
        } catch (error) {
            toast.error(error.response?.data.message || 'Error during registration');
        }
    };

    return (
        <div className={styles.addingBaseContainer}>
            <Helmet>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
            </Helmet>
            <header className={styles.addingHeader}>
                <div className={styles.addingLogo}>
                    <img src={logo} alt="Logo"/>
                </div>
                <NavRespo/>
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
                                name="confirmedPassword"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={user.confirmedPassword}
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
                        <ToastContainer position={"top-center"} autoClose={4000}/>
                    </Box>
                </div>
            </main>
        </div>
    );
};

export default AddWorker;
