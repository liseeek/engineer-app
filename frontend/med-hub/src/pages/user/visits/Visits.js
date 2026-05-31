import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
    Paper,
    Stack,
    Tab,
    Tabs,
    Typography,
    CircularProgress,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import EventBusyIcon from "@mui/icons-material/EventBusy";
import DescriptionIcon from "@mui/icons-material/Description";

import AuthenticatedLayout from "../../../layouts/AuthenticatedLayout";
import AppointmentCard from "./AppointmentCard";
import RescheduleDialog from "../../../components/RescheduleDialog";
import { request } from "../../../helpers/axiosHelper";
import { toDateTimeKey } from "../../../helpers/dateTimeSort";
import { toast, ToastContainer } from "react-toastify";

const TAB_KEYS = {
    UPCOMING: "upcoming",
    PAST: "past",
    CANCELED: "canceled",
};

function EmptyState({ tab, onBookNew }) {
    if (tab === TAB_KEYS.UPCOMING) {
        return (
            <Box
                sx={{
                    border: "1px dashed",
                    borderColor: "divider",
                    borderRadius: 2,
                    p: 3,
                    textAlign: "center",
                    backgroundColor: "background.paper",
                }}
            >
                <Stack spacing={1.5} alignItems="center">
                    <EventBusyIcon color="action" />
                    <Typography variant="body1" fontWeight={600}>
                        You have no upcoming visits.
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        Book your first appointment to get started.
                    </Typography>
                    <Button variant="contained" startIcon={<AddIcon />} onClick={onBookNew}>
                        Book new
                    </Button>
                </Stack>
            </Box>
        );
    }

    if (tab === TAB_KEYS.PAST) {
        return (
            <Box sx={{ textAlign: "center", py: 4, color: "text.secondary" }}>
                <Typography variant="body1">No past visits yet.</Typography>
            </Box>
        );
    }

    return (
        <Box sx={{ textAlign: "center", py: 4, color: "text.secondary" }}>
            <Typography variant="body1">No canceled visits.</Typography>
        </Box>
    );
}

const Visits = () => {
    const navigate = useNavigate();
    const [rows, setRows] = useState([]);
    const [activeTab, setActiveTab] = useState(TAB_KEYS.UPCOMING);
    const [openRescheduleDialog, setOpenRescheduleDialog] = useState(false);
    const [appointmentToReschedule, setAppointmentToReschedule] = useState(null);

    const [noteDialogOpen, setNoteDialogOpen] = useState(false);
    const [loadingNote, setLoadingNote] = useState(false);
    const [currentNote, setCurrentNote] = useState(null);

    const fetchAppointments = async () => {
        try {
            const response = await request("get", "/v1/users/currentUser/appointments");
            const sourceRows = Array.isArray(response.data) ? response.data : [];

            const transformedData = sourceRows.map((appointment) => ({
                id: appointment.appointmentId,
                doctor: appointment.doctor,
                location: appointment.location,
                rawDate: appointment.date,
                rawTime: appointment.time,
                visitDateTimeTs: toDateTimeKey(appointment.date, appointment.time),
                visitStatus: appointment.appointmentStatus,
                visitType: appointment.appointmentType,
                rescheduleReason: appointment.rescheduleReason || "",
            }));

            setRows(transformedData);
        } catch {
            toast.error("Failed to fetch appointments. Please try again later.");
        }
    };

    const cancelAppointment = async (id) => {
        try {
            await request("patch", `/v1/appointments/${id}/cancel`);
            toast.success("Appointment canceled successfully!");
            return true;
        } catch {
            toast.error("Failed to cancel appointment. Please try again later.");
            return false;
        }
    };

    useEffect(() => {
        fetchAppointments();
    }, []);

    const handleCancelClick = async (id) => {
        const canceled = await cancelAppointment(id);
        if (!canceled) return;

        setRows((prevRows) =>
            prevRows.map((row) => (row.id === id ? { ...row, visitStatus: "CANCELED" } : row))
        );
    };

    const handleRescheduleClick = (appointment) => {
        setAppointmentToReschedule(appointment);
        setOpenRescheduleDialog(true);
    };

    const handleConfirmReschedule = async (newSlotId) => {
        await request("patch", `/v1/appointments/${appointmentToReschedule.id}/reschedule`, { newSlotId });
        toast.success("Appointment rescheduled successfully!");
        await fetchAppointments();
    };

    const handleViewNoteClick = async (id) => {
        setNoteDialogOpen(true);
        setLoadingNote(true);
        setCurrentNote(null);
        try {
            const response = await request("get", `/v1/appointments/${id}/note`);
            setCurrentNote(response.data);
        } catch (error) {
            toast.error("Failed to fetch visit note. It might not be available yet.");
            setNoteDialogOpen(false);
        } finally {
            setLoadingNote(false);
        }
    };

    const buckets = useMemo(() => {
        const nowTs = Date.now();
        const upcoming = [];
        const past = [];
        const canceled = [];

        rows.forEach((row) => {
            if (row.visitStatus === "CANCELED") {
                canceled.push(row);
                return;
            }

            const isFutureAssigned =
                (row.visitStatus === "ACTIVE" || row.visitStatus === "RESCHEDULED") &&
                row.visitDateTimeTs >= nowTs;

            if (isFutureAssigned) {
                upcoming.push(row);
                return;
            }

            past.push(row);
        });

        upcoming.sort((a, b) => a.visitDateTimeTs - b.visitDateTimeTs);
        past.sort((a, b) => b.visitDateTimeTs - a.visitDateTimeTs);
        canceled.sort((a, b) => b.visitDateTimeTs - a.visitDateTimeTs);

        return { upcoming, past, canceled };
    }, [rows]);

    const currentRows = buckets[activeTab] || [];

    return (
        <AuthenticatedLayout>
            <Paper
                elevation={2}
                sx={{
                    maxWidth: 960,
                    mx: "auto",
                    mt: 3,
                    p: { xs: 2, sm: 4 },
                    borderRadius: 3,
                    width: "100%",
                }}
            >
                <Stack
                    direction={{ xs: "column", sm: "row" }}
                    justifyContent="space-between"
                    alignItems={{ xs: "stretch", sm: "center" }}
                    spacing={1.5}
                    mb={2}
                >
                    <Typography variant="h4" fontWeight={700}>
                        Your Visits
                    </Typography>
                    <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate("/booking")}>
                        Book new
                    </Button>
                </Stack>

                <Tabs value={activeTab} onChange={(_event, value) => setActiveTab(value)} variant="fullWidth">
                    <Tab value={TAB_KEYS.UPCOMING} label={`Upcoming (${buckets.upcoming.length})`} />
                    <Tab value={TAB_KEYS.PAST} label={`Past (${buckets.past.length})`} />
                    <Tab value={TAB_KEYS.CANCELED} label={`Canceled (${buckets.canceled.length})`} />
                </Tabs>

                <Box sx={{ mt: 2 }}>
                    {currentRows.length === 0 ? (
                        <EmptyState tab={activeTab} onBookNew={() => navigate("/booking")} />
                    ) : (
                        <Stack spacing={2}>
                            {currentRows.map((appointment) => (
                                <AppointmentCard
                                    key={appointment.id}
                                    appointment={appointment}
                                    onCancel={handleCancelClick}
                                    onReschedule={handleRescheduleClick}
                                    onViewNote={handleViewNoteClick}
                                />
                            ))}
                        </Stack>
                    )}
                </Box>
            </Paper>

            <RescheduleDialog
                open={openRescheduleDialog}
                onClose={() => {
                    setOpenRescheduleDialog(false);
                    setAppointmentToReschedule(null);
                }}
                doctorId={appointmentToReschedule?.doctor?.doctorId}
                locationId={appointmentToReschedule?.location?.locationId}
                appointmentType={appointmentToReschedule?.visitType}
                showReasonField={false}
                onConfirm={handleConfirmReschedule}
            />

            <Dialog open={noteDialogOpen} onClose={() => setNoteDialogOpen(false)} maxWidth="sm" fullWidth>
                <DialogTitle>
                    <Stack direction="row" alignItems="center" spacing={1}>
                        <DescriptionIcon color="primary" />
                        <Typography variant="h6">Visit Note</Typography>
                    </Stack>
                </DialogTitle>
                <DialogContent dividers>
                    {loadingNote ? (
                        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                            <CircularProgress />
                        </Box>
                    ) : currentNote ? (
                        <Stack spacing={2}>
                            <Box>
                                <Typography variant="subtitle2" color="text.secondary">Diagnosis</Typography>
                                <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
                                    {currentNote.diagnosis || "No diagnosis provided."}
                                </Typography>
                            </Box>
                            <Divider />
                            <Box>
                                <Typography variant="subtitle2" color="text.secondary">Prescription</Typography>
                                <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
                                    {currentNote.prescription || "No prescription provided."}
                                </Typography>
                            </Box>
                            <Divider />
                            <Box>
                                <Typography variant="subtitle2" color="text.secondary">Additional Notes</Typography>
                                <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
                                    {currentNote.notes || "No additional notes."}
                                </Typography>
                            </Box>
                        </Stack>
                    ) : (
                        <Typography color="error">Note details could not be loaded.</Typography>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setNoteDialogOpen(false)}>Close</Button>
                </DialogActions>
            </Dialog>

            <ToastContainer position="top-center" autoClose={4000} />
        </AuthenticatedLayout>
    );
};

export default Visits;
