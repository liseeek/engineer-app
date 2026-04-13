import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';
import { Autocomplete, Box, TextField, Typography } from '@mui/material';
import { toast, ToastContainer } from 'react-toastify';
import { request, unwrapPage } from '../../../helpers/axiosHelper';

const AddDoctor = () => {
    const [doctors, setDoctors] = useState([]);
    const [selectedDoctor, setSelectedDoctor] = useState(null);
    const [workerLocation, setWorkerLocation] = useState({});

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

    useEffect(() => {
        const fetchVerifiedDoctors = async () => {
            try {
                const response = await request(
                    'get',
                    '/v1/doctors?status=VERIFIED&size=500'
                );
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
        fetchVerifiedDoctors();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedDoctor) {
            toast.error('Please select a doctor');
            return;
        }
        try {
            await request('post', '/v1/workers/doctor-location-requests', {
                doctorId: selectedDoctor.doctorId,
            });
            toast.success('Request sent — the doctor must accept it after logging in.');
            setSelectedDoctor(null);
        } catch (error) {
            const msg =
                error.response?.data?.message ||
                error.response?.data?.error ||
                'Failed to send request';
            toast.error(msg);
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
                    <h1 className={styles.addingHeader}>Doctor Assignment Request</h1>
                    <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
                        Select a doctor registered in the system. They will appear at your facility only after accepting.
                    </Typography>
                    {workerLocation.locationName && (
                        <Box sx={{ mb: 2, color: 'text.secondary', fontSize: '0.875rem' }}>
                            Your facility: {workerLocation.locationName}
                        </Box>
                    )}
                    <form onSubmit={handleSubmit}>
                        <Autocomplete
                            options={doctors}
                            getOptionLabel={(option) => option.fullName || ''}
                            value={selectedDoctor}
                            onChange={(e, newValue) => setSelectedDoctor(newValue)}
                            renderInput={(params) => (
                                <TextField {...params} label="Search for a doctor" fullWidth margin="normal" required />
                            )}
                        />
                        <button type="submit" className={styles.addingButton}>
                            SEND REQUEST
                        </button>
                    </form>
                    <ToastContainer position={'top-center'} autoClose={4000} />
                </Box>
            </div>
        </AuthenticatedLayout>
    );
};

export default AddDoctor;
