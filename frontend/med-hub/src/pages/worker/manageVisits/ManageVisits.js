import React, { useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';

import { request, unwrapPage } from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import CancelIcon from '@mui/icons-material/Close';
import DoneIcon from '@mui/icons-material/Done';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import { DataGrid, GridActionsCellItem, } from '@mui/x-data-grid';
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button } from '@mui/material';
import { toast, ToastContainer } from "react-toastify";
import RescheduleDialog from "../../../components/RescheduleDialog";
import { toDateTimeKey } from "../../../helpers/dateTimeSort";

const ManageVisits = () => {
    const [rows, setRows] = useState([]);
    const [openCancelDialog, setOpenCancelDialog] = useState(false);
    const [appointmentToCancel, setAppointmentToCancel] = useState(null);
    const [cancelReason, setCancelReason] = useState('');
    const [openRescheduleDialog, setOpenRescheduleDialog] = useState(false);
    const [appointmentToReschedule, setAppointmentToReschedule] = useState(null);

    useEffect(() => {
        // #region agent log
        fetch('http://127.0.0.1:7659/ingest/b51b0a01-b793-442c-a3aa-1b3ff4899381',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'3d2a8b'},body:JSON.stringify({sessionId:'3d2a8b',runId:'run-worker-visits-1',hypothesisId:'H0',location:'ManageVisits.js:component:mounted',message:'ManageVisits component mounted',data:{},timestamp:Date.now()})}).catch(()=>{});
        // #endregion
        const handler = (event) => {
            // #region agent log
            fetch('http://127.0.0.1:7659/ingest/b51b0a01-b793-442c-a3aa-1b3ff4899381',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'3d2a8b'},body:JSON.stringify({sessionId:'3d2a8b',runId:'run-worker-visits-1',hypothesisId:'H4',location:'ManageVisits.js:window:error',message:'Runtime error on worker visits page',data:{message:event?.message,filename:event?.filename,lineno:event?.lineno,colno:event?.colno},timestamp:Date.now()})}).catch(()=>{});
            // #endregion
        };
        window.addEventListener("error", handler);
        return () => window.removeEventListener("error", handler);
    }, []);

    const fetchAppointments = async () => {
        try {
            // #region agent log
            fetch('http://127.0.0.1:7659/ingest/b51b0a01-b793-442c-a3aa-1b3ff4899381',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'3d2a8b'},body:JSON.stringify({sessionId:'3d2a8b',runId:'run-worker-visits-1',hypothesisId:'H1',location:'ManageVisits.js:fetchAppointments:start',message:'Starting worker visits fetch',data:{url:'/v1/workers/currentWorker/appointments?size=200'},timestamp:Date.now()})}).catch(()=>{});
            // #endregion
            const response = await request('get', "/v1/workers/currentWorker/appointments?size=200");
            // #region agent log
            fetch('http://127.0.0.1:7659/ingest/b51b0a01-b793-442c-a3aa-1b3ff4899381',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'3d2a8b'},body:JSON.stringify({sessionId:'3d2a8b',runId:'run-worker-visits-1',hypothesisId:'H1',location:'ManageVisits.js:fetchAppointments:response',message:'Received worker visits response',data:{status:response?.status,isArray:Array.isArray(response?.data),hasContentArray:Array.isArray(response?.data?.content),topLevelKeys:Object.keys(response?.data || {}).slice(0,10)},timestamp:Date.now()})}).catch(()=>{});
            // #endregion
            const rawItems = unwrapPage(response.data);
            // #region agent log
            fetch('http://127.0.0.1:7659/ingest/b51b0a01-b793-442c-a3aa-1b3ff4899381',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'3d2a8b'},body:JSON.stringify({sessionId:'3d2a8b',runId:'run-worker-visits-1',hypothesisId:'H1',location:'ManageVisits.js:fetchAppointments:unwrapped',message:'Unwrapped worker visits payload',data:{count:rawItems.length,sampleKeys:rawItems[0]?Object.keys(rawItems[0]):[]},timestamp:Date.now()})}).catch(()=>{});
            // #endregion

            const transformedData = rawItems.map((appointment) => {
                const visitDateTime = new Date(`${appointment.date}T${appointment.time}`);

                const formattedDate = new Intl.DateTimeFormat("en-GB", {
                    dateStyle: "short",
                    timeStyle: "short",
                    hour12: false,
                }).format(visitDateTime);

                return {
                    id: appointment.appointmentId,
                    doctor: `${appointment.doctor.name} ${appointment.doctor.surname}`,
                    doctorId: appointment.doctor.doctorId,
                    locationId: appointment.location.locationId,
                    facility: appointment.location.locationName,
                    address: appointment.location.address,
                    visitDateTime: formattedDate,
                    rawDate: appointment.date,
                    rawTime: appointment.time,
                    visitDateTimeTs: toDateTimeKey(appointment.date, appointment.time),
                    visitStatus: appointment.appointmentStatus,
                    visitType: appointment.appointmentType,
                    rescheduleReason: appointment.rescheduleReason || "",
                };
            });
            setRows(transformedData);
            // #region agent log
            fetch('http://127.0.0.1:7659/ingest/b51b0a01-b793-442c-a3aa-1b3ff4899381',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'3d2a8b'},body:JSON.stringify({sessionId:'3d2a8b',runId:'run-worker-visits-1',hypothesisId:'H5',location:'ManageVisits.js:fetchAppointments:setRows',message:'Rows prepared for worker DataGrid',data:{rowsCount:transformedData.length,firstRowKeys:transformedData[0]?Object.keys(transformedData[0]):[]},timestamp:Date.now()})}).catch(()=>{});
            // #endregion
        } catch (error) {
            // #region agent log
            fetch('http://127.0.0.1:7659/ingest/b51b0a01-b793-442c-a3aa-1b3ff4899381',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'3d2a8b'},body:JSON.stringify({sessionId:'3d2a8b',runId:'run-worker-visits-1',hypothesisId:'H2',location:'ManageVisits.js:fetchAppointments:catch',message:'Worker visits fetch/mapping failed',data:{status:error?.response?.status,errorMessage:error?.message,responseData:error?.response?.data?JSON.stringify(error.response.data).slice(0,400):null},timestamp:Date.now()})}).catch(()=>{});
            // #endregion
            toast.error("Failed to fetch appointments. Please try again later.");
        }
    };

    const cancelAppointment = async (id, reason) => {
        try {
            await request('post',
                `/v1/facility/appointments/${id}/cancel?reason=${encodeURIComponent(reason)}`);
            toast.success("Appointment canceled successfully!");
            return 200;
        } catch (error) {
            toast.error("Failed to cancel appointment. Please try again later.");
            return error.response?.status || 500;
        }
    };

    const handleCancelClick = (id) => () => {
        setAppointmentToCancel(id);
        setCancelReason('');
        setOpenCancelDialog(true);
    };

    const handleConfirmCancel = async () => {
        if (!cancelReason.trim()) {
            toast.error('Please provide a reason for cancellation');
            return;
        }

        const responseStatus = await cancelAppointment(appointmentToCancel, cancelReason);
        if (responseStatus === 200) {
            setRows((prevRows) =>
                prevRows.map((row) => (row.id === appointmentToCancel ? { ...row, visitStatus: "CANCELED" } : row))
            );
            setOpenCancelDialog(false);
            setAppointmentToCancel(null);
            setCancelReason('');
        }
    };

    const completeAppointment = async (id) => {
        try {
            await request('patch', `/v1/appointments/${id}/complete`);
            toast.success("Appointment completed successfully!");
            return 200;
        } catch (error) {
            toast.error("Failed to complete appointment. Please try again later.");
            return error.response?.status || 500;
        }
    };

    const handleCompleteClick = (id) => async () => {
        const responseStatus = await completeAppointment(id);
        if (responseStatus === 200) {
            setRows((prevRows) =>
                prevRows.map((row) => (row.id === id ? { ...row, visitStatus: "COMPLETED" } : row))
            );
        }
    };

    const canReschedule = (row) => {
        const isAllowedStatus = row.visitStatus === "ACTIVE" || row.visitStatus === "RESCHEDULED";
        const rowDate = new Date(`${row.rawDate}T${row.rawTime}`);
        const now = new Date();
        return isAllowedStatus && rowDate >= now;
    };

    const handleRescheduleClick = (row) => () => {
        setAppointmentToReschedule(row);
        setOpenRescheduleDialog(true);
    };

    const handleConfirmReschedule = async (newSlotId, reason) => {
        await request('post', `/v1/facility/appointments/${appointmentToReschedule.id}/reschedule`, { newSlotId, reason });
        toast.success("Appointment rescheduled successfully!");
        await fetchAppointments();
    };

    useEffect(() => {
        fetchAppointments();
    }, []);

    useEffect(() => {
        // #region agent log
        fetch('http://127.0.0.1:7659/ingest/b51b0a01-b793-442c-a3aa-1b3ff4899381',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'3d2a8b'},body:JSON.stringify({sessionId:'3d2a8b',runId:'run-worker-visits-1',hypothesisId:'H5',location:'ManageVisits.js:rows:effect',message:'Worker rows state changed',data:{rowsCount:rows.length},timestamp:Date.now()})}).catch(()=>{});
        // #endregion
    }, [rows]);

    const columns = [
        { field: "doctor", headerName: "Doctor", width: 180, editable: false },
        { field: "facility", headerName: "Facility", width: 180, editable: false },
        { field: "address", headerName: "Address", width: 180, editable: false },
        {
            field: "visitDateTimeTs",
            headerName: "Visit Date & Time",
            width: 150,
            editable: false,
            valueGetter: (_value, row) => row?.visitDateTimeTs ?? Number.POSITIVE_INFINITY,
            sortComparator: (value1, value2) => value1 - value2,
            renderCell: (params) => params?.row?.visitDateTime ?? "",
        },
        { field: "visitStatus", headerName: "Visit Status", width: 130, editable: false },
        { field: "visitType", headerName: "Visit Type", width: 90, editable: false },
        { field: "rescheduleReason", headerName: "Reschedule Reason", width: 220, editable: false },
        {
            field: "doneAction",
            headerName: "Complete",
            width: 100,
            type: "actions",
            getActions: ({ id }) => [
                <GridActionsCellItem
                    icon={<DoneIcon />}
                    label="Done"
                    className="textPrimary"
                    onClick={handleCompleteClick(id)}
                    color="inherit"
                />,
            ],
        },
        {
            field: "cancelAction",
            headerName: "Cancel",
            width: 100,
            type: "actions",
            getActions: ({ id }) => [
                <GridActionsCellItem
                    icon={<CancelIcon />}
                    label="Cancel"
                    className="textPrimary"
                    onClick={handleCancelClick(id)}
                    color="inherit"
                />,
            ],
        },
        {
            field: "rescheduleAction",
            headerName: "Reschedule",
            width: 120,
            type: "actions",
            getActions: ({ row }) => [
                <GridActionsCellItem
                    icon={<SwapHorizIcon />}
                    label="Reschedule"
                    className="textPrimary"
                    onClick={handleRescheduleClick(row)}
                    color="inherit"
                    disabled={!canReschedule(row)}
                />,
            ],
        },
    ];

    return (
        <AuthenticatedLayout>
                <div className={styles.addingContainer}>
                    <Box
                        sx={{
                            width: "90%",
                            maxWidth: "1150px",
                            padding: "20px",
                            backgroundColor: "#fff",
                            borderRadius: "8px",
                            boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
                            margin: "0 auto",
                        }}
                    >
                        <h1 className={styles.addingHeader}>Manage Visits in Your Facility</h1>
                        <Box
                            sx={{
                                height: 500,
                                width: "100%",
                                "& .actions": {
                                    color: "text.secondary",
                                },
                                "& .textPrimary": {
                                    color: "text.primary",
                                },
                            }}
                        >
                            <DataGrid
                                rows={rows}
                                columns={columns}
                                editMode="row"
                                initialState={{
                                    sorting: {
                                        sortModel: [{ field: "visitDateTimeTs", sort: "asc" }],
                                    },
                                }}
                            />
                        </Box>
                    </Box>
                </div>

            <Dialog open={openCancelDialog} onClose={() => setOpenCancelDialog(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Cancel Appointment</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Reason for Cancellation *"
                        fullWidth
                        margin="normal"
                        multiline
                        rows={4}
                        value={cancelReason}
                        onChange={(e) => setCancelReason(e.target.value)}
                        placeholder="Please provide a reason for canceling this appointment"
                        required
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenCancelDialog(false)}>Cancel</Button>
                    <Button onClick={handleConfirmCancel} variant="contained" color="error">
                        Confirm Cancellation
                    </Button>
                </DialogActions>
            </Dialog>

            <RescheduleDialog
                open={openRescheduleDialog}
                onClose={() => {
                    setOpenRescheduleDialog(false);
                    setAppointmentToReschedule(null);
                }}
                doctorId={appointmentToReschedule?.doctorId}
                locationId={appointmentToReschedule?.locationId}
                appointmentType={appointmentToReschedule?.visitType}
                showReasonField
                onConfirm={handleConfirmReschedule}
            />

            <ToastContainer position="top-center" autoClose={4000} />
        </AuthenticatedLayout>
    );
};

export default ManageVisits;

