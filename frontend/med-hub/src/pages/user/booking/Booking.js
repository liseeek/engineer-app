import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import AuthenticatedLayout from "../../../layouts/AuthenticatedLayout";
import styles from "../../../components/Adding.module.css";

import { Autocomplete, Box, MenuItem, TextField } from "@mui/material";
import { toast, ToastContainer } from "react-toastify";
import { request, unwrapPage } from "../../../helpers/axiosHelper";
import { isFutureSlot, sortTimeAsc } from "../../../helpers/dateTimeSort";
import WeekSlotPicker from "./WeekSlotPicker";
import ConfirmBookingDialog from "./ConfirmBookingDialog";

const Booking = () => {
    const routerLocation = useLocation();
    const navigate = useNavigate();

    const [booking, setBooking] = useState({
        city: "",
        specializationId: "",
        doctorId: "",
        locationId: "",
        appointmentType: "",
    });

    const [groupedAppointments, setGroupedAppointments] = useState({});
    const [pendingAppointment, setPendingAppointment] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const [cities, setCities] = useState([]);
    const [specializations, setSpecializations] = useState([]);
    const [doctors, setDoctors] = useState([]);
    const [locations, setLocations] = useState([]);

    // Holds prefill data arriving from the AI widget via router state.
    const pendingPrefillRef = useRef(null);

    useEffect(() => {
        const fetchCities = async () => {
            try {
                const response = await request('get', `/v1/locations/cities/distinct`);
                setCities(response.data);
            } catch (error) {
                toast.error("Failed to fetch cities.");
            }
        };
        fetchCities();
    }, []);

    // Separate ref to hold the prefill doctor id across async loading stages.
    const pendingDoctorIdRef = useRef(null);

    // Consume prefill data from AI widget or DoctorProfile page (passed via router state).
    useEffect(() => {
        const state = routerLocation.state;
        if (!state?.prefillCity) return;

        setBooking(prev => ({ ...prev, city: state.prefillCity }));
        if (state.prefillSpecializationId) {
            pendingPrefillRef.current = {
                specializationId: state.prefillSpecializationId,
                specializationName: state.prefillSpecializationName || '',
                city: state.prefillCity,
            };
        }
        if (state.prefillDoctorId) {
            pendingDoctorIdRef.current = state.prefillDoctorId;
        }
        // Clear router state so F5 does not re-trigger the prefill.
        navigate(routerLocation.pathname, { replace: true, state: null });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Once specializations are loaded, apply the pending prefill specialization.
    useEffect(() => {
        const pending = pendingPrefillRef.current;
        if (!pending) return;
        if (specializations.length === 0) return;

        const match = specializations.find(s => s.specializationId === pending.specializationId);
        if (match) {
            setBooking(prev => ({ ...prev, specializationId: match.specializationId }));
        } else {
            toast.warn(`No "${pending.specializationName}" available in ${pending.city}. Please select a specialization manually.`);
        }
        pendingPrefillRef.current = null;
    }, [specializations]);

    // Once doctors are loaded (after city+spec selected), apply the pending prefill doctor.
    useEffect(() => {
        const pendingDocId = pendingDoctorIdRef.current;
        if (!pendingDocId || doctors.length === 0) return;

        const match = doctors.find(d => d.doctorId === pendingDocId);
        if (match) {
            setBooking(prev => ({ ...prev, doctorId: match.doctorId }));
        }
        pendingDoctorIdRef.current = null;
    }, [doctors]);

    const handleCityChange = (event, newValue) => {
        setBooking({ ...booking, city: newValue });
    };

    useEffect(() => {
        const fetchSpecializations = async () => {
            if (booking.city) {
                try {
                    const response = await request('get', `/v1/specializations/by-city?city=${encodeURIComponent(booking.city)}`);
                    setSpecializations(response.data);
                } catch (error) {
                    toast.error("Failed to fetch specializations.");
                }
            }
        };
        fetchSpecializations();
    }, [booking.city]);

    const handleSpecializationChange = (event, newValue) => {
        setBooking({ ...booking, specializationId: newValue?.specializationId || "" });
    };

    useEffect(() => {
        const fetchDoctors = async () => {
            if (booking.city && booking.specializationId) {
                try {
                    const response = await request('get', `/v1/doctors/by-city-and-specialization?city=${encodeURIComponent(booking.city)}&specializationId=${booking.specializationId}&size=200`);
                    const data = unwrapPage(response.data).map((doc) => ({
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
        setBooking({ ...booking, doctorId: newValue?.doctorId || "" });
    };

    useEffect(() => {
        const fetchLocations = async () => {
            if (booking.doctorId) {
                try {
                    const response = await request('get', `/v1/doctors/${booking.doctorId}/locations`);

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
        setBooking({ ...booking, locationId: newValue?.locationId || "" });
    };

    const handleAppointmentTypeChange = (event) => {
        setBooking({ ...booking, appointmentType: event.target.value });
    };

    const fetchAppointments = useCallback(async () => {
        if (!(booking.locationId && booking.doctorId && booking.appointmentType)) {
            setGroupedAppointments({});
            return;
        }

        try {
            const response = await request('get', `/v1/availability?locationId=${booking.locationId}&doctorId=${booking.doctorId}&appointmentType=${booking.appointmentType}`);
            const now = new Date();
            const grouped = response.data.reduce((acc, appointment) => {
                if (!isFutureSlot(appointment.date, appointment.time, now)) return acc;
                const date = appointment.date;
                if (!acc[date]) acc[date] = [];
                acc[date].push(appointment);
                return acc;
            }, {});

            Object.keys(grouped).forEach((dateKey) => {
                grouped[dateKey].sort((left, right) => sortTimeAsc(left.time, right.time));
            });
            setGroupedAppointments(grouped);
        } catch (error) {
            toast.error("Failed to fetch appointments.");
        }
    }, [booking.locationId, booking.doctorId, booking.appointmentType]);

    useEffect(() => {
        fetchAppointments();
    }, [fetchAppointments]);

    const slotsVisible = Boolean(
        booking.locationId && booking.doctorId && booking.appointmentType
    );

    const bookingContext = useMemo(() => {
        const doctor = doctors.find((doc) => doc.doctorId === booking.doctorId);
        const location = locations.find((loc) => loc.locationId === booking.locationId);
        return {
            doctorName: doctor?.fullName || "",
            locationName: location?.locationName || "",
            appointmentType: booking.appointmentType,
        };
    }, [doctors, locations, booking.doctorId, booking.locationId, booking.appointmentType]);

    const handleSlotSelect = (appointment) => {
        setPendingAppointment(appointment);
    };

    const handleConfirmClose = () => {
        if (submitting) return;
        setPendingAppointment(null);
    };

    const handleConfirmBooking = async () => {
        if (!pendingAppointment) return;
        setSubmitting(true);
        try {
            await request('patch', `/v1/appointments/${pendingAppointment.appointmentId}`);
            toast.success("Appointment booked successfully.");
            setPendingAppointment(null);
            await fetchAppointments();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to book appointment.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <AuthenticatedLayout>
                <div className={styles.addingContainer}>
                    <Box
                        sx={{
                            width: "90%",
                            maxWidth: "720px",
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
                            {slotsVisible && (
                                <WeekSlotPicker
                                    groupedAppointments={groupedAppointments}
                                    onSelect={handleSlotSelect}
                                />
                            )}
                        </form>
                        <ToastContainer position="top-center" autoClose={4000} />
                    </Box>
                </div>
                <ConfirmBookingDialog
                    open={Boolean(pendingAppointment)}
                    appointment={pendingAppointment}
                    context={bookingContext}
                    onConfirm={handleConfirmBooking}
                    onClose={handleConfirmClose}
                    isSubmitting={submitting}
                />
        </AuthenticatedLayout>
    );
};

export default Booking;
