import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';
import { Autocomplete, Box, TextField } from '@mui/material';

import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { request, unwrapPage } from "../../../helpers/axiosHelper";

const DeleteLocation = () => {

    const [locations, setLocations] = useState([]);
    const [selectLocations, setSelectLocations] = useState(null);


    const fetchLocations = async () => {
        try {
            const response = await request('get', '/v1/locations?size=500');
            if (response.status === 200) {
                setLocations(unwrapPage(response.data));
            }
        } catch {
            toast.error('Failed to fetch locations');
        }

    };
    useEffect(() => {
        fetchLocations();
    }, []);

    const handleLocationsChange = (event, newValue) => {
        setSelectLocations(newValue);
    };

    const handleDelete = async (locationId) => {
        try {
            await request('delete', `/v1/locations/${locationId}`);
            toast.success("Location deleted successfully.");

            await fetchLocations();
        } catch (error) {
            toast.error("Failed to delete the location. Please try again.");
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

                        <ToastContainer position={"top-center"} autoClose={4000} />
                    </Box>
                </div>
        </AuthenticatedLayout>
    );
};

export default DeleteLocation;
