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
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
    Paper,
    Stack,
    TextField,
    Typography,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import EditIcon from "@mui/icons-material/Edit";
import PhoneIcon from "@mui/icons-material/Phone";
import EmailIcon from "@mui/icons-material/Email";
import { request } from "../../../helpers/axiosHelper";
import { useAuth } from "../../../context/AuthContext";
import { ROLES } from "../../../helpers/roles";
import { toast, ToastContainer } from "react-toastify";

const LocationProfile = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { role } = useAuth();

    const [location, setLocation] = useState(null);
    const [doctors, setDoctors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editOpen, setEditOpen] = useState(false);
    const [editForm, setEditForm] = useState({
        description: "",
        yearEstablished: "",
        phoneNumber: "",
        email: "",
    });
    const [saving, setSaving] = useState(false);
    const [workerLocationId, setWorkerLocationId] = useState(null);

    useEffect(() => {
        const loadData = async () => {
            try {
                const [locRes, docRes] = await Promise.all([
                    request("get", `/v1/locations/${id}`),
                    request("get", `/v1/doctors?size=200`),
                ]);
                const locData = locRes.data;
                setLocation(locData);
                setEditForm({
                    description: locData.description || "",
                    yearEstablished: locData.yearEstablished ?? "",
                    phoneNumber: locData.phoneNumber || "",
                    email: locData.email || "",
                });

                const allDoctors = docRes.data?.content || docRes.data || [];
                const here = allDoctors.filter(
                    (d) => d.locations?.some((l) => l.locationId === locData.locationId)
                );
                setDoctors(here);
            } catch {
                toast.error("Failed to load facility profile.");
            } finally {
                setLoading(false);
            }
        };
        loadData();
    }, [id]);

    useEffect(() => {
        if (role !== ROLES.WORKER) return;
        request("get", "/v1/workers/currentWorker/location")
            .then((r) => setWorkerLocationId(r.data?.locationId ?? null))
            .catch(() => {});
    }, [role]);

    const isWorkerHere =
        role === ROLES.WORKER &&
        workerLocationId != null &&
        workerLocationId === location?.locationId;
    const canEdit = isWorkerHere || role === ROLES.ADMIN;

    const handleSave = async () => {
        setSaving(true);
        try {
            const payload = {};
            if (editForm.description !== "") payload.description = editForm.description;
            if (editForm.yearEstablished !== "") {
                payload.yearEstablished = parseInt(editForm.yearEstablished, 10);
            }
            if (editForm.phoneNumber !== "") payload.phoneNumber = editForm.phoneNumber;
            if (editForm.email !== "") payload.email = editForm.email;

            const res = await request("patch", `/v1/locations/${id}`, payload);
            setLocation(res.data);
            toast.success("Facility updated successfully.");
            setEditOpen(false);
        } catch (err) {
            const msg = err?.response?.data?.message || "Failed to update facility.";
            toast.error(msg);
        } finally {
            setSaving(false);
        }
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

    if (!location) {
        return (
            <AuthenticatedLayout>
                <Box textAlign="center" mt={8}>
                    <Typography color="text.secondary" variant="h6">
                        Facility not found.
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
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Box>
                        <Typography variant="h4" fontWeight={700}>
                            {location.locationName}
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                            {location.address}, {location.city}
                            {location.country ? `, ${location.country}` : ""}
                        </Typography>
                    </Box>
                    {canEdit && (
                        <Button
                            startIcon={<EditIcon />}
                            variant="outlined"
                            onClick={() => setEditOpen(true)}
                        >
                            Edit facility
                        </Button>
                    )}
                </Box>

                {/* Meta: year + contact */}
                <Stack direction="row" spacing={3} flexWrap="wrap" useFlexGap mb={2}>
                    {location.yearEstablished && (
                        <Typography variant="body2" color="text.secondary">
                            Est. {location.yearEstablished}
                        </Typography>
                    )}
                    {location.phoneNumber && (
                        <Stack direction="row" spacing={0.5} alignItems="center">
                            <PhoneIcon fontSize="small" color="action" />
                            <Typography variant="body2">{location.phoneNumber}</Typography>
                        </Stack>
                    )}
                    {location.email && (
                        <Stack direction="row" spacing={0.5} alignItems="center">
                            <EmailIcon fontSize="small" color="action" />
                            <Typography variant="body2">{location.email}</Typography>
                        </Stack>
                    )}
                </Stack>

                {/* Description */}
                {location.description && (
                    <>
                        <Divider sx={{ mb: 2 }} />
                        <Typography variant="body1" color="text.secondary" sx={{ whiteSpace: "pre-line", mb: 2 }}>
                            {location.description}
                        </Typography>
                    </>
                )}

                <Divider sx={{ mb: 3 }} />

                {/* Doctors */}
                <Typography variant="h6" fontWeight={600} mb={2}>
                    Doctors at this facility
                </Typography>
                {doctors.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">
                        No doctors listed for this facility.
                    </Typography>
                ) : (
                    <Stack spacing={1.5}>
                        {doctors.map((doc) => (
                            <Card key={doc.doctorId} elevation={1} sx={{ borderRadius: 2 }}>
                                <CardActionArea onClick={() => navigate(`/doctors/${doc.doctorId}`)}>
                                    <CardContent>
                                        <Box display="flex" alignItems="center" gap={2}>
                                            <Avatar
                                                src={doc.avatarUrl || undefined}
                                                alt={`${doc.name} ${doc.surname}`}
                                                sx={{ width: 44, height: 44 }}
                                            >
                                                {doc.name?.[0]}
                                            </Avatar>
                                            <Box>
                                                <Typography fontWeight={600}>
                                                    {doc.name} {doc.surname}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    {(doc.specializations || [])
                                                        .map((s) => s.specializationName)
                                                        .join(", ")}
                                                </Typography>
                                            </Box>
                                        </Box>
                                    </CardContent>
                                </CardActionArea>
                            </Card>
                        ))}
                    </Stack>
                )}
            </Paper>

            {/* Edit Dialog */}
            <Dialog open={editOpen} onClose={() => setEditOpen(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Edit facility information</DialogTitle>
                <DialogContent>
                    <Stack spacing={2} mt={1}>
                        <TextField
                            label="Description"
                            multiline
                            rows={4}
                            value={editForm.description}
                            onChange={(e) => setEditForm((f) => ({ ...f, description: e.target.value }))}
                            inputProps={{ maxLength: 2000 }}
                            fullWidth
                        />
                        <TextField
                            label="Year established"
                            type="number"
                            value={editForm.yearEstablished}
                            onChange={(e) => setEditForm((f) => ({ ...f, yearEstablished: e.target.value }))}
                            inputProps={{ min: 1800, max: new Date().getFullYear() }}
                            fullWidth
                        />
                        <TextField
                            label="Phone number"
                            value={editForm.phoneNumber}
                            onChange={(e) => setEditForm((f) => ({ ...f, phoneNumber: e.target.value }))}
                            inputProps={{ maxLength: 20 }}
                            fullWidth
                        />
                        <TextField
                            label="Email"
                            type="email"
                            value={editForm.email}
                            onChange={(e) => setEditForm((f) => ({ ...f, email: e.target.value }))}
                            inputProps={{ maxLength: 100 }}
                            fullWidth
                        />
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setEditOpen(false)}>Cancel</Button>
                    <Button
                        variant="contained"
                        onClick={handleSave}
                        disabled={saving}
                    >
                        {saving ? "Saving…" : "Save"}
                    </Button>
                </DialogActions>
            </Dialog>
        </AuthenticatedLayout>
    );
};

export default LocationProfile;
