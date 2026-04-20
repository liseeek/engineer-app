import React, { useMemo, useState } from "react";
import { Box, Chip, IconButton, Typography } from "@mui/material";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";

import { groupByBucket, TIME_BUCKETS } from "../../../helpers/timeBucket";
import { formatTimeHm } from "../../../helpers/dateTimeSort";

const DAYS_IN_VIEW = 7;

const pad = (value) => String(value).padStart(2, "0");

const toIsoDate = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const startOfDay = (date) => {
    const copy = new Date(date);
    copy.setHours(0, 0, 0, 0);
    return copy;
};

const addDays = (date, days) => {
    const copy = new Date(date);
    copy.setDate(copy.getDate() + days);
    return copy;
};

const formatWeekday = (date) =>
    date.toLocaleDateString("en-US", { weekday: "short" });

const formatDayMonth = (date) =>
    date.toLocaleDateString("en-US", { day: "2-digit", month: "short" });

const formatRangeLabel = (start, endInclusive) => {
    const sameMonth = start.getMonth() === endInclusive.getMonth();
    const left = start.toLocaleDateString("en-US", { day: "numeric", month: sameMonth ? undefined : "short" });
    const right = endInclusive.toLocaleDateString("en-US", { day: "numeric", month: "short", year: start.getFullYear() !== endInclusive.getFullYear() ? "numeric" : undefined });
    return `${left} – ${right}`;
};

const WeekSlotPicker = ({ groupedAppointments, onSelect }) => {
    const today = useMemo(() => startOfDay(new Date()), []);
    const [weekStart, setWeekStart] = useState(today);

    const days = useMemo(() => {
        return Array.from({ length: DAYS_IN_VIEW }, (_, index) => {
            const date = addDays(weekStart, index);
            const iso = toIsoDate(date);
            const dayAppointments = groupedAppointments?.[iso] ?? [];
            return {
                date,
                iso,
                isToday: date.getTime() === today.getTime(),
                buckets: groupByBucket(dayAppointments),
                totalSlots: dayAppointments.length,
            };
        });
    }, [weekStart, groupedAppointments, today]);

    const totalSlotsInView = useMemo(
        () => days.reduce((sum, day) => sum + day.totalSlots, 0),
        [days]
    );

    const canGoBack = weekStart.getTime() > today.getTime();

    const handlePrevious = () => {
        if (!canGoBack) return;
        const candidate = addDays(weekStart, -DAYS_IN_VIEW);
        setWeekStart(candidate.getTime() < today.getTime() ? today : candidate);
    };

    const handleNext = () => {
        setWeekStart(addDays(weekStart, DAYS_IN_VIEW));
    };

    const endOfRange = addDays(weekStart, DAYS_IN_VIEW - 1);

    return (
        <Box sx={{ mt: 3 }}>
            <Box
                sx={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    mb: 1,
                }}
            >
                <IconButton
                    aria-label="Previous week"
                    onClick={handlePrevious}
                    disabled={!canGoBack}
                    size="small"
                >
                    <ChevronLeftIcon />
                </IconButton>
                <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                    {formatRangeLabel(weekStart, endOfRange)}
                </Typography>
                <IconButton
                    aria-label="Next week"
                    onClick={handleNext}
                    size="small"
                >
                    <ChevronRightIcon />
                </IconButton>
            </Box>

            {totalSlotsInView === 0 ? (
                <Box
                    sx={{
                        textAlign: "center",
                        py: 3,
                        color: "text.secondary",
                        border: "1px dashed",
                        borderColor: "divider",
                        borderRadius: 1,
                    }}
                    data-testid="week-slot-picker-empty"
                >
                    <Typography variant="body2">No available slots in this range</Typography>
                </Box>
            ) : (
                <Box
                    sx={{
                        display: "grid",
                        gridTemplateColumns: `repeat(${DAYS_IN_VIEW}, minmax(120px, 1fr))`,
                        gap: 1,
                        overflowX: "auto",
                        pb: 1,
                    }}
                    data-testid="week-slot-picker-grid"
                >
                    {days.map((day) => (
                        <Box
                            key={day.iso}
                            sx={{
                                border: "1px solid",
                                borderColor: day.isToday ? "primary.main" : "divider",
                                borderRadius: 1,
                                p: 1,
                                minWidth: 120,
                                backgroundColor: day.isToday ? "action.hover" : "background.paper",
                            }}
                            data-testid={`day-column-${day.iso}`}
                        >
                            <Box sx={{ textAlign: "center", mb: 1 }}>
                                <Typography
                                    variant="caption"
                                    sx={{
                                        display: "block",
                                        fontWeight: day.isToday ? 700 : 500,
                                        textTransform: "uppercase",
                                        color: day.isToday ? "primary.main" : "text.secondary",
                                    }}
                                >
                                    {formatWeekday(day.date)}
                                </Typography>
                                <Typography
                                    variant="body2"
                                    sx={{ fontWeight: day.isToday ? 700 : 500 }}
                                >
                                    {formatDayMonth(day.date)}
                                </Typography>
                            </Box>

                            {day.totalSlots === 0 ? (
                                <Typography
                                    variant="caption"
                                    sx={{ display: "block", color: "text.disabled", textAlign: "center", py: 1 }}
                                >
                                    No slots
                                </Typography>
                            ) : (
                                TIME_BUCKETS.filter((bucket) => day.buckets[bucket.id].length > 0).map((bucket) => (
                                    <Box key={bucket.id} sx={{ mb: 1 }}>
                                        <Typography
                                            variant="caption"
                                            sx={{
                                                display: "block",
                                                color: "text.secondary",
                                                mb: 0.5,
                                                textAlign: "center",
                                            }}
                                        >
                                            {bucket.label}
                                        </Typography>
                                        <Box
                                            sx={{
                                                display: "flex",
                                                flexWrap: "wrap",
                                                gap: 0.5,
                                                justifyContent: "center",
                                            }}
                                        >
                                            {day.buckets[bucket.id].map((appointment) => {
                                                const display = formatTimeHm(appointment.time);
                                                return (
                                                    <Chip
                                                        key={appointment.appointmentId}
                                                        label={display}
                                                        variant="outlined"
                                                        color="primary"
                                                        clickable
                                                        size="small"
                                                        onClick={() => onSelect?.(appointment)}
                                                        role="button"
                                                        aria-label={`Book ${display} on ${appointment.date}`}
                                                    />
                                                );
                                            })}
                                        </Box>
                                    </Box>
                                ))
                            )}
                        </Box>
                    ))}
                </Box>
            )}
        </Box>
    );
};

export default WeekSlotPicker;
