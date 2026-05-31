import React, { useCallback, useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';
import { request } from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import { Button, Typography, Chip } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import { toast, ToastContainer } from "react-toastify";
import { DataGrid, GridActionsCellItem } from '@mui/x-data-grid';
 
const VerifyDoctors = () => {
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 20 });
    const [rowCount, setRowCount] = useState(0);
 
    const fetchPendingDoctors = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: String(paginationModel.page),
                size: String(paginationModel.pageSize),
                status: 'PENDING'
            });
            const response = await request('get', `/v1/doctors?${params.toString()}`);
            const data = response.data;
            const content = data.content ?? [];
            setRows(content.map(d => ({
                id: d.doctorId,
                ...d,
                specializationsStr: d.specializations.map(s => s.specializationName).join(', ')
            })));
            setRowCount(data.totalElements || content.length);
        } catch (error) {
            toast.error('Failed to load pending doctors.');
        } finally {
            setLoading(false);
        }
    }, [paginationModel]);
 
    useEffect(() => {
        fetchPendingDoctors();
    }, [fetchPendingDoctors]);
 
    const handleVerify = (id, status) => async () => {
        try {
            await request('patch', `/v1/doctors/${id}/verify?status=${status}`);
            toast.success(`Doctor ${status === 'VERIFIED' ? 'verified' : 'rejected'} successfully`);
            fetchPendingDoctors();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Action failed');
        }
    };
 
    const columns = [
        { field: 'doctorId', headerName: 'ID', width: 70 },
        { field: 'name', headerName: 'Name', width: 130 },
        { field: 'surname', headerName: 'Surname', width: 130 },
        { field: 'email', headerName: 'Email', width: 200 },
        { field: 'pwz', headerName: 'PWZ', width: 100 },
        { field: 'specializationsStr', headerName: 'Specializations', width: 200, flex: 1 },
        {
            field: 'actions',
            headerName: 'Actions',
            type: 'actions',
            width: 120,
            getActions: (params) => [
                <GridActionsCellItem
                    key="verify"
                    icon={<CheckCircleIcon color="success" />}
                    label="Verify"
                    onClick={handleVerify(params.id, 'VERIFIED')}
                />,
                <GridActionsCellItem
                    key="reject"
                    icon={<CancelIcon color="error" />}
                    label="Reject"
                    onClick={handleVerify(params.id, 'REJECTED')}
                />,
            ],
        },
    ];
 
    return (
        <AuthenticatedLayout>
            <div className={styles.addingContainer}>
                <Box
                    sx={{
                        width: '90%',
                        maxWidth: '1200px',
                        padding: '20px',
                        backgroundColor: '#fff',
                        borderRadius: '8px',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                        margin: '0 auto',
                    }}
                >
                    <h1 className={styles.addingHeader}>Doctor Verification Queue</h1>
                    <Typography variant="body1" sx={{ mb: 3, color: 'text.secondary', textAlign: 'center' }}>
                        The following doctors have self-registered and are awaiting PWZ verification.
                    </Typography>
                    
                    <Box sx={{ height: 600, width: '100%' }}>
                        <DataGrid
                            rows={rows}
                            columns={columns}
                            rowCount={rowCount}
                            loading={loading}
                            paginationMode="server"
                            paginationModel={paginationModel}
                            onPaginationModelChange={setPaginationModel}
                            pageSizeOptions={[10, 20, 50]}
                            disableRowSelectionOnClick
                        />
                    </Box>
                </Box>
            </div>
            <ToastContainer position="top-center" autoClose={4000} />
        </AuthenticatedLayout>
    );
};
 
export default VerifyDoctors;
