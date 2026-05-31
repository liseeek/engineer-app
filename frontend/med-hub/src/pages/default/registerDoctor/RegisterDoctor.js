import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Helmet } from 'react-helmet';
import { request } from '../../../helpers/axiosHelper';
import logo from '../../../img/logo.svg';
import styles from '../register/Register.module.css';
import { Autocomplete, Box, TextField, Typography, LinearProgress, Collapse } from '@mui/material';
import { CheckCircle, Cancel } from '@mui/icons-material';
import { toast, ToastContainer } from 'react-toastify';
import { usePasswordValidation } from '../../../hooks/usePasswordValidation';

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
    const [isPasswordFocused, setIsPasswordFocused] = useState(false);
    const [isPasswordConfirmationFocused, setIsPasswordConfirmationFocused] = useState(false);

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

    const passwordValidation = usePasswordValidation(form.password);

    const getStrengthPercentage = () => {
        const passedChecks = Object.values(passwordValidation.checks).filter(Boolean).length;
        return (passedChecks / 5) * 100;
    };

    const getStrengthColor = () => {
        if (passwordValidation.strength === 'strong') return 'success';
        if (passwordValidation.strength === 'medium') return 'warning';
        return 'error';
    };

    const requirements = [
        { key: 'minLength', label: 'Minimum 8 characters', valid: passwordValidation.checks.minLength },
        { key: 'hasUpperCase', label: 'At least 1 uppercase letter', valid: passwordValidation.checks.hasUpperCase },
        { key: 'hasNumber', label: 'At least 1 number', valid: passwordValidation.checks.hasNumber },
        { key: 'hasSpecialChar', label: 'At least 1 special character (@#$%^&+=!)', valid: passwordValidation.checks.hasSpecialChar },
        { key: 'noSpaces', label: 'No spaces', valid: passwordValidation.checks.noSpaces },
    ];

    const passwordsMatch = form.password === form.passwordConfirmation && 
                          form.passwordConfirmation.length > 0;

    const showMatchIndicator = isPasswordConfirmationFocused || 
                               (form.password.length > 0 && form.passwordConfirmation.length > 0);

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!passwordValidation.isValid) {
            toast.error('Password does not meet the requirements');
            return;
        }

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
            const msg = err.response?.data?.message || 'Registration failed';
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
                        <Typography variant="body2" sx={{ mb: 3, textAlign: 'center', color: 'text.secondary' }}>
                            After registration, your account will be reviewed by an administrator. 
                            You will be able to log in and accept facility assignments once verified.
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
                                onFocus={() => setIsPasswordFocused(true)}
                                onBlur={() => setIsPasswordFocused(false)}
                                required
                            />
                            <Collapse in={isPasswordFocused || form.password.length > 0}>
                                <Box sx={{ mt: 1, mb: 2 }}>
                                    <LinearProgress
                                        variant="determinate"
                                        value={getStrengthPercentage()}
                                        color={getStrengthColor()}
                                        sx={{
                                            height: 8,
                                            borderRadius: 4,
                                            mb: 1,
                                            backgroundColor: 'rgba(0, 0, 0, 0.1)',
                                        }}
                                    />
                                    <Typography
                                        variant="caption"
                                        sx={{
                                            display: 'block',
                                            mb: 2,
                                            color: getStrengthColor() === 'success' ? 'success.main' :
                                                  getStrengthColor() === 'warning' ? 'warning.main' : 'error.main',
                                            fontWeight: 'medium',
                                        }}
                                    >
                                        Password Strength: {passwordValidation.strength.toUpperCase()}
                                    </Typography>
                                    <Box sx={{ mt: 1 }}>
                                        {requirements.map((req) => (
                                            <Box
                                                key={req.key}
                                                sx={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    mb: 1,
                                                }}
                                            >
                                                {req.valid ? (
                                                    <CheckCircle
                                                        color="success"
                                                        sx={{ fontSize: 20, mr: 1 }}
                                                    />
                                                ) : (
                                                    <Cancel
                                                        color="error"
                                                        sx={{ fontSize: 20, mr: 1 }}
                                                    />
                                                )}
                                                <Typography
                                                    variant="body2"
                                                    sx={{
                                                        color: req.valid ? 'text.primary' : 'text.secondary',
                                                        fontSize: '0.875rem',
                                                    }}
                                                >
                                                    {req.label}
                                                </Typography>
                                            </Box>
                                        ))}
                                    </Box>
                                </Box>
                            </Collapse>
                            <TextField
                                label="Confirm Password"
                                name="passwordConfirmation"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={form.passwordConfirmation}
                                onChange={handleChange}
                                onFocus={() => setIsPasswordConfirmationFocused(true)}
                                onBlur={() => setIsPasswordConfirmationFocused(false)}
                                required
                            />
                            <Collapse in={showMatchIndicator}>
                                <Box sx={{ mt: 0.5, mb: 1, display: 'flex', alignItems: 'center' }}>
                                    {passwordsMatch ? (
                                        <>
                                            <CheckCircle color="success" sx={{ fontSize: 20, mr: 1 }} />
                                            <Typography variant="body2" color="success.main">
                                                Passwords match
                                            </Typography>
                                        </>
                                    ) : (
                                        form.passwordConfirmation.length > 0 && (
                                            <>
                                                <Cancel color="error" sx={{ fontSize: 20, mr: 1 }} />
                                                <Typography variant="body2" color="error.main">
                                                    Passwords do not match
                                                </Typography>
                                            </>
                                        )
                                    )}
                                </Box>
                            </Collapse>
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
                                isOptionEqualToValue={(option, value) => option.specializationId === value.specializationId}
                                value={selectedSpecs}
                                onChange={(e, v) => setSelectedSpecs(v)}
                                renderInput={(params) => (
                                    <TextField {...params} label="Specializations" margin="normal" />
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
