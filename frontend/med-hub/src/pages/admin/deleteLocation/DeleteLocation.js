import React, {useEffect, useState} from 'react';
import {Helmet} from "react-helmet";
import logo from '../../../img/logo.svg';
import NavRespo from "../../../components/NavRespo";
import styles from '../../../components/Adding.module.css';
import {Autocomplete, Box, TextField} from '@mui/material';
import axios from "axios";
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import {getAuthToken} from "../../../helpers/axiosHelper";

const DeleteLocation = () => {

    const [locations, setLocations] = useState([]);
    const [selectLocations, setSelectLocations] = useState(null);


    const fetchLocations = async () => {
        const token = getAuthToken();
        if (!token) {
            console.error('No token found');
            toast.error('You are not authenticated. Please log in.');
            return null;
        }
        try {
            const response = await axios.get('http://localhost:8080/v1/locations', {
                headers: {Authorization: `Bearer ${token}`},
            });
            if (response.status === 200) {
                const data = response.data;
                setLocations(data);
                console.log('Locations fetched successfully!');
            }
        } catch (error) {
            console.error('Failed to fetch locations!');
        }

    };
    useEffect(() => {
        fetchLocations();
    }, []);

    const handleLocationsChange = (event, newValue) => {
        setSelectLocations(newValue);
    };

    const handleDelete = async (locationId) => {
        const token = getAuthToken();
        if (!token) {
            console.error('No token found');
            toast.error('You are not authenticated. Please log in.');
            return null;
        }
        try {
            await axios.delete(`/v1/locations/${locationId}`, {
                headers: {Authorization: `Bearer ${token}`},
            });
            toast.success("Location deleted successfully.");

            await fetchLocations();
        } catch (error) {
            toast.error("Failed to delete the location. Please try again.");
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
                        <h1 className={styles.addingHeader}>Delete Location</h1>
                        <form>
                            <Autocomplete
                                options={locations}
                                getOptionLabel={(option) => option.locationName || ''}
                                value={selectLocations}
                                onChange={handleLocationsChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search Location To Delete"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />

                            <button onClick={() => handleDelete(selectLocations.locationId)}
                                    className={styles.deleteButton}>Delete Location
                            </button>
                        </form>

                        <ToastContainer position={"top-center"} autoClose={4000}/>
                    </Box>
                </div>
            </main>
        </div>
    );
};

export default DeleteLocation;
