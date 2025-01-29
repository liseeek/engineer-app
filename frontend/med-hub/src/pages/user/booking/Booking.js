import React, {useEffect, useState} from "react";
import {Helmet} from "react-helmet";
import styles from "../../../components/Adding.module.css";
import NavRespo from "../../../components/NavRespo";
import logo from "../../../img/logo.svg";
import axios from "axios";
import {Autocomplete, Box, MenuItem, TextField} from "@mui/material";
import {toast, ToastContainer} from "react-toastify";
import {getAuthToken} from "../../../helpers/axiosHelper";

const Booking = () => {
    const [booking, setBooking] = useState({
        city: "",
        specializationId: "",
        doctorId: "",
        locationId: "",
        appointmentType: "",
    });

    const [appointments, setAppointments] = useState([]);
    const [groupedAppointments, setGroupedAppointments] = useState({});
    const [selectedDate, setSelectedDate] = useState("");
    const [selectedTime, setSelectedTime] = useState("");

    const [cities, setCities] = useState([]);
    const [specializations, setSpecializations] = useState([]);
    const [doctors, setDoctors] = useState([]);
    const [locations, setLocations] = useState([]);

    useEffect(() => {
        const fetchCities = async () => {
            const token = getAuthToken();
            if (!token) {
                toast.error("You are not authenticated. Please log in.");
                return;
            }
            try {
                const response = await axios.get(`/v1/locations/cities/distinct`, {
                    headers: {Authorization: `Bearer ${token}`},
                });
                setCities(response.data);
            } catch (error) {
                toast.error("Failed to fetch cities.");
            }
        };
        fetchCities();
    }, []);

    const handleCityChange = (event, newValue) => {
        setBooking({...booking, city: newValue});
    };

    useEffect(() => {
        const fetchSpecializations = async () => {
            if (booking.city) {
                const token = getAuthToken();
                if (!token) {
                    toast.error("You are not authenticated. Please log in.");
                    return;
                }
                try {
                    const response = await axios.get(`/v1/specializations/by-city`, {
                        params: {
                            city: booking.city,
                        },
                        headers: {Authorization: `Bearer ${token}`},
                    });
                    setSpecializations(response.data);
                } catch (error) {
                    toast.error("Failed to fetch specializations.");
                }
            }
        };
        fetchSpecializations();
    }, [booking.city]);

    const handleSpecializationChange = (event, newValue) => {
        setBooking({...booking, specializationId: newValue?.specializationId || ""});
    };

    useEffect(() => {
        const fetchDoctors = async () => {
            if (booking.city && booking.specializationId) {
                const token = getAuthToken();
                if (!token) {
                    toast.error("You are not authenticated. Please log in.");
                    return;
                }
                try {
                    const response = await axios.get(`/v1/doctors/by-city-and-specialization`, {
                        params: {
                            city: booking.city,
                            specializationId: booking.specializationId,
                        },
                        headers: {Authorization: `Bearer ${token}`},
                    });
                    const data = response.data.map((doc) => ({
                        doctorId: doc.doctorId,
                        fullName: `${doc.name} ${doc.surname}`,
                    }));
                    setDoctors(data);
                } catch (error) {
                    toast.error("Failed to fetch doctors.");
                }
            }
        };
        fetchDoctors();
    }, [booking.city, booking.specializationId]);

    const handleDoctorChange = (event, newValue) => {
        setBooking({...booking, doctorId: newValue?.doctorId || ""});
    };

    useEffect(() => {
        const fetchLocations = async () => {
            if (booking.doctorId) {
                const token = getAuthToken();
                if (!token) {
                    toast.error("You are not authenticated. Please log in.");
                    return;
                }
                try {
                    const response = await axios.get(`/v1/doctors/${booking.doctorId}/locations`, {
                        headers: {Authorization: `Bearer ${token}`},
                    });

                    if (response.status === 200) {
                        const filteredLocations = response.data.filter(
                            (location) => location.city === booking.city
                        );
                        setLocations(filteredLocations);
                    }
                } catch (error) {
                    toast.error("Failed to fetch locations.");
                }
            } else {
                setLocations([]);
            }
        };
        fetchLocations();
    }, [booking.doctorId, booking.city]);

    const handleLocationChange = (event, newValue) => {
        setBooking({...booking, locationId: newValue?.locationId || ""});
    };

    const handleAppointmentTypeChange = (event) => {
        setBooking({...booking, appointmentType: event.target.value});
    };

    useEffect(() => {
        const fetchAppointments = async () => {
            if (booking.locationId && booking.doctorId && booking.appointmentType) {
                const token = getAuthToken();
                if (!token) {
                    toast.error("You are not authenticated. Please log in.");
                    return;
                }
                try {
                    const response = await axios.get(`/v1/availability`, {
                        params: {
                            locationId: booking.locationId,
                            doctorId: booking.doctorId,
                            appointmentType: booking.appointmentType,
                        },
                        headers: { Authorization: `Bearer ${token}` },
                    });
                    setAppointments(response.data);

                    const grouped = response.data.reduce((acc, appointment) => {
                        const date = appointment.date;
                        if (!acc[date]) acc[date] = [];
                        acc[date].push(appointment);
                        return acc;
                    }, {});
                    setGroupedAppointments(grouped);
                } catch (error) {
                    toast.error("Failed to fetch appointments.");
                }
            }
        };
        fetchAppointments();
    }, [booking.locationId, booking.doctorId, booking.appointmentType]);

    const handleDateChange = (event) => {
        setSelectedDate(event.target.value);
        setSelectedTime("");
    };

    const handleTimeChange = (event) => {
        setSelectedTime(event.target.value);
    };

    const handleAppointmentSelect = async (appointmentId) => {
        const token = getAuthToken();
        if (!token) {
            toast.error("You are not authenticated. Please log in.");
            return;
        }
        try {
            await axios.patch(`/v1/appointments/${appointmentId}`, null, {
                headers: {Authorization: `Bearer ${token}`},
            });
            toast.success("Appointment booked successfully.");
        } catch (error) {
            toast.error("Failed to book appointment.");
        }
    };

    const handleBook = async () => {
        const selectedAppointment = groupedAppointments[selectedDate]?.find(
            (appointment) => appointment.time === selectedTime
        );
        if (selectedAppointment) {
            await handleAppointmentSelect(selectedAppointment.appointmentId);
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
                            width: "90%",
                            maxWidth: "600px",
                            padding: "20px",
                            backgroundColor: "#fff",
                            borderRadius: "8px",
                            boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
                            margin: "0 auto",
                        }}
                    >
                        <h1 className={styles.addingHeader}>Book Appointment</h1>
                        <form>
                            <Autocomplete
                                options={cities}
                                value={booking.city}
                                onChange={handleCityChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search City"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />
                            <Autocomplete
                                options={specializations}
                                getOptionLabel={(option) => option.specializationName}
                                value={
                                    specializations.find(
                                        (spec) => spec.specializationId === booking.specializationId
                                    ) || null
                                }
                                onChange={handleSpecializationChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search Specialization"
                                        fullWidth
                                        margin="normal"

                                        required
                                    />
                                )}
                            />
                            <Autocomplete
                                options={doctors}
                                getOptionLabel={(option) => option.fullName}
                                value={
                                    doctors.find(
                                        (doc) => doc.doctorId === booking.doctorId
                                    ) || null
                                }
                                onChange={handleDoctorChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search Doctor"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />
                            <Autocomplete
                                options={locations}
                                getOptionLabel={(option) => option.locationName}
                                value={
                                    locations.find(
                                        (loc) => loc.locationId === booking.locationId
                                    ) || null
                                }
                                onChange={handleLocationChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Search Facility"
                                        fullWidth
                                        margin="normal"
                                        required
                                    />
                                )}
                            />
                            <TextField
                                select
                                label="Select Appointment Type"
                                value={booking.appointmentType}
                                onChange={handleAppointmentTypeChange}
                                fullWidth
                                margin="normal"
                                required
                            >
                                <MenuItem value="PRIVATE">PRIVATE</MenuItem>
                                <MenuItem value="NFZ">NFZ</MenuItem>
                            </TextField>
                            <TextField
                                select
                                label="Select Date"
                                value={selectedDate}
                                onChange={handleDateChange}
                                fullWidth
                                margin="normal"
                                required
                            >
                                <MenuItem value="" disabled>
                                    Select a date
                                </MenuItem>
                                {Object.keys(groupedAppointments).map((date) => (
                                    <MenuItem key={date} value={date}>
                                        {date}
                                    </MenuItem>
                                ))}
                            </TextField>
                            {selectedDate && (
                                <TextField
                                    select
                                    label="Select Time"
                                    value={selectedTime}
                                    onChange={handleTimeChange}
                                    fullWidth
                                    margin="normal"
                                    required
                                >
                                    <MenuItem value="" disabled>
                                        Select a time
                                    </MenuItem>
                                    {groupedAppointments[selectedDate]?.map((appointment) => (
                                        <MenuItem key={appointment.time} value={appointment.time}>
                                            {appointment.time}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            )}
                            {selectedTime && (
                                <button className={styles.addingButton} onClick={handleBook}>
                                    Book
                                </button>
                            )}
                        </form>
                        <ToastContainer position="top-center" autoClose={4000}/>
                    </Box>
                </div>
            </main>
        </div>
    );
};

export default Booking;
