import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';

import { request, unwrapPage } from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import CancelIcon from '@mui/icons-material/Close';
import DoneIcon from '@mui/icons-material/Done';
import { DataGrid, GridActionsCellItem, } from '@mui/x-data-grid';
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button } from '@mui/material';
import { toast, ToastContainer } from "react-toastify";

const ManageVisits = () => {
    const [rows, setRows] = useState([]);
    const [openCancelDialog, setOpenCancelDialog] = useState(false);
    const [appointmentToCancel, setAppointmentToCancel] = useState(null);
    const [cancelReason, setCancelReason] = useState('');

    const fetchAppointments = async () => {
        try {
            const response = await request('get', "/v1/workers/currentWorker/appointments?size=200");

            const transformedData = unwrapPage(response.data).map((appointment) => {
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
            toast.error("Failed to fetch appointments. Please try again later.");
        }
    };

    const cancelAppointment = async (id, reason) => {
        try {
            if (reason) {
                await request('post',
                    `/v1/facility/appointments/${id}/cancel?reason=${encodeURIComponent(reason)}`);
            } else {
                await request('patch', `/v1/appointments/${id}/cancel`);
            }
            toast.success("Appointment canceled successfully!");
            return 200;
        } catch (error) {
            toast.error("Failed to cancel appointment. Please try again later.");
            return error.response?.status || 500;
        }
    };

    const handleCancelClick = (id) => () => {
        setAppointmentToCancel(id);
        setCancelReason('');
        setOpenCancelDialog(true);
    };

    const handleConfirmCancel = async () => {
        if (!cancelReason.trim()) {
            toast.error('Please provide a reason for cancellation');
            return;
        }

        const responseStatus = await cancelAppointment(appointmentToCancel, cancelReason);
        if (responseStatus === 200) {
            setRows((prevRows) =>
                prevRows.map((row) => (row.id === appointmentToCancel ? { ...row, visitStatus: "CANCELED" } : row))
            );
            setOpenCancelDialog(false);
            setAppointmentToCancel(null);
            setCancelReason('');
        }
    };

    const completeAppointment = async (id) => {
        try {
            await request('patch', `/v1/appointments/${id}/complete`);
            toast.success("Appointment completed successfully!");
            return 200;
        } catch (error) {
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
        <AuthenticatedLayout>
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

            <Dialog open={openCancelDialog} onClose={() => setOpenCancelDialog(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Cancel Appointment</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Reason for Cancellation *"
                        fullWidth
                        margin="normal"
                        multiline
                        rows={4}
                        value={cancelReason}
                        onChange={(e) => setCancelReason(e.target.value)}
                        placeholder="Please provide a reason for canceling this appointment"
                        required
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenCancelDialog(false)}>Cancel</Button>
                    <Button onClick={handleConfirmCancel} variant="contained" color="error">
                        Confirm Cancellation
                    </Button>
                </DialogActions>
            </Dialog>

            <ToastContainer position="top-center" autoClose={4000} />
        </AuthenticatedLayout>
    );
};

export default ManageVisits;

