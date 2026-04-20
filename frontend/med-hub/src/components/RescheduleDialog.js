import React, { useEffect, useMemo, useState } from "react";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, TextField } from "@mui/material";
import { toast } from "react-toastify";
import { request } from "../helpers/axiosHelper";
import { formatTimeHm, isFutureSlot } from "../helpers/dateTimeSort";

const RescheduleDialog = ({
    open,
    onClose,
    doctorId,
    locationId,
    appointmentType,
    showReasonField = false,
    onConfirm,
}) => {
    const [groupedAppointments, setGroupedAppointments] = useState({});
    const [selectedDate, setSelectedDate] = useState("");
    const [selectedTime, setSelectedTime] = useState("");
    const [reason, setReason] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (!open) {
            setGroupedAppointments({});
            setSelectedDate("");
            setSelectedTime("");
            setReason("");
            return;
        }

        const fetchAvailability = async () => {
            if (!doctorId || !locationId || !appointmentType) {
                setGroupedAppointments({});
                return;
            }
            try {
                const response = await request(
                    "get",
                    `/v1/availability?locationId=${locationId}&doctorId=${doctorId}&appointmentType=${appointmentType}`
                );
                const now = new Date();
                const grouped = response.data.reduce((acc, appointment) => {
                    if (!isFutureSlot(appointment.date, appointment.time, now)) {
                        return acc;
                    }
                    const date = appointment.date;
                    if (!acc[date]) {
                        acc[date] = [];
                    }
                    acc[date].push(appointment);
                    return acc;
                }, {});
                setGroupedAppointments(grouped);
            } catch (error) {
                toast.error("Failed to fetch availability for reschedule.");
            }
        };

        fetchAvailability();
    }, [open, doctorId, locationId, appointmentType]);

    const hasAnySlots = useMemo(
        () => Object.keys(groupedAppointments).length > 0,
        [groupedAppointments]
    );

    const handleConfirm = async () => {
        const selectedAppointment = groupedAppointments[selectedDate]?.find(
            (appointment) => appointment.time === selectedTime
        );
        if (!selectedAppointment) {
            toast.error("Please select a valid date and time.");
            return;
        }
        if (showReasonField && !reason.trim()) {
            toast.error("Please provide reschedule reason.");
            return;
        }

        try {
            setIsSubmitting(true);
            await onConfirm(selectedAppointment.appointmentId, reason.trim());
            setSelectedDate("");
            setSelectedTime("");
            setReason("");
            onClose();
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to reschedule appointment.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>Reschedule Appointment</DialogTitle>
            <DialogContent>
                <TextField
                    select
                    label="Select Date"
                    value={selectedDate}
                    onChange={(event) => {
                        setSelectedDate(event.target.value);
                        setSelectedTime("");
                    }}
                    fullWidth
                    margin="normal"
                    required
                    disabled={!hasAnySlots}
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

                <TextField
                    select
                    label="Select Time"
                    value={selectedTime}
                    onChange={(event) => setSelectedTime(event.target.value)}
                    fullWidth
                    margin="normal"
                    required
                    disabled={!selectedDate}
                >
                    <MenuItem value="" disabled>
                        Select a time
                    </MenuItem>
                    {(groupedAppointments[selectedDate] || []).map((appointment) => (
                        <MenuItem key={appointment.appointmentId} value={appointment.time}>
                            {formatTimeHm(appointment.time)}
                        </MenuItem>
                    ))}
                </TextField>

                {showReasonField && (
                    <TextField
                        label="Reason for Reschedule *"
                        fullWidth
                        margin="normal"
                        multiline
                        rows={3}
                        value={reason}
                        onChange={(event) => setReason(event.target.value)}
                        placeholder="Please provide reason for moving the appointment"
                        required
                    />
                )}

                {!hasAnySlots && (
                    <p style={{ marginTop: "8px", marginBottom: 0 }}>
                        No available slots found for the same doctor and facility.
                    </p>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} disabled={isSubmitting}>
                    Cancel
                </Button>
                <Button onClick={handleConfirm} variant="contained" disabled={isSubmitting || !hasAnySlots}>
                    Confirm
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default RescheduleDialog;
