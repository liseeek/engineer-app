import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';

import { request } from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import { DataGrid, GridActionsCellItem } from '@mui/x-data-grid';
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button } from '@mui/material';
import NoteIcon from '@mui/icons-material/Note';
import { toast, ToastContainer } from "react-toastify";

const DoctorSchedule = () => {
    const [rows, setRows] = useState([]);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [openDialog, setOpenDialog] = useState(false);
    const [noteData, setNoteData] = useState({
        diagnosis: '',
        prescription: '',
        notes: ''
    });

    const fetchAppointments = async () => {
        try {
            const response = await request('get', "/v1/doctor/appointments");

            const transformedData = response.data.map((appointment) => {
                const visitDateTime = new Date(`${appointment.date}T${appointment.time}`);

                const formattedDate = new Intl.DateTimeFormat("en-GB", {
                    dateStyle: "short",
                    timeStyle: "short",
                    hour12: false,
                }).format(visitDateTime);

                return {
                    id: appointment.appointmentId,
                    patient: `${appointment.user.name} ${appointment.user.surname}`,
                    facility: appointment.location.locationName,
                    address: appointment.location.address,
                    visitDateTime: formattedDate,
                    visitStatus: appointment.appointmentStatus,
                    visitType: appointment.appointmentType,
                    rescheduleReason: appointment.rescheduleReason || "",
                };
            });

            setRows(transformedData);
        } catch {
            toast.error("Failed to fetch appointments. Please try again later.");
        }
    };

    const handleAddNoteClick = (id) => () => {
        const appointment = rows.find(row => row.id === id);
        setSelectedAppointment(appointment);
        setNoteData({ diagnosis: '', prescription: '', notes: '' });
        setOpenDialog(true);
    };

    const handleNoteChange = (e) => {
        setNoteData({ ...noteData, [e.target.name]: e.target.value });
    };

    const handleSaveNote = async () => {
        if (!noteData.diagnosis.trim()) {
            toast.error('Diagnosis is required');
            return;
        }

        try {
            await request('post', `/v1/doctor/appointments/${selectedAppointment.id}/note`, noteData);
            toast.success("Visit note added successfully!");
            setOpenDialog(false);
            fetchAppointments();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to add visit note. Please try again later.");
        }
    };

    useEffect(() => {
        fetchAppointments();
    }, []);

    const columns = [
        { field: "patient", headerName: "Patient", width: 180, editable: false },
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
        { field: "rescheduleReason", headerName: "Reschedule Reason", width: 220, editable: false },
        {
            field: "addNote",
            headerName: "Add Note",
            width: 120,
            type: "actions",
            getActions: ({ id, row }) => [
                <GridActionsCellItem
                    icon={<NoteIcon />}
                    label="Add Note"
                    className="textPrimary"
                    onClick={handleAddNoteClick(id)}
                    color="inherit"
                    disabled={row.visitStatus === 'COMPLETED' || row.visitStatus === 'CANCELED'}
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
                        <h1 className={styles.addingHeader}>My Schedule</h1>
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

            <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="md" fullWidth>
                <DialogTitle>Add Visit Note</DialogTitle>
                <DialogContent>
                    {selectedAppointment && (
                        <Box sx={{ mb: 2 }}>
                            <Box sx={{ mb: 2 }}>
                                <strong>Patient:</strong> {selectedAppointment.patient}
                            </Box>
                            <Box sx={{ mb: 2 }}>
                                <strong>Date & Time:</strong> {selectedAppointment.visitDateTime}
                            </Box>
                            <Box sx={{ mb: 2 }}>
                                <strong>Facility:</strong> {selectedAppointment.facility}
                            </Box>
                        </Box>
                    )}
                    <TextField
                        label="Diagnosis *"
                        name="diagnosis"
                        fullWidth
                        margin="normal"
                        multiline
                        rows={3}
                        value={noteData.diagnosis}
                        onChange={handleNoteChange}
                        required
                    />
                    <TextField
                        label="Prescription"
                        name="prescription"
                        fullWidth
                        margin="normal"
                        multiline
                        rows={3}
                        value={noteData.prescription}
                        onChange={handleNoteChange}
                    />
                    <TextField
                        label="Notes"
                        name="notes"
                        fullWidth
                        margin="normal"
                        multiline
                        rows={3}
                        value={noteData.notes}
                        onChange={handleNoteChange}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
                    <Button onClick={handleSaveNote} variant="contained" color="primary">
                        Save Note
                    </Button>
                </DialogActions>
            </Dialog>

            <ToastContainer position="top-center" autoClose={4000} />
        </AuthenticatedLayout>
    );
};

export default DoctorSchedule;
