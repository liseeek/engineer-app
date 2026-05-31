import React, { useEffect, useState } from "react";
import AuthenticatedLayout from "../../../layouts/AuthenticatedLayout";
import {
    Avatar,
    Box,
    Button,
    CircularProgress,
    Paper,
    Stack,
    TextField,
    Typography,
} from "@mui/material";
import SaveIcon from "@mui/icons-material/Save";
import { request } from "../../../helpers/axiosHelper";
import { toast, ToastContainer } from "react-toastify";

const DoctorOwnProfile = () => {
    const [profile, setProfile] = useState(null);
    const [bio, setBio] = useState("");
    const [avatarUrl, setAvatarUrl] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        request("get", "/v1/doctor/me/profile")
            .then((r) => {
                setProfile(r.data);
                setBio(r.data.bio || "");
                setAvatarUrl(r.data.avatarUrl || "");
            })
            .catch(() => toast.error("Failed to load profile."))
            .finally(() => setLoading(false));
    }, []);

    const handleSave = async () => {
        setSaving(true);
        try {
            const payload = {};
            if (bio !== (profile?.bio || "")) payload.bio = bio;
            if (avatarUrl !== (profile?.avatarUrl || "")) payload.avatarUrl = avatarUrl;

            const res = await request("patch", "/v1/doctor/me/profile", payload);
            setProfile(res.data);
            setBio(res.data.bio || "");
            setAvatarUrl(res.data.avatarUrl || "");
            toast.success("Profile updated.");
        } catch (err) {
            const msg = err?.response?.data?.message || "Failed to save profile.";
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

    return (
        <AuthenticatedLayout>
            <ToastContainer position="top-right" autoClose={3000} />
            <Paper
                elevation={2}
                sx={{ maxWidth: 640, mx: "auto", mt: 3, p: { xs: 2, sm: 4 }, borderRadius: 3 }}
            >
                <Typography variant="h4" fontWeight={700} mb={3} textAlign="center">
                    My profile
                </Typography>

                {/* Preview */}
                <Box display="flex" alignItems="center" gap={2} mb={3}>
                    <Avatar
                        src={avatarUrl || undefined}
                        alt={`${profile?.name} ${profile?.surname}`}
                        sx={{ width: 72, height: 72, fontSize: 28 }}
                    >
                        {profile?.name?.[0]}
                    </Avatar>
                    <Box>
                        <Typography variant="h6" fontWeight={600}>
                            {profile?.name} {profile?.surname}
                        </Typography>
                    </Box>
                </Box>

                <Stack spacing={3}>
                    <TextField
                        label="Avatar URL"
                        value={avatarUrl}
                        onChange={(e) => setAvatarUrl(e.target.value)}
                        placeholder="https://example.com/photo.jpg"
                        helperText="Enter a direct image URL for your profile photo."
                        inputProps={{ maxLength: 255 }}
                        fullWidth
                    />

                    <TextField
                        label="About / Bio"
                        multiline
                        rows={6}
                        value={bio}
                        onChange={(e) => setBio(e.target.value)}
                        placeholder="Tell patients about your experience, specializations, approach..."
                        inputProps={{ maxLength: 2000 }}
                        helperText={`${bio.length} / 2000`}
                        fullWidth
                    />

                    <Box>
                        <Button
                            variant="contained"
                            startIcon={<SaveIcon />}
                            onClick={handleSave}
                            disabled={saving}
                        >
                            {saving ? "Saving…" : "Save changes"}
                        </Button>
                    </Box>
                </Stack>
            </Paper>
        </AuthenticatedLayout>
    );
};

export default DoctorOwnProfile;
