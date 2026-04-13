import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';
import { Autocomplete, Box, TextField } from '@mui/material';
import { toast, ToastContainer } from 'react-toastify';
import { request, unwrapPage } from '../../../helpers/axiosHelper';

const DeleteDoctor = () => {
    const [doctorFromWorkerLocation, setDoctorFromWorkerLocation] = useState([]);
    const [selectDoctors, setSelectDoctors] = useState(null);
    const [workerLocation, setWorkerLocation] = useState(null);

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

    const fetchDoctorsFromWorkerLocation = async () => {
        try {
            const response = await request('get', '/v1/workers/currentWorker/doctors?size=500');
            if (response.status === 200) {
                const data = unwrapPage(response.data).map((doc) => ({
                    doctorId: doc.doctorId,
                    fullName: `${doc.name} ${doc.surname}`,
                }));
                setDoctorFromWorkerLocation(data);
            }
        } catch {
            toast.error('Failed to fetch doctors');
        }
    };

    useEffect(() => {
        fetchWorkerLocation();
        fetchDoctorsFromWorkerLocation();
    }, []);

    const handleUnlink = async (doctorId) => {
        if (!workerLocation?.locationId) {
            toast.error('No facility assigned');
            return;
        }
        try {
            await request('patch', `/v1/doctors/${doctorId}`, {
                operationType: 'REMOVE',
                locationId: workerLocation.locationId,
            });
            toast.success('Doctor unlinked from facility.');
            await fetchDoctorsFromWorkerLocation();
            setSelectDoctors(null);
        } catch (error) {
            toast.error('Failed to unlink doctor.');
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
                    <h1 className={styles.addingHeader}>Unlink Doctor from Facility</h1>
                    <form>
                        <Autocomplete
                            options={doctorFromWorkerLocation}
                            getOptionLabel={(option) => option.fullName}
                            value={selectDoctors}
                            onChange={(event, newValue) => setSelectDoctors(newValue)}
                            renderInput={(params) => (
                                <TextField {...params} label="Select a doctor" fullWidth margin="normal" required />
                            )}
                        />
                        <button
                            type="button"
                            onClick={() => selectDoctors && handleUnlink(selectDoctors.doctorId)}
                            className={styles.deleteButton}
                        >
                            UNLINK FROM FACILITY
                        </button>
                    </form>
                    <ToastContainer position={'top-center'} autoClose={4000} />
                </Box>
            </div>
        </AuthenticatedLayout>
    );
};

export default DeleteDoctor;
