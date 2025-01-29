import React, {useEffect, useState} from 'react';
import {Helmet} from "react-helmet";
import styles from '../../../components/Adding.module.css';
import NavRespo from '../../../components/NavRespo';
import logo from '../../../img/logo.svg';
import axios from "axios";
import {Autocomplete, Box, MenuItem, TextField} from "@mui/material";
import {DatePicker, LocalizationProvider} from "@mui/x-date-pickers";
import {AdapterDateFns} from "@mui/x-date-pickers/AdapterDateFnsV3";
import {toast, ToastContainer} from "react-toastify";
import {getAuthToken} from "../../../helpers/axiosHelper";

const AddDoctorAvailability = () => {
    const [availability, setAvailability] = useState({
        doctorId: '',
        date: '',
        fromTime: '08:00',
        toTime: '16:00',
        visitTime: '',
        appointmentType: '',
    });

    const [doctors, setDoctors] = useState([]);
    const [startDate, setStartDate] = useState(new Date());

    useEffect(() => {
        const fetchDoctors = async () => {
            const token = getAuthToken();
            if (!token) {
                toast.error("You are not authenticated. Please log in.");
                return;
            }

            try {
                const response = await axios.get('/v1/workers/currentWorker/doctors', {
                    headers: {Authorization: `Bearer ${token}`},
                });

                if (response.status === 200) {
                    const data = response.data.map((doc) => ({
                        doctorId: doc.doctorId,
                        fullName: `${doc.name} ${doc.surname}`,
                    }));
                    setDoctors(data);
                }
            } catch (error) {
                console.error("Failed to fetch doctors:", error);
                toast.error("Failed to fetch doctors. Please try again.");
            }
        };

        fetchDoctors();
    }, []);

    const handleDoctorChange = (event, newValue) => {
        setAvailability({
            ...availability,
            doctorId: newValue?.doctorId || ''
        });
    };

    const handleDateChange = (date) => {
        setStartDate(date);
        setAvailability({
            ...availability,
            date: date.toISOString().split('T')[0]
        });
    };

    const handleAvailabilityChange = (e) => {
        const {name, value} = e.target;
        setAvailability({...availability, [name]: value});
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const token = getAuthToken();
        if (!token) {
            toast.error("You are not authenticated. Please log in.");
            return;
        }

        if (!availability.doctorId) {
            toast.error("Please select a doctor.");
            return;
        }

        const parsedFrom = new Date(`1970-01-01T${availability.fromTime}`);
        const parsedTo = new Date(`1970-01-01T${availability.toTime}`);
        if (parsedFrom >= parsedTo) {
            toast.error("'From Time' must be before 'To Time'");
            return;
        }

        if (!availability.visitTime || availability.visitTime <= 0) {
            toast.error("Visit time must be a positive number.");
            return;
        }

        try {
            const response = await axios.post('/v1/availability', availability,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                }
            );

            if (response.status === 201) {
                toast.success("Availability created successfully.");
                setAvailability({
                    doctorId: '',
                    date: '',
                    fromTime: '08:00',
                    toTime: '16:00',
                    visitTime: '',
                    appointmentType: '',
                });
                setStartDate(new Date());
            } else {
                toast.error("Failed to create availability. Please try again.");
            }
        } catch (error) {
            console.error("Failed to create availability:", error);
            toast.error(
                "Failed to create availability. Please check your inputs and try again."
            );
        }
    };

    return (
        <div>
            <Helmet>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
            </Helmet>
            <div className={styles.addingBaseContainer}>
                <header className={styles.addingHeader}>
                    <div className={styles.addingLogo}>
                        <img src={logo} alt="Logo"/>
                    </div>
                    <NavRespo/>
                </header>
            </div>
            <main className={styles.addingMain}>
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
                        <h1 className={styles.addingHeader}>Add Doctor Availability</h1>
                        <form onSubmit={handleSubmit}>
                            <Autocomplete
                                options={doctors}
                                getOptionLabel={(option) => option.fullName}
                                value={doctors.find(doc => doc.doctorId === availability.doctorId) || null}
                                onChange={handleDoctorChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Select Doctor"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />
                            <LocalizationProvider dateAdapter={AdapterDateFns}>
                                <DatePicker
                                    label="Select Date"
                                    value={startDate}
                                    onChange={handleDateChange}
                                    sx={{width: '100%'}}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            fullWidth
                                            margin="normal"
                                            required
                                        />
                                    )}
                                    disablePast
                                />
                            </LocalizationProvider>
                            <TextField
                                label="From time"
                                name="fromTime"
                                type="time"
                                fullWidth
                                margin="normal"
                                value={availability.fromTime}
                                onChange={handleAvailabilityChange}
                                required
                            />
                            <TextField
                                label="To time"
                                name="toTime"
                                type="time"
                                fullWidth
                                margin="normal"
                                value={availability.toTime}
                                onChange={handleAvailabilityChange}
                                required
                            />
                            <TextField
                                label="Visit Duration (minutes)"
                                name="visitTime"
                                type="number"
                                fullWidth
                                margin="normal"
                                value={availability.visitTime}
                                onChange={handleAvailabilityChange}
                                required
                            />
                            <TextField
                                select
                                label="Appointment Type"
                                name="appointmentType"
                                fullWidth
                                margin="normal"
                                value={availability.appointmentType}
                                onChange={handleAvailabilityChange}
                                required
                            >
                                <MenuItem value="PRIVATE">PRIVATE</MenuItem>
                                <MenuItem value="NFZ">NFZ</MenuItem>
                            </TextField>
                            <button className={styles.addingButton} type="submit">
                                Create Availability
                            </button>
                        </form>
                        <ToastContainer position="top-center" autoClose={4000}/>
                    </Box>
                </div>
            </main>
        </div>
    );
};

export default AddDoctorAvailability;
