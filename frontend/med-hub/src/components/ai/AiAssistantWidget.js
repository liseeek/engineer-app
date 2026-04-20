import React, { useState, useCallback, useEffect } from 'react';
import {
    Fab, Paper, IconButton, Typography, Box, Slide,
    Select, MenuItem, FormControl, InputLabel, RadioGroup,
    FormControlLabel, Radio, Checkbox, FormGroup, TextField,
    Button, Chip, CircularProgress, Divider, Grow, Autocomplete,
} from '@mui/material';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import CloseIcon from '@mui/icons-material/Close';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from 'react-router-dom';
import { request } from '../../helpers/axiosHelper';
import { toast } from 'react-toastify';

const SYMPTOMS = [
    'Headache', 'Chest pain', 'Fatigue', 'Fever', 'Cough',
    'Shortness of breath', 'Back pain', 'Joint pain', 'Skin rash',
    'Abdominal pain', 'Nausea', 'Dizziness', 'Vision problems',
    'Hearing problems', 'Anxiety', 'Insomnia', 'Weight changes',
    'Frequent urination', 'Numbness', 'Swelling'
];

const AGE_RANGES = [
    { value: 'UNDER_18', label: 'Under 18' },
    { value: 'AGE_18_30', label: '18–30' },
    { value: 'AGE_31_50', label: '31–50' },
    { value: 'AGE_51_70', label: '51–70' },
    { value: 'OVER_70', label: 'Over 70' },
];

const CONFIDENCE_COLORS = { HIGH: 'success', MEDIUM: 'warning', LOW: 'default' };

export default function AiAssistantWidget() {
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);
    const [step, setStep] = useState(0);
    const [loading, setLoading] = useState(false);
    const [cooldown, setCooldown] = useState(false);
    const [pulseBadge, setPulseBadge] = useState(true);

    const [ageRange, setAgeRange] = useState('');
    const [gender, setGender] = useState('');
    const [city, setCity] = useState('');
    const [cities, setCities] = useState([]);
    const [selectedSymptoms, setSelectedSymptoms] = useState([]);
    const [additionalDescription, setAdditionalDescription] = useState('');
    const [results, setResults] = useState(null);

    useEffect(() => {
        const fetchCities = async () => {
            try {
                const response = await request('get', '/v1/locations/cities/distinct');
                setCities(response.data);
            } catch {
                toast.error('Failed to load cities. Please try again.');
            }
        };
        fetchCities();
    }, []);

    const resetForm = useCallback(() => {
        setStep(0);
        setAgeRange('');
        setGender('');
        setCity('');
        setSelectedSymptoms([]);
        setAdditionalDescription('');
        setResults(null);
    }, []);

    const handleToggle = () => {
        setOpen(prev => !prev);
        setPulseBadge(false);
    };

    const handleClose = () => {
        setOpen(false);
        resetForm();
    };

    const handleSymptomToggle = (symptom) => {
        setSelectedSymptoms(prev =>
            prev.includes(symptom) ? prev.filter(s => s !== symptom) : [...prev, symptom]
        );
    };

    const handleAnalyze = async () => {
        setLoading(true);
        setCooldown(true);
        setTimeout(() => setCooldown(false), 10000);

        try {
            const response = await request('POST', '/v1/symptom-checker', {
                ageRange, gender, symptoms: selectedSymptoms, additionalDescription
            });
            setResults(response.data);
            setStep(2);
        } catch (err) {
            const msg = err.response?.data?.message || 'AI assistant is temporarily unavailable. Please try again later.';
            toast.error(msg);
        } finally {
            setLoading(false);
        }
    };

    const canProceedStep0 = ageRange && gender && city;
    const canProceedStep1 = selectedSymptoms.length > 0;

    return (
        <>
            <Grow in>
                <Fab
                    color="primary"
                    onClick={handleToggle}
                    sx={{
                        position: 'fixed', bottom: 56, right: 24, zIndex: 1300,
                        ...(pulseBadge && {
                            '&::after': {
                                content: '""', position: 'absolute', top: -4, right: -4,
                                width: 14, height: 14, borderRadius: '50%',
                                bgcolor: 'error.main', border: '2px solid white',
                                animation: 'pulse 1.5s infinite',
                            },
                            '@keyframes pulse': {
                                '0%': { transform: 'scale(1)', opacity: 1 },
                                '50%': { transform: 'scale(1.3)', opacity: 0.7 },
                                '100%': { transform: 'scale(1)', opacity: 1 },
                            },
                        }),
                    }}
                    aria-label="AI Assistant"
                >
                    <AutoAwesomeIcon />
                </Fab>
            </Grow>

            <Slide direction="up" in={open} mountOnEnter unmountOnExit>
                <Paper
                    elevation={8}
                    sx={{
                        position: 'fixed', bottom: 122, right: 24, zIndex: 1300,
                        width: 380, maxHeight: '70vh', display: 'flex', flexDirection: 'column',
                        borderRadius: 3, overflow: 'hidden',
                    }}
                >
                    <Box sx={{ bgcolor: 'primary.main', color: 'white', px: 2, py: 1.5, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <AutoAwesomeIcon fontSize="small" />
                            <Typography variant="subtitle1" fontWeight={600}>Find the right specialist</Typography>
                        </Box>
                        <IconButton size="small" onClick={handleClose} sx={{ color: 'white' }}>
                            <CloseIcon fontSize="small" />
                        </IconButton>
                    </Box>

                    <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
                        {step === 0 && (
                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                <Typography variant="body2" color="text.secondary">
                                    Tell us about yourself so we can suggest the right specialist.
                                </Typography>
                                <FormControl fullWidth size="small">
                                    <InputLabel>Age range</InputLabel>
                                    <Select value={ageRange} label="Age range" onChange={e => setAgeRange(e.target.value)}>
                                        {AGE_RANGES.map(a => <MenuItem key={a.value} value={a.value}>{a.label}</MenuItem>)}
                                    </Select>
                                </FormControl>
                                <FormControl>
                                    <Typography variant="body2" sx={{ mb: 0.5 }}>Gender</Typography>
                                    <RadioGroup row value={gender} onChange={e => setGender(e.target.value)}>
                                        <FormControlLabel value="MALE" control={<Radio size="small" />} label="Male" />
                                        <FormControlLabel value="FEMALE" control={<Radio size="small" />} label="Female" />
                                        <FormControlLabel value="OTHER" control={<Radio size="small" />} label="Other" />
                                    </RadioGroup>
                                </FormControl>
                                <Autocomplete
                                    options={cities}
                                    value={city}
                                    onChange={(_, newValue) => setCity(newValue || '')}
                                    size="small"
                                    renderInput={(params) => (
                                        <TextField {...params} label="Preferred city" fullWidth />
                                    )}
                                />
                                <Button variant="contained" disabled={!canProceedStep0} onClick={() => setStep(1)} fullWidth>
                                    Next
                                </Button>
                            </Box>
                        )}

                        {step === 1 && (
                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                <Typography variant="body2" color="text.secondary">
                                    What symptoms are you experiencing?
                                </Typography>
                                <FormGroup sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 0 }}>
                                    {SYMPTOMS.map(s => (
                                        <FormControlLabel
                                            key={s}
                                            control={<Checkbox size="small" checked={selectedSymptoms.includes(s)} onChange={() => handleSymptomToggle(s)} />}
                                            label={<Typography variant="body2">{s}</Typography>}
                                        />
                                    ))}
                                </FormGroup>
                                <TextField
                                    multiline rows={2} size="small" fullWidth
                                    label="Anything else? (optional)"
                                    value={additionalDescription}
                                    onChange={e => setAdditionalDescription(e.target.value)}
                                    inputProps={{ maxLength: 500 }}
                                />
                                <Box sx={{ display: 'flex', gap: 1 }}>
                                    <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => setStep(0)}>
                                        Back
                                    </Button>
                                    <Button
                                        variant="contained" fullWidth
                                        disabled={!canProceedStep1 || loading || cooldown}
                                        onClick={handleAnalyze}
                                    >
                                        {loading ? <CircularProgress size={22} color="inherit" /> : 'Analyze'}
                                    </Button>
                                </Box>
                            </Box>
                        )}

                        {step === 2 && results && (
                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                <Typography variant="body2" color="text.secondary">
                                    Based on your symptoms, we recommend:
                                </Typography>
                                {results.recommendations.map((rec, i) => (
                                    <Paper key={i} variant="outlined" sx={{ p: 1.5 }}>
                                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 0.5 }}>
                                            <Typography variant="subtitle2">{rec.specializationName}</Typography>
                                            <Chip label={rec.confidence} size="small" color={CONFIDENCE_COLORS[rec.confidence] || 'default'} />
                                        </Box>
                                        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                                            {rec.reasoning}
                                        </Typography>
                                        <Button
                                            size="small"
                                            variant="outlined"
                                            onClick={() => {
                                                handleClose();
                                                navigate('/booking', {
                                                    state: {
                                                        prefillCity: city,
                                                        prefillSpecializationId: rec.specializationId,
                                                        prefillSpecializationName: rec.specializationName,
                                                    },
                                                });
                                            }}
                                        >
                                            Book appointment
                                        </Button>
                                    </Paper>
                                ))}
                                {results.recommendations.length === 0 && (
                                    <Typography variant="body2">
                                        We could not determine a specific specialization. Please consult your family doctor.
                                    </Typography>
                                )}
                                <Divider />
                                <Button variant="text" size="small" onClick={resetForm}>Start over</Button>
                            </Box>
                        )}
                    </Box>

                    <Box
                        sx={{
                            px: 2,
                            py: 1,
                            bgcolor: 'grey.50',
                            borderTop: '1px solid',
                            borderColor: 'divider',
                            textAlign: 'center',
                        }}
                    >
                        <Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.4 }}>
                            This tool provides general guidance only and is not a substitute for professional medical advice.
                        </Typography>
                    </Box>
                </Paper>
            </Slide>
        </>
    );
}
