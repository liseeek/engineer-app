import React from "react";
import {
    Box,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
    Typography,
} from "@mui/material";
import { formatTimeHm } from "../../../helpers/dateTimeSort";

const formatLongDate = (isoDate) => {
    if (!isoDate) return "";
    const parsed = new Date(`${isoDate}T00:00:00`);
    if (Number.isNaN(parsed.getTime())) return isoDate;
    return parsed.toLocaleDateString("en-US", {
        weekday: "short",
        day: "numeric",
        month: "short",
        year: "numeric",
    });
};

const SummaryRow = ({ label, value }) => (
    <Box sx={{ display: "flex", justifyContent: "space-between", gap: 2, py: 0.5 }}>
        <Typography variant="body2" color="text.secondary">
            {label}
        </Typography>
        <Typography variant="body2" sx={{ fontWeight: 500, textAlign: "right" }}>
            {value || "—"}
        </Typography>
    </Box>
);

const ConfirmBookingDialog = ({
    open,
    appointment,
    context,
    onConfirm,
    onClose,
    isSubmitting = false,
}) => {
    const doctorName = context?.doctorName || "";
    const locationName = context?.locationName || "";
    const appointmentType = context?.appointmentType || "";

    return (
        <Dialog open={open} onClose={isSubmitting ? undefined : onClose} fullWidth maxWidth="xs">
            <DialogTitle>Confirm appointment</DialogTitle>
            <DialogContent dividers>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Please review your appointment details before booking.
                </Typography>
                <SummaryRow label="Doctor" value={doctorName} />
                <SummaryRow label="Facility" value={locationName} />
                <SummaryRow label="Type" value={appointmentType} />
                <Divider sx={{ my: 1 }} />
                <SummaryRow label="Date" value={formatLongDate(appointment?.date)} />
                <SummaryRow label="Time" value={formatTimeHm(appointment?.time)} />
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} disabled={isSubmitting}>
                    Cancel
                </Button>
                <Button
                    onClick={onConfirm}
                    variant="contained"
                    disabled={isSubmitting || !appointment}
                    startIcon={isSubmitting ? <CircularProgress size={16} color="inherit" /> : null}
                >
                    {isSubmitting ? "Booking..." : "Book"}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ConfirmBookingDialog;
