import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';
import { Box, Button, Typography } from '@mui/material';
import { toast, ToastContainer } from 'react-toastify';
import { request } from '../../../helpers/axiosHelper';

const DoctorFacilityRequests = () => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    const load = async () => {
        try {
            const res = await request('get', '/v1/doctor/location-requests');
            if (res.status === 200) setRequests(res.data);
        } catch (e) {
            toast.error('Failed to fetch requests');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, []);

    const accept = async (id) => {
        try {
            await request('post', `/v1/doctor/location-requests/${id}/accept`);
            toast.success('Facility assignment accepted');
            load();
        } catch (e) {
            toast.error(e.response?.data?.message || 'Acceptance failed');
        }
    };

    const reject = async (id) => {
        try {
            await request('post', `/v1/doctor/location-requests/${id}/reject`);
            toast.info('Request rejected');
            load();
        } catch (e) {
            toast.error(e.response?.data?.message || 'Error');
        }
    };

    return (
        <AuthenticatedLayout>
            <div className={styles.addingContainer}>
                <Box
                    sx={{
                        width: '90%',
                        maxWidth: '640px',
                        padding: '20px',
                        backgroundColor: '#fff',
                        borderRadius: '8px',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                        margin: '0 auto',
                    }}
                >
                    <h1 className={styles.addingHeader}>Facility Assignment Requests</h1>
                    {loading && <Typography>Loading...</Typography>}
                    {!loading && requests.length === 0 && (
                        <Typography color="text.secondary">No pending requests.</Typography>
                    )}
                    {!loading &&
                        requests.map((r) => (
                            <Box
                                key={r.id}
                                sx={{
                                    border: '1px solid #eee',
                                    borderRadius: 1,
                                    p: 2,
                                    mb: 2,
                                    display: 'flex',
                                    flexWrap: 'wrap',
                                    alignItems: 'center',
                                    gap: 2,
                                }}
                            >
                                <Box sx={{ flex: '1 1 200px' }}>
                                    <Typography fontWeight="bold">{r.locationName}</Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        {r.city}
                                        {r.address ? `, ${r.address}` : ''}
                                    </Typography>
                                </Box>
                                <Box sx={{ display: 'flex', gap: 1 }}>
                                    <Button variant="contained" color="success" size="small" onClick={() => accept(r.id)}>
                                        Accept
                                    </Button>
                                    <Button variant="outlined" color="error" size="small" onClick={() => reject(r.id)}>
                                        Reject
                                    </Button>
                                </Box>
                            </Box>
                        ))}
                    <ToastContainer position="top-center" autoClose={4000} />
                </Box>
            </div>
        </AuthenticatedLayout>
    );
};

export default DoctorFacilityRequests;
