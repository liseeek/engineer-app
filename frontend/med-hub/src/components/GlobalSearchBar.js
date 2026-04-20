import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    Autocomplete,
    Box,
    CircularProgress,
    ClickAwayListener,
    IconButton,
    TextField,
    Typography,
    useMediaQuery,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import LocalHospitalIcon from "@mui/icons-material/LocalHospital";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import { request, unwrapPage } from "../helpers/axiosHelper";
import styles from "./GlobalSearchBar.module.css";

const MIN_QUERY_LENGTH = 2;
const DEBOUNCE_MS = 250;

const normalize = (value) => (value || "").trim().toLowerCase();

function GlobalSearchBar({ locations = [] }) {
    const navigate = useNavigate();
    const isMobile = useMediaQuery("(max-width: 768px)");

    const [query, setQuery] = useState("");
    const [loadingDoctors, setLoadingDoctors] = useState(false);
    const [doctorResults, setDoctorResults] = useState([]);
    const [focused, setFocused] = useState(false);
    const [mobileExpanded, setMobileExpanded] = useState(false);

    const trimmedQuery = query.trim();
    const hasMinQuery = trimmedQuery.length >= MIN_QUERY_LENGTH;

    useEffect(() => {
        if (!hasMinQuery) {
            setDoctorResults([]);
            setLoadingDoctors(false);
            return;
        }

        let active = true;
        setLoadingDoctors(true);
        const timeout = setTimeout(async () => {
            try {
                const response = await request(
                    "get",
                    `/v1/doctors/search?q=${encodeURIComponent(trimmedQuery)}&size=5`
                );
                if (!active) return;
                setDoctorResults(unwrapPage(response.data));
            } catch {
                if (active) setDoctorResults([]);
            } finally {
                if (active) setLoadingDoctors(false);
            }
        }, DEBOUNCE_MS);

        return () => {
            active = false;
            clearTimeout(timeout);
        };
    }, [hasMinQuery, trimmedQuery]);

    const doctorOptions = useMemo(
        () =>
            doctorResults.map((doctor) => ({
                key: `doctor-${doctor.doctorId}`,
                type: "doctor",
                group: "Doctors",
                label: `${doctor.name} ${doctor.surname}`,
                doctorId: doctor.doctorId,
                subtitle:
                    (doctor.specializations || [])
                        .map((spec) => spec.specializationName)
                        .join(", ") || "Doctor profile",
            })),
        [doctorResults]
    );

    const locationOptions = useMemo(() => {
        if (!hasMinQuery) return [];
        const q = normalize(trimmedQuery);
        return locations
            .filter((location) => {
                const fields = [
                    location.locationName,
                    location.city,
                    location.address,
                    location.country,
                ];
                return fields.some((value) => normalize(value).includes(q));
            })
            .slice(0, 5)
            .map((location) => ({
                key: `location-${location.locationId}`,
                type: "location",
                group: "Facilities",
                label: location.locationName || location.city || "Facility",
                locationId: location.locationId,
                subtitle: [location.city, location.address].filter(Boolean).join(", "),
            }));
    }, [hasMinQuery, locations, trimmedQuery]);

    const allOptions = useMemo(
        () => [...doctorOptions, ...locationOptions],
        [doctorOptions, locationOptions]
    );

    const closeSearch = useCallback(() => {
        setFocused(false);
        if (isMobile) setMobileExpanded(false);
    }, [isMobile]);

    const handleOptionSelect = useCallback(
        (option) => {
            if (!option) return;

            if (option.type === "doctor") {
                navigate(`/doctors/${option.doctorId}`);
            } else if (option.type === "location") {
                navigate(`/locations/${option.locationId}`);
            }

            closeSearch();
        },
        [navigate, closeSearch]
    );

    const submitQuery = useCallback(() => {
        if (!trimmedQuery || allOptions.length === 0) return;
        handleOptionSelect(allOptions[0]);
    }, [allOptions, handleOptionSelect, trimmedQuery]);

    if (isMobile && !mobileExpanded) {
        return (
            <div className={styles.mobileToggleWrapper}>
                <IconButton
                    aria-label="Open global search"
                    onClick={() => setMobileExpanded(true)}
                    size="large"
                >
                    <SearchIcon />
                </IconButton>
            </div>
        );
    }

    return (
        <ClickAwayListener onClickAway={closeSearch}>
            <div className={styles.container}>
                <Autocomplete
                    className={styles.autocomplete}
                    options={allOptions}
                    freeSolo
                    filterOptions={(options) => options}
                    groupBy={(option) => option.group}
                    getOptionLabel={(option) =>
                        typeof option === "string" ? option : option.label
                    }
                    inputValue={query}
                    onInputChange={(_, value, reason) => {
                        if (reason === "input" || reason === "clear") {
                            setQuery(value);
                        }
                    }}
                    onChange={(_, value) => {
                        if (typeof value === "string") {
                            setQuery(value);
                            return;
                        }
                        handleOptionSelect(value);
                    }}
                    loading={loadingDoctors}
                    open={focused && hasMinQuery && allOptions.length > 0}
                    onOpen={() => setFocused(true)}
                    onClose={() => setFocused(false)}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            size="small"
                            placeholder="Search doctors and facilities..."
                            onFocus={() => setFocused(true)}
                            onKeyDown={(event) => {
                                if (event.key === "Enter") {
                                    event.preventDefault();
                                    submitQuery();
                                }
                                if (event.key === "Escape") {
                                    closeSearch();
                                }
                            }}
                            InputProps={{
                                ...params.InputProps,
                                endAdornment: (
                                    <>
                                        {loadingDoctors ? (
                                            <CircularProgress color="inherit" size={16} />
                                        ) : null}
                                        <IconButton
                                            size="small"
                                            aria-label="Run search"
                                            onMouseDown={(event) => event.preventDefault()}
                                            onClick={submitQuery}
                                        >
                                            <SearchIcon fontSize="small" />
                                        </IconButton>
                                        {params.InputProps.endAdornment}
                                    </>
                                ),
                            }}
                        />
                    )}
                    renderGroup={(params) => (
                        <li key={params.key}>
                            <div className={styles.groupLabel}>{params.group}</div>
                            <ul className={styles.groupList}>{params.children}</ul>
                        </li>
                    )}
                    renderOption={(props, option) => (
                        <li {...props} key={option.key}>
                            <Box className={styles.optionRow}>
                                {option.type === "doctor" && (
                                    <LocalHospitalIcon fontSize="small" color="action" />
                                )}
                                {option.type === "location" && (
                                    <LocationOnIcon fontSize="small" color="action" />
                                )}
                                <Box>
                                    <Typography className={styles.optionTitle}>
                                        {option.label}
                                    </Typography>
                                    {option.subtitle ? (
                                        <Typography className={styles.optionSubtitle}>
                                            {option.subtitle}
                                        </Typography>
                                    ) : null}
                                </Box>
                            </Box>
                        </li>
                    )}
                />
            </div>
        </ClickAwayListener>
    );
}

export default GlobalSearchBar;
