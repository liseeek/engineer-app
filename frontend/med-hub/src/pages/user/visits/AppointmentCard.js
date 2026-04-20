import React from "react";
import {
    Alert,
    Avatar,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    Stack,
    Typography,
} from "@mui/material";
import CancelIcon from "@mui/icons-material/Close";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import SwapHorizIcon from "@mui/icons-material/SwapHoriz";
import { formatTimeHm } from "../../../helpers/dateTimeSort";

const dateFormatter = new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
});

const statusColorMap = {
    ACTIVE: "success",
    RESCHEDULED: "info",
    COMPLETED: "default",
    CANCELED: "error",
};

const statusLabelMap = {
    ACTIVE: "Active",
    RESCHEDULED: "Rescheduled",
    COMPLETED: "Completed",
    CANCELED: "Canceled",
};

function StatusChip({ status }) {
    return (
        <Chip
            size="small"
            color={statusColorMap[status] || "default"}
            label={statusLabelMap[status] || status || "Unknown"}
        />
    );
}

export default function AppointmentCard({ appointment, onCancel, onReschedule }) {
    const visitDate = new Date(`${appointment.rawDate}T${appointment.rawTime}`);
    const isFutureAssigned =
        (appointment.visitStatus === "ACTIVE" || appointment.visitStatus === "RESCHEDULED") &&
        appointment.visitDateTimeTs >= Date.now();

    const doctorFullName = `${appointment.doctor?.name || ""} ${appointment.doctor?.surname || ""}`.trim();
    const doctorSpecs =
        (appointment.doctor?.specializations || []).map((spec) => spec.specializationName).join(", ") ||
        "Specialization not provided";
    const locationLine = [
        appointment.location?.locationName,
        appointment.location?.address,
        appointment.location?.city,
    ]
        .filter(Boolean)
        .join(", ");

    const dateLabel = Number.isNaN(visitDate.getTime())
        ? appointment.rawDate
        : dateFormatter.format(visitDate);
    const timeLabel = formatTimeHm(appointment.rawTime);
    const reason = appointment.rescheduleReason?.trim();

    return (
        <Card variant="outlined">
            <CardContent>
                <Stack
                    direction={{ xs: "column", sm: "row" }}
                    spacing={2}
                    alignItems={{ xs: "flex-start", sm: "center" }}
                    justifyContent="space-between"
                >
                    <Stack direction="row" spacing={2} alignItems="center" minWidth={0}>
                        <Avatar src={appointment.doctor?.avatarUrl || undefined}>
                            {appointment.doctor?.name?.[0] || "D"}
                        </Avatar>
                        <Box minWidth={0}>
                            <Typography fontWeight={700} noWrap>
                                {doctorFullName || "Doctor"}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" noWrap>
                                {doctorSpecs}
                            </Typography>
                            <Stack direction="row" spacing={0.5} alignItems="center" mt={0.5} minWidth={0}>
                                <LocationOnIcon fontSize="small" color="action" />
                                <Typography variant="body2" color="text.secondary" noWrap>
                                    {locationLine || "Facility details unavailable"}
                                </Typography>
                            </Stack>
                        </Box>
                    </Stack>

                    <Stack alignItems={{ xs: "flex-start", sm: "flex-end" }} spacing={1} flexShrink={0}>
                        <Typography variant="h6" fontWeight={700}>
                            {dateLabel} - {timeLabel}
                        </Typography>

                        <Stack direction="row" spacing={1}>
                            <StatusChip status={appointment.visitStatus} />
                            <Chip size="small" variant="outlined" label={appointment.visitType || "Visit"} />
                        </Stack>

                        {isFutureAssigned && (
                            <Stack direction="row" spacing={1}>
                                <Button
                                    size="small"
                                    startIcon={<SwapHorizIcon />}
                                    onClick={() => onReschedule?.(appointment)}
                                >
                                    Reschedule
                                </Button>
                                <Button
                                    size="small"
                                    color="error"
                                    startIcon={<CancelIcon />}
                                    onClick={() => onCancel?.(appointment.id)}
                                >
                                    Cancel
                                </Button>
                            </Stack>
                        )}
                    </Stack>
                </Stack>

                {reason && (
                    <Alert icon={<InfoOutlinedIcon fontSize="inherit" />} severity="info" sx={{ mt: 2 }}>
                        <strong>Reschedule reason:</strong> {reason}
                    </Alert>
                )}
            </CardContent>
        </Card>
    );
}
