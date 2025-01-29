import React, {useEffect, useState} from 'react';
import {Helmet} from "react-helmet";
import styles from '../../../components/Adding.module.css';
import NavRespo from '../../../components/NavRespo';
import logo from '../../../img/logo.svg';
import axios from "axios";
import {getAuthToken} from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import CancelIcon from '@mui/icons-material/Close';
import DoneIcon from '@mui/icons-material/Done';
import {DataGrid, GridActionsCellItem,} from '@mui/x-data-grid';
import {toast, ToastContainer} from "react-toastify";

const ManageVisits = () => {
    const [rows, setRows] = useState([]);

    const fetchAppointments = async () => {
        const token = getAuthToken();
        if (!token) {
            toast.error("You are not authenticated. Please log in.");
            return;
        }

        try {
            const response = await axios.get("/v1/workers/currentWorker/appointments", {
                headers: { Authorization: `Bearer ${token}` },
            });

            const transformedData = response.data.map((appointment) => {
                const visitDateTime = new Date(`${appointment.date}T${appointment.time}`);

                const formattedDate = new Intl.DateTimeFormat("en-GB", {
                    dateStyle: "short",
                    timeStyle: "short",
                    hour12: false,
                }).format(visitDateTime);

                return {
                    id: appointment.appointmentId,
                    doctor: `${appointment.doctor.name} ${appointment.doctor.surname}`,
                    facility: appointment.location.locationName,
                    address: appointment.location.address,
                    visitDateTime: formattedDate,
                    visitStatus: appointment.appointmentStatus,
                    visitType: appointment.appointmentType,
                };
            });

            setRows(transformedData);
        } catch (error) {
            console.error("Failed to fetch appointments:", error);
            toast.error("Failed to fetch appointments. Please try again later.");
        }
    };

    const cancelAppointment = async (id) => {
        const token = getAuthToken();
        if (!token) {
            toast.error("You are not authenticated. Please log in.");
            return null;
        }

        try {
            await axios.patch(`/v1/appointments/${id}/cancel`, null, {
                headers: { Authorization: `Bearer ${token}` },
            });
            toast.success("Appointment canceled successfully!");
            return 200;
        } catch (error) {
            console.error("Failed to cancel appointment:", error);
            toast.error("Failed to cancel appointment. Please try again later.");
            return error.response?.status || 500;
        }
    };

    const handleCancelClick = (id) => async () => {
        const responseStatus = await cancelAppointment(id);
        if (responseStatus === 200) {
            setRows((prevRows) =>
                prevRows.map((row) => (row.id === id ? { ...row, visitStatus: "CANCELED" } : row))
            );
        }
    };

    const completeAppointment = async (id) => {
        const token = getAuthToken();
        if (!token) {
            toast.error("You are not authenticated. Please log in.");
            return null;
        }

        try {
            await axios.patch(`/v1/appointments/${id}/complete`, null, {
                headers: { Authorization: `Bearer ${token}` },
            });
            toast.success("Appointment completed successfully!");
            return 200;
        } catch (error) {
            console.error("Failed to complete appointment:", error);
            toast.error("Failed to complete appointment. Please try again later.");
            return error.response?.status || 500;
        }
    };

    const handleCompleteClick = (id) => async () => {
        const responseStatus = await completeAppointment(id);
        if (responseStatus === 200) {
            setRows((prevRows) =>
                prevRows.map((row) => (row.id === id ? { ...row, visitStatus: "COMPLETED" } : row))
            );
        }
    };

    useEffect(() => {
        fetchAppointments();
    }, []);

    const columns = [
        { field: "doctor", headerName: "Doctor", width: 180, editable: false },
        { field: "facility", headerName: "Facility", width: 180, editable: false },
        { field: "address", headerName: "Address", width: 180, editable: false },
        {
            field: "visitDateTime",
            headerName: "Visit Date & Time",
            width: 150,
            editable: false,
        },
        { field: "visitStatus", headerName: "Visit Status", width: 130, editable: false },
        { field: "visitType", headerName: "Visit Type", width: 90, editable: false },
        {
            field: "doneAction",
            headerName: "Complete",
            width: 100,
            type: "actions",
            getActions: ({ id }) => [
                <GridActionsCellItem
                    icon={<DoneIcon />}
                    label="Done"
                    className="textPrimary"
                    onClick={handleCompleteClick(id)}
                    color="inherit"
                />,
            ],
        },
        {
            field: "cancelAction",
            headerName: "Cancel",
            width: 100,
            type: "actions",
            getActions: ({ id }) => [
                <GridActionsCellItem
                    icon={<CancelIcon />}
                    label="Cancel"
                    className="textPrimary"
                    onClick={handleCancelClick(id)}
                    color="inherit"
                />,
            ],
        },
    ];

    return (
        <div>
            <Helmet>
                <meta name="viewport" content="width=device-width, initial-scale=1" />
            </Helmet>
            <div className={styles.addingBaseContainer}>
                <header className={styles.addingHeader}>
                    <div className={styles.addingLogo}>
                        <img src={logo} alt="Logo" />
                    </div>
                    <NavRespo />
                </header>
            </div>
            <main className={styles.addingMain}>
                <div className={styles.addingContainer}>
                    <Box
                        sx={{
                            width: "90%",
                            maxWidth: "1150px",
                            padding: "20px",
                            backgroundColor: "#fff",
                            borderRadius: "8px",
                            boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
                            margin: "0 auto",
                        }}
                    >
                        <h1 className={styles.addingHeader}>Manage Visits in Your Facility</h1>
                        <Box
                            sx={{
                                height: 500,
                                width: "100%",
                                "& .actions": {
                                    color: "text.secondary",
                                },
                                "& .textPrimary": {
                                    color: "text.primary",
                                },
                            }}
                        >
                            <DataGrid rows={rows} columns={columns} editMode="row" />
                        </Box>
                    </Box>
                </div>
            </main>
            <ToastContainer position="top-center" autoClose={4000} />
        </div>
    );
};

export default ManageVisits;

