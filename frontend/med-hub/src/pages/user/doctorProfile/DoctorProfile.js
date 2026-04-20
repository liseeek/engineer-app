import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import AuthenticatedLayout from "../../../layouts/AuthenticatedLayout";
import {
    Avatar,
    Box,
    Button,
    Card,
    CardActionArea,
    CardContent,
    Chip,
    CircularProgress,
    Divider,
    Paper,
    Stack,
    Typography,
} from "@mui/material";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import CalendarMonthIcon from "@mui/icons-material/CalendarMonth";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { request } from "../../../helpers/axiosHelper";
import { toast, ToastContainer } from "react-toastify";

const DoctorProfile = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [doctor, setDoctor] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        request("get", `/v1/doctors/${id}`)
            .then((r) => setDoctor(r.data))
            .catch(() => toast.error("Failed to load doctor profile."))
            .finally(() => setLoading(false));
    }, [id]);

    const handleBook = () => {
        const firstLocation = doctor?.locations?.[0];
        navigate("/booking", {
            state: {
                prefillCity: firstLocation?.city || "",
                prefillSpecializationId: doctor?.specializations?.[0]?.specializationId || null,
                prefillSpecializationName: doctor?.specializations?.[0]?.specializationName || "",
                prefillDoctorId: doctor.doctorId,
            },
        });
    };

    if (loading) {
        return (
            <AuthenticatedLayout>
                <Box display="flex" justifyContent="center" mt={10}>
                    <CircularProgress />
                </Box>
            </AuthenticatedLayout>
        );
    }

    if (!doctor) {
        return (
            <AuthenticatedLayout>
                <Box textAlign="center" mt={8}>
                    <Typography color="text.secondary" variant="h6">
                        Doctor not found.
                    </Typography>
                </Box>
            </AuthenticatedLayout>
        );
    }

    return (
        <AuthenticatedLayout>
            <ToastContainer position="top-right" autoClose={3000} />
            <Paper
                elevation={2}
                sx={{ maxWidth: 800, mx: "auto", mt: 3, p: { xs: 2, sm: 4 }, borderRadius: 3 }}
            >
                <Button
                    startIcon={<ArrowBackIcon />}
                    onClick={() => navigate(-1)}
                    sx={{ mb: 2 }}
                    variant="text"
                >
                    Back
                </Button>

                {/* Header */}
                <Box display="flex" alignItems="center" gap={3} mb={3}>
                    <Avatar
                        src={doctor.avatarUrl || undefined}
                        alt={`${doctor.name} ${doctor.surname}`}
                        sx={{ width: 80, height: 80, fontSize: 32 }}
                    >
                        {doctor.name?.[0]}
                    </Avatar>
                    <Box>
                        <Typography variant="h4" fontWeight={700}>
                            {doctor.name} {doctor.surname}
                        </Typography>
                        <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap mt={1}>
                            {(doctor.specializations || []).map((s) => (
                                <Chip
                                    key={s.specializationId}
                                    label={s.specializationName}
                                    color="primary"
                                    size="small"
                                />
                            ))}
                        </Stack>
                    </Box>
                </Box>

                <Button
                    variant="contained"
                    startIcon={<CalendarMonthIcon />}
                    onClick={handleBook}
                    sx={{ mb: 3 }}
                >
                    Book appointment
                </Button>

                <Divider sx={{ mb: 3 }} />

                {/* Bio */}
                {doctor.bio && (
                    <>
                        <Typography variant="h6" fontWeight={600} mb={1}>
                            About
                        </Typography>
                        <Typography variant="body1" color="text.secondary" mb={3} sx={{ whiteSpace: "pre-line" }}>
                            {doctor.bio}
                        </Typography>
                        <Divider sx={{ mb: 3 }} />
                    </>
                )}

                {/* Locations */}
                {(doctor.locations || []).length > 0 && (
                    <>
                        <Typography variant="h6" fontWeight={600} mb={2}>
                            Facilities
                        </Typography>
                        <Stack spacing={1.5}>
                            {doctor.locations.map((loc) => (
                                <Card
                                    key={loc.locationId}
                                    elevation={1}
                                    sx={{ borderRadius: 2 }}
                                >
                                    <CardActionArea onClick={() => navigate(`/locations/${loc.locationId}`)}>
                                        <CardContent>
                                            <Box display="flex" alignItems="center" gap={1}>
                                                <LocationOnIcon color="action" />
                                                <Box>
                                                    <Typography fontWeight={600}>{loc.locationName}</Typography>
                                                    <Typography variant="body2" color="text.secondary">
                                                        {loc.address}, {loc.city}
                                                    </Typography>
                                                </Box>
                                            </Box>
                                        </CardContent>
                                    </CardActionArea>
                                </Card>
                            ))}
                        </Stack>
                    </>
                )}
            </Paper>
        </AuthenticatedLayout>
    );
};

export default DoctorProfile;
