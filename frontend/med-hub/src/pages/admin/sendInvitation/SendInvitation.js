import React, { useState, useEffect } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';
import { Box, TextField, Autocomplete } from '@mui/material';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { request, unwrapPage } from '../../../helpers/axiosHelper';

const SendInvitation = () => {
    const [invitation, setInvitation] = useState({
        email: '',
        locationId: '',
    });

    const [locations, setLocations] = useState([]);

    useEffect(() => {
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
        fetchLocations();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setInvitation({ ...invitation, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!invitation.email || !invitation.locationId) {
            toast.error('Please fill in the email and facility');
            return;
        }

        const payload = {
            email: invitation.email,
            role: 'WORKER',
            locationId: invitation.locationId,
        };

        try {
            const response = await request('post', '/v1/admin/invitations', payload);
            if (response.status === 200 || response.status === 202) {
                toast.success('Invitation sent!');
                setInvitation({ email: '', locationId: '' });
            }
        } catch (error) {
            if (error.response) {
                toast.error(error.response.data.message || 'Failed to send invitation');
            } else if (error.request) {
                toast.error('No response from server');
            } else {
                toast.error(`Error: ${error.message}`);
            }
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
                    <h1 className={styles.addingHeader}>Send Worker Invitation</h1>
                    <form onSubmit={handleSubmit}>
                        <TextField
                            label="Email"
                            name="email"
                            type="email"
                            fullWidth
                            margin="normal"
                            value={invitation.email}
                            onChange={handleChange}
                            required
                        />
                        <Autocomplete
                            options={locations}
                            getOptionLabel={(option) => option.locationName || ''}
                            value={locations.find((l) => l.locationId === invitation.locationId) || null}
                            onChange={(e, newValue) => {
                                setInvitation({
                                    ...invitation,
                                    locationId: newValue ? newValue.locationId : '',
                                });
                            }}
                            renderInput={(params) => (
                                <TextField {...params} label="Facility" fullWidth margin="normal" required />
                            )}
                        />
                        <button className={styles.addingButton} type="submit">
                            SEND INVITATION
                        </button>
                    </form>
                </Box>
            </div>
            <ToastContainer position="top-center" autoClose={4000} />
        </AuthenticatedLayout>
    );
};

export default SendInvitation;
