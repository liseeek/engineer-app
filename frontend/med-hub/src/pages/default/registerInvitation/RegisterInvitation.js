import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Helmet } from 'react-helmet';
import { request } from '../../../helpers/axiosHelper';
import logo from '../../../img/logo.svg';
import styles from '../register/Register.module.css';
import { Box, TextField, LinearProgress, Collapse, Typography } from '@mui/material';
import { CheckCircle, Cancel } from '@mui/icons-material';
import { toast, ToastContainer } from 'react-toastify';
import { usePasswordValidation } from '../../../hooks/usePasswordValidation';

const RegisterInvitation = () => {
    const { token } = useParams();
    const navigate = useNavigate();
    const [invitationData, setInvitationData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState({
        name: '',
        surname: '',
        password: '',
        passwordConfirmation: '',
        pesel: '',
        phoneNumber: '',
    });
    const [isPasswordFocused, setIsPasswordFocused] = useState(false);
    const [isPasswordConfirmationFocused, setIsPasswordConfirmationFocused] = useState(false);

    useEffect(() => {
        const validateToken = async () => {
            try {
                const response = await request('get', `/v1/invitations/${token}`);
                setInvitationData(response.data);
                if (response.data.role && response.data.role.toUpperCase() !== 'WORKER') {
                    toast.error('This invitation is not for a worker. Doctors register on the doctor registration page.');
                    setTimeout(() => navigate('/'), 4000);
                }
                setLoading(false);
            } catch (error) {
                toast.error('Invalid or expired invitation token');
                setLoading(false);
                setTimeout(() => navigate('/'), 3000);
            }
        };

        if (token) {
            validateToken();
        }
    }, [token, navigate]);

    const passwordValidation = usePasswordValidation(user.password);

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
        {
            key: 'hasSpecialChar',
            label: 'At least 1 special character (@#$%^&+=!)',
            valid: passwordValidation.checks.hasSpecialChar,
        },
        { key: 'noSpaces', label: 'No spaces', valid: passwordValidation.checks.noSpaces },
    ];

    const passwordsMatch = user.password === user.passwordConfirmation && user.passwordConfirmation.length > 0;

    const showMatchIndicator =
        isPasswordConfirmationFocused || (user.password.length > 0 && user.passwordConfirmation.length > 0);

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'pesel') {
            const numericValue = value.replace(/\D/g, '');
            if (numericValue.length <= 11) {
                setUser({ ...user, [name]: numericValue });
            }
        } else {
            setUser({ ...user, [name]: value });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (user.password !== user.passwordConfirmation) {
            toast.error('Passwords do not match');
            return;
        }

        if (invitationData?.role && invitationData.role.toUpperCase() !== 'WORKER') {
            toast.error('Invalid invitation type');
            return;
        }

        try {
            const registrationData = {
                token: token,
                name: user.name,
                surname: user.surname,
                password: user.password,
                passwordConfirmation: user.passwordConfirmation,
                phoneNumber: user.phoneNumber,
            };
            const response = await request('post', '/v1/invitations/register', registrationData);
            if (response.status === 200 || response.status === 201) {
                toast.success('Registration successful! You can now log in.');
                navigate('/');
            }
        } catch (error) {
            if (error.response) {
                toast.error(error.response.data.message || 'Error during registration');
            } else if (error.request) {
                toast.error('No response from server');
            } else {
                toast.error(`Error: ${error.message}`);
            }
        }
    };

    if (loading) {
        return (
            <div>
                <Helmet>
                    <meta name="viewport" content="" />
                </Helmet>
                <div className={styles.registerContainer}>
                    <div className={styles.registerContent}>
                        <Box sx={{ textAlign: 'center', padding: '20px' }}>
                            <Typography>Validating invitation...</Typography>
                        </Box>
                    </div>
                </div>
            </div>
        );
    }

    if (!invitationData) {
        return null;
    }

    if (invitationData.role && invitationData.role.toUpperCase() !== 'WORKER') {
        return (
            <div className={styles.registerContainer}>
                <div className={styles.registerContent}>
                    <Typography>Unsupported invitation type.</Typography>
                </div>
            </div>
        );
    }

    return (
        <div>
            <Helmet>
                <meta name="viewport" content="" />
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
                        <h1 className={styles.registerTitle}>Complete Your Registration</h1>
                        <Typography variant="body2" sx={{ mb: 1, color: 'text.secondary' }}>
                            Email: {invitationData.email} | Role: {invitationData.role}
                        </Typography>
                        {invitationData.locationName && (
                            <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
                                Location: {invitationData.locationName}
                            </Typography>
                        )}
                        <form onSubmit={handleSubmit}>
                            <TextField
                                label="Name"
                                name="name"
                                type="text"
                                fullWidth
                                margin="normal"
                                value={user.name}
                                onChange={handleChange}
                                required
                            />
                            <TextField
                                label="Surname"
                                name="surname"
                                type="text"
                                fullWidth
                                margin="normal"
                                value={user.surname}
                                onChange={handleChange}
                                required
                            />
                            <TextField
                                label="Password"
                                name="password"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={user.password}
                                onChange={handleChange}
                                onFocus={() => setIsPasswordFocused(true)}
                                onBlur={() => setIsPasswordFocused(false)}
                                required
                            />
                            <Collapse in={isPasswordFocused || user.password.length > 0}>
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
                                            color:
                                                getStrengthColor() === 'success'
                                                    ? 'success.main'
                                                    : getStrengthColor() === 'warning'
                                                      ? 'warning.main'
                                                      : 'error.main',
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
                                                    <CheckCircle color="success" sx={{ fontSize: 20, mr: 1 }} />
                                                ) : (
                                                    <Cancel color="error" sx={{ fontSize: 20, mr: 1 }} />
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
                                value={user.passwordConfirmation}
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
                                        user.passwordConfirmation.length > 0 && (
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
                                label="PESEL"
                                name="pesel"
                                type="text"
                                fullWidth
                                margin="normal"
                                value={user.pesel}
                                onChange={handleChange}
                                required
                            />
                            <TextField
                                label="Phone Number"
                                name="phoneNumber"
                                type="text"
                                fullWidth
                                margin="normal"
                                value={user.phoneNumber}
                                onChange={handleChange}
                                required
                            />
                            <button className={styles.registerButton} type="submit">
                                REGISTER
                            </button>
                        </form>

                        <ToastContainer position="top-center" autoClose={4000} />
                    </Box>
                </div>
            </div>
        </div>
    );
};

export default RegisterInvitation;
