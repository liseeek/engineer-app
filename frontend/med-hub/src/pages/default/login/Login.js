import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {decodeToken, getAuthToken, getUserRole, request, setAuthHeader} from '../../../helpers/axiosHelper';
import {Helmet} from "react-helmet";
import logo from '../../../img/logo.svg';
import styles from './Login.module.css';
import {Box, TextField} from "@mui/material";
import {toast, ToastContainer} from "react-toastify";

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [messages, setMessages] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const token = getAuthToken();
        if (token) {
            const decodedToken = decodeToken();
            const currentDate = new Date();

            if (decodedToken.exp * 1000 < currentDate.getTime()) {
                localStorage.removeItem('auth_token');
                localStorage.removeItem('user_role');
            } else {
                const userRole = getUserRole();
                console.log("User already logged in, role:", userRole);

                switch (userRole) {
                    case 'ADMIN':
                        navigate('/addWorker');
                        break;
                    case 'USER':
                        navigate('/mainpage');
                        break;
                    case 'WORKER':
                        navigate('/addDoctor');
                        break;
                    default:
                        navigate('/');
                        break;
                }
            }
        }
    }, [navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await request('post', '/v1/signin', {email, password});
            console.log("Response data:", response.data);

            if (response.data.jwtToken && response.data.authority) {
                setAuthHeader(response.data);

                const userRole = response.data.authority;

                console.log("Login successful, role:", userRole);

                switch (userRole) {
                    case 'ROLE_ADMIN':
                        navigate('/addWorker');
                        break;
                    case 'ROLE_USER':
                        navigate('/mainpage');
                        break;
                    case 'ROLE_WORKER':
                        navigate('/addDoctor');
                        break;
                    default:
                        navigate('/');
                        break;
                }
            } else {
                toast.error("Login failed: Invalid response");
            }
        } catch (error) {
            console.error("Login error:", error);
            toast.error(error.response?.data.message || 'Login failed');
        }
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        if (name === "email") {
            setEmail(value);
        } else if (name === "password") {
            setPassword(value);
        }
    };

    return (
        <div>
            <Helmet>
                <meta name="viewport" content=""/>
            </Helmet>
            <div className={styles.loginContainer}>
                <div className={styles.loginLogo}>
                    <img src={logo} alt="Logo"/>
                </div>
                <div className={styles.loginInnerContainer}>
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
                        <form onSubmit={handleSubmit}>
                            <TextField
                                label="Email"
                                name="email"
                                type="email"
                                fullWidth
                                margin="normal"
                                value={email}
                                onChange={handleChange}
                                required
                            />

                            <TextField
                                label="Password"
                                name="password"
                                type="password"
                                fullWidth
                                margin="normal"
                                value={password}
                                onChange={handleChange}
                                required
                            />
                            <button className={styles.loginButton} type="submit">LOGIN</button>
                            <a className={styles.loginRegisterButton} href="/register">Don't have an account? Sign
                                up</a>
                        </form>

                        <ToastContainer position={"top-center"} autoClose={4000}/>
                    </Box>
                </div>
            </div>
        </div>
    );
};

export default Login;
