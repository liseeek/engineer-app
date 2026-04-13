import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Helmet } from 'react-helmet';
import { request } from '../../../helpers/axiosHelper';
import logo from '../../../img/logo.svg';
import styles from '../register/Register.module.css';
import { Autocomplete, Box, TextField, Typography } from '@mui/material';
import { toast, ToastContainer } from 'react-toastify';

const RegisterDoctor = () => {
    const navigate = useNavigate();
    const [specializations, setSpecializations] = useState([]);
    const [selectedSpecs, setSelectedSpecs] = useState([]);
    const [form, setForm] = useState({
        email: '',
        password: '',
        passwordConfirmation: '',
        name: '',
        surname: '',
        pwz: '',
        phoneNumber: '',
    });

    useEffect(() => {
        const load = async () => {
            try {
                const res = await request('get', '/v1/specializations');
                if (res.status === 200) setSpecializations(res.data);
            } catch (e) {
                toast.error('Failed to load specializations');
            }
        };
        load();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        if (name === 'pwz') {
            const digits = value.replace(/\D/g, '').slice(0, 7);
            setForm((f) => ({ ...f, pwz: digits }));
        } else if (name === 'phoneNumber') {
            const digits = value.replace(/\D/g, '').slice(0, 15);
            setForm((f) => ({ ...f, phoneNumber: digits }));
        } else {
            setForm((f) => ({ ...f, [name]: value }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (form.password !== form.passwordConfirmation) {
            toast.error('Passwords do not match');
            return;
        }
        if (selectedSpecs.length === 0) {
            toast.error('Select at least one specialization');
            return;
        }
        const payload = {
            email: form.email,
            password: form.password,
            passwordConfirmation: form.passwordConfirmation,
            name: form.name,
            surname: form.surname,
            pwz: form.pwz,
            phoneNumber: form.phoneNumber,
            specializationIds: selectedSpecs.map((s) => s.specializationId),
        };
        try {
            const res = await request('post', '/v1/doctors/signup', payload);
            if (res.status === 201) {
                toast.success('Registration successful — you can now log in');
                navigate('/');
            }
        } catch (err) {
            if (err.response?.status === 403) {
                toast.error(
                    err.response?.data?.message ||
                        'Doctor self-registration is disabled. Set MEDHUB_DOCTOR_SELF_SIGNUP_ENABLED=true on the backend and REACT_APP_DOCTOR_SIGNUP_ENABLED=true when building the frontend (enabled by default in docker-compose).'
                );
                return;
            }
            const msg =
                err.response?.data?.message ||
                (Array.isArray(err.response?.data?.errors) && err.response.data.errors[0]?.defaultMessage) ||
                'Registration failed';
            toast.error(msg);
        }
    };

    return (
        <div>
            <Helmet>
                <title>Doctor Registration — MedHub</title>
            </Helmet>
            <div className={styles.registerContainer}>
                <div className={styles.registerLogo}>
                    <img src={logo} alt="Logo" />
                </div>
                <div className={styles.registerContent}>
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
                        <h1 className={styles.registerTitle}>Doctor Registration</h1>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            After registration, a facility can send an assignment request — you will accept it after logging in.
                        </Typography>
                        <form onSubmit={handleSubmit}>
                            <TextField
                                label="Email"
                                name="email"
                                type="email"
                                fullWidth
                                margin="normal"
                                value={form.email}
                                onChange={handleChange}
                                required
                            />
                            <TextField
                                label="Password"
                                name="password"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={form.password}
                                onChange={handleChange}
                                required
                                inputProps={{ minLength: 8 }}
                            />
                            <TextField
                                label="Confirm Password"
                                name="passwordConfirmation"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={form.passwordConfirmation}
                                onChange={handleChange}
                                required
                            />
                            <TextField
                                label="First Name"
                                name="name"
                                fullWidth
                                margin="normal"
                                value={form.name}
                                onChange={handleChange}
                                required
                            />
                            <TextField
                                label="Last Name"
                                name="surname"
                                fullWidth
                                margin="normal"
                                value={form.surname}
                                onChange={handleChange}
                                required
                            />
                            <TextField
                                label="PWZ (7 digits)"
                                name="pwz"
                                fullWidth
                                margin="normal"
                                value={form.pwz}
                                onChange={handleChange}
                                required
                                inputProps={{ maxLength: 7 }}
                            />
                            <TextField
                                label="Phone"
                                name="phoneNumber"
                                fullWidth
                                margin="normal"
                                value={form.phoneNumber}
                                onChange={handleChange}
                                required
                            />
                            <Autocomplete
                                multiple
                                options={specializations}
                                getOptionLabel={(o) => o.specializationName || ''}
                                value={selectedSpecs}
                                onChange={(e, v) => setSelectedSpecs(v)}
                                renderInput={(params) => (
                                    <TextField {...params} label="Specializations" margin="normal" required />
                                )}
                            />
                            <button className={styles.registerButton} type="submit" style={{ marginTop: 16 }}>
                                REGISTER
                            </button>
                        </form>
                        <Typography sx={{ mt: 2, textAlign: 'center' }}>
                            <Link to="/">Back to login</Link>
                        </Typography>
                        <ToastContainer position="top-center" autoClose={4000} />
                    </Box>
                </div>
            </div>
        </div>
    );
};

export default RegisterDoctor;
