import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Helmet} from "react-helmet";
import axios from 'axios';
import logo from '../../../img/logo.svg';
import styles from './Register.module.css';
import {Box, TextField} from "@mui/material";
import {toast, ToastContainer} from "react-toastify";

const Register = () => {
    const [user, setUser] = useState({
        name: '',
        surname: '',
        email: '',
        password: '',
        confirmedPassword: '',
        phoneNumber: ''
    });
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (user.password !== user.confirmedPassword) {
            toast.error('Passwords do not match');
            return;
        }

        try {
            const response = await axios.post('http://localhost:8080/v1/users/signup', user);
            if (response.status === 200 || response.status === 201) {
                toast.success('User registered successfully');
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

    const handleChange = (e) => {
        const {name, value} = e.target;
        setUser({...user, [name]: value});
    };

    return (
        <div>
            <Helmet>
                <meta name="viewport" content=""/>
            </Helmet>
            <div className={styles.registerContainer}>
                <div className={styles.registerLogo}>
                    <img src={logo} alt="Logo"/>
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
                        <h1 className={styles.registerTitle}>Create an Account</h1>
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
                                label="Email"
                                name="email"
                                type="email"
                                fullWidth
                                margin="normal"
                                value={user.email}
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
                                required
                            />
                            <TextField
                                label="Confirm Password"
                                name="confirmedPassword"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={user.confirmedPassword}
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
                            <button className={styles.registerButton} type="submit">REGISTER</button>
                        </form>

                        <ToastContainer position="top-center" autoClose={4000}/>
                    </Box>
                </div>
            </div>
        </div>
    );
};

export default Register;
