import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';
import { Autocomplete, Box, TextField, Typography } from '@mui/material';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { request, unwrapPage } from '../../../helpers/axiosHelper';

const UpdateDoctorLocation = () => {
    const [doctors, setDoctors] = useState([]);
    const [selectDoctors, setSelectDoctors] = useState(null);
    const [workerLocation, setWorkerLocation] = useState(null);

    useEffect(() => {
        const fetchWorkerLocation = async () => {
            try {
                const response = await request('get', '/v1/workers/currentWorker/location');
                if (response.status === 200) {
                    setWorkerLocation(response.data);
                }
            } catch {
                toast.error('Failed to fetch facility data');
            }
        };
        fetchWorkerLocation();
    }, []);

    const fetchDoctors = async () => {
        try {
            const response = await request('get', '/v1/workers/currentWorker/doctors?size=500');
            if (response.status === 200) {
                const data = unwrapPage(response.data).map((doc) => ({
                    doctorId: doc.doctorId,
                    fullName: `${doc.name} ${doc.surname}`,
                }));
                setDoctors(data);
            }
        } catch {
            toast.error('Failed to fetch doctors');
        }
    };

    useEffect(() => {
        fetchDoctors();
    }, []);

    const handleRemove = async () => {
        if (!selectDoctors || !workerLocation?.locationId) {
            toast.error('Please select a doctor');
            return;
        }
        try {
            await request('patch', `/v1/doctors/${selectDoctors.doctorId}`, {
                operationType: 'REMOVE',
                locationId: workerLocation.locationId,
            });
            toast.success('Doctor unlinked from facility.');
            setSelectDoctors(null);
            fetchDoctors();
        } catch (error) {
            toast.error('Failed to update.');
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
                    <h1 className={styles.addingHeader}>Unlink Doctor (Your Facility)</h1>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        To add a doctor, use "Doctor Assignment Request" — the doctor must accept the invitation.
                    </Typography>
                    <form
                        onSubmit={(e) => {
                            e.preventDefault();
                            handleRemove();
                        }}
                    >
                        <Autocomplete
                            options={doctors}
                            getOptionLabel={(option) => option.fullName}
                            value={selectDoctors}
                            onChange={(event, newValue) => setSelectDoctors(newValue)}
                            renderInput={(params) => (
                                <TextField {...params} label="Doctor at your facility" fullWidth margin="normal" required />
                            )}
                        />
                        <button type="submit" className={styles.deleteButton}>
                            UNLINK FROM FACILITY
                        </button>
                    </form>
                    <ToastContainer position={'top-center'} autoClose={4000} />
                </Box>
            </div>
        </AuthenticatedLayout>
    );
};

export default UpdateDoctorLocation;
