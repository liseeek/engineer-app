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
        role: 'WORKER',
        pwz: '',
        specializationId: '',
    });
 
    const [locations, setLocations] = useState([]);
    const [specializations, setSpecializations] = useState([]);
 
    useEffect(() => {
        const fetchData = async () => {
            try {
                const [locRes, specRes] = await Promise.all([
                    request('get', '/v1/locations?size=500'),
                    request('get', '/v1/specializations')
                ]);
                if (locRes.status === 200) setLocations(unwrapPage(locRes.data));
                if (specRes.status === 200) setSpecializations(specRes.data);
            } catch {
                toast.error('Failed to fetch required data');
            }
        };
        fetchData();
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
            role: invitation.role,
            locationId: invitation.locationId,
            pwz: invitation.role === 'DOCTOR' ? invitation.pwz : null,
            specializationId: invitation.role === 'DOCTOR' ? invitation.specializationId : null,
        };
 
        try {
            const response = await request('post', '/v1/admin/invitations', payload);
            if (response.status === 200 || response.status === 202) {
                toast.success('Invitation sent!');
                setInvitation({ ...invitation, email: '', pwz: '', specializationId: '' });
            }
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to send invitation');
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
                    <h1 className={styles.addingHeader}>Send Invitation</h1>
                    <form onSubmit={handleSubmit}>
                        <TextField
                            select
                            label="Role"
                            name="role"
                            fullWidth
                            margin="normal"
                            value={invitation.role}
                            onChange={handleChange}
                            SelectProps={{ native: true }}
                            required
                        >
                            <option value="WORKER">Worker (Receptionist/Nurse)</option>
                            <option value="DOCTOR">Doctor</option>
                        </TextField>
 
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
                                <TextField {...params} label="Target Facility" fullWidth margin="normal" required />
                            )}
                        />
 
                        {invitation.role === 'DOCTOR' && (
                            <>
                                <TextField
                                    label="PWZ (Doctor License ID)"
                                    name="pwz"
                                    fullWidth
                                    margin="normal"
                                    value={invitation.pwz}
                                    onChange={handleChange}
                                    helperText="Exactly 7 digits (optional here, but required during registration if not provided)"
                                />
                                <Autocomplete
                                    options={specializations}
                                    getOptionLabel={(option) => option.specializationName || ''}
                                    value={specializations.find((s) => s.specializationId === invitation.specializationId) || null}
                                    onChange={(e, newValue) => {
                                        setInvitation({
                                            ...invitation,
                                            specializationId: newValue ? newValue.specializationId : '',
                                        });
                                    }}
                                    renderInput={(params) => (
                                        <TextField {...params} label="Specialization (Primary)" fullWidth margin="normal" />
                                    )}
                                />
                            </>
                        )}
 
                        <button className={styles.addingButton} type="submit" style={{ marginTop: '20px' }}>
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
