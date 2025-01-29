import React, {useState} from 'react';
import {Helmet} from "react-helmet";
import logo from '../../../img/logo.svg';
import NavRespo from "../../../components/NavRespo";
import styles from '../../../components/Adding.module.css';
import {Box, TextField} from '@mui/material';
import axios from "axios";
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import {getAuthToken} from "../../../helpers/axiosHelper";

const AddLocation = () => {
    const [location, setLocation] = useState({
        locationName: '',
        address: '',
        city: '',
        country: '',
    });


    const handleChange = (e) => {
        setLocation({...location, [e.target.name]: e.target.value});
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const token = getAuthToken();
        if (!token) {
            console.error('No token found');
            toast.error('You are not authenticated. Please log in.');
            return null;
        }
        try {
            const response = await axios.post('http://localhost:8080/v1/locations', location, {
                headers: {Authorization: `Bearer ${token}`},
            });
            if (response.status === 201) {
                toast.success('Location added successfully!');
            }
        } catch (error) {
            toast.error(error.response?.data.message || 'Error! Location already exists!');
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
                        <h1 className={styles.addingHeader}>Add Location</h1>
                        <form onSubmit={handleSubmit}>
                            <TextField
                                label="Location Name"
                                name="locationName"
                                fullWidth
                                margin="normal"
                                value={location.locationName}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="Address"
                                name="address"
                                fullWidth
                                margin="normal"
                                value={location.address}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="City"
                                name="city"
                                fullWidth
                                margin="normal"
                                value={location.city}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="Country"
                                name="country"
                                fullWidth
                                margin="normal"
                                value={location.country}
                                onChange={handleChange}
                                required
                            />

                            <button type="submit" className={styles.addingButton}>
                                Add Location
                            </button>
                        </form>

                        <ToastContainer position={"top-center"} autoClose={4000}/>
                    </Box>
                </div>
            </main>
        </div>
    );
};

export default AddLocation;
