import React, { useCallback, useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';

import { request } from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import { TextField, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import { toast, ToastContainer } from "react-toastify";
import { DataGrid, GridActionsCellItem } from '@mui/x-data-grid';

const ManageSpecializations = () => {
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
    const [specToDelete, setSpecToDelete] = useState(null);
    const [openAddDialog, setOpenAddDialog] = useState(false);
    const [newSpecName, setNewSpecName] = useState('');

    const [searchInput, setSearchInput] = useState('');
    const [search, setSearch] = useState('');

    const fetchSpecializations = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams();
            if (search.trim()) {
                params.append('search', search.trim());
            }
            const response = await request('get', `/v1/specializations?${params.toString()}`);
            const data = response.data || [];
            // data handled
            setRows(
                data.map((s) => ({
                    id: s.specializationId,
                    specializationId: s.specializationId,
                    specializationName: s.specializationName ?? '',
                }))
            );
        } catch (error) {
            toast.error('Failed to load specializations.');
        } finally {
            setLoading(false);
        }
    }, [search]);

    useEffect(() => {
        fetchSpecializations();
    }, [fetchSpecializations]);

    const handleSearchClick = () => {
        setSearch(searchInput);
    };

    const handleDeleteClick = (id) => () => {
        const spec = rows.find(r => r.id === id);
        if (spec) {
            setSpecToDelete(spec);
            setOpenDeleteDialog(true);
        }
    };

    const handleConfirmDelete = async () => {
        if (!specToDelete) return;
        try {
            await request('delete', `/v1/specializations/${specToDelete.specializationId}`);
            toast.success('Specialization deleted successfully!');
            setOpenDeleteDialog(false);
            setSpecToDelete(null);
            await fetchSpecializations();
        } catch (error) {
            const msg = error.response?.data?.message || 'Failed to delete specialization. It might be assigned to doctors.';
            toast.error(msg);
            setOpenDeleteDialog(false);
        }
    };

    const handleAddSpecialization = async () => {
        if (!newSpecName.trim()) {
            toast.error('Specialization name cannot be empty.');
            return;
        }
        try {
            await request('post', '/v1/specializations', { specializationName: newSpecName.trim() });
            toast.success('Specialization added successfully!');
            setOpenAddDialog(false);
            setNewSpecName('');
            await fetchSpecializations();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to add specialization.');
        }
    };

    const columns = [
        { field: 'specializationId', headerName: 'ID', width: 100 },
        { field: 'specializationName', headerName: 'Specialization Name', flex: 1 },
        {
            field: 'actions',
            headerName: 'Actions',
            width: 100,
            type: 'actions',
            getActions: ({ id }) => [
                <GridActionsCellItem
                    key="delete"
                    icon={<DeleteIcon />}
                    label="Delete"
                    className="textPrimary"
                    onClick={handleDeleteClick(id)}
                    color="inherit"
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
                        maxWidth: '800px',
                        padding: '20px',
                        backgroundColor: '#fff',
                        borderRadius: '8px',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                        margin: '0 auto',
                    }}
                >
                    <h1 className={styles.addingHeader}>Manage Specializations</h1>
                    
                    <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                        <TextField
                            label="Search by specialization name"
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSearchClick()}
                            sx={{ minWidth: 280, flex: 1 }}
                            size="small"
                        />
                        <Button 
                            variant="contained" 
                            startIcon={<SearchIcon />} 
                            onClick={handleSearchClick}
                            sx={{
                                borderRadius: '10px',
                                textTransform: 'none',
                                fontWeight: 600,
                                px: 3
                            }}
                        >
                            Search
                        </Button>
                        <Button 
                            variant="contained" 
                            startIcon={<AddIcon />} 
                            onClick={() => setOpenAddDialog(true)}
                            sx={{
                                backgroundColor: '#6BCBB8',
                                color: '#1a1a1a',
                                borderRadius: '10px',
                                textTransform: 'none',
                                fontWeight: 'bold',
                                px: 3,
                                '&:hover': {
                                    backgroundColor: '#59bca8',
                                    boxShadow: '0 4px 12px rgba(107, 203, 184, 0.4)',
                                }
                            }}
                        >
                            Add New
                        </Button>
                    </Box>

                    <Box
                        sx={{
                            height: 520,
                            width: '100%',
                            '& .textPrimary': { color: 'text.primary' },
                        }}
                    >
                        <DataGrid
                            rows={rows}
                            columns={columns}
                            loading={loading}
                            pageSizeOptions={[10, 20, 50]}
                            initialState={{
                                pagination: { paginationModel: { pageSize: 20 } },
                            }}
                            disableRowSelectionOnClick
                        />
                    </Box>
                </Box>
            </div>

            {/* Add Dialog */}
            <Dialog open={openAddDialog} onClose={() => setOpenAddDialog(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Add New Specialization</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="Specialization Name"
                        type="text"
                        fullWidth
                        variant="outlined"
                        value={newSpecName}
                        onChange={(e) => setNewSpecName(e.target.value)}
                        required
                    />
                </DialogContent>
                <DialogActions sx={{ p: 2, gap: 1 }}>
                    <Button onClick={() => setOpenAddDialog(false)} sx={{ borderRadius: '8px', textTransform: 'none' }}>Cancel</Button>
                    <Button 
                        onClick={handleAddSpecialization} 
                        variant="contained" 
                        sx={{
                            backgroundColor: '#6BCBB8',
                            color: '#1a1a1a',
                            borderRadius: '8px',
                            textTransform: 'none',
                            fontWeight: 'bold',
                            '&:hover': { backgroundColor: '#59bca8' }
                        }}
                    >
                        Add Specialization
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Delete Dialog */}
            <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
                <DialogTitle>Confirm deletion</DialogTitle>
                <DialogContent>
                    Are you sure you want to delete specialization <strong>{specToDelete?.specializationName}</strong>? This action cannot be undone.
                </DialogContent>
                <DialogActions sx={{ p: 2, gap: 1 }}>
                    <Button onClick={() => setOpenDeleteDialog(false)} sx={{ borderRadius: '8px', textTransform: 'none' }}>Cancel</Button>
                    <Button 
                        onClick={handleConfirmDelete} 
                        variant="contained" 
                        color="error"
                        sx={{ borderRadius: '8px', textTransform: 'none', fontWeight: 'bold' }}
                    >
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            <ToastContainer position="top-center" autoClose={4000} />
        </AuthenticatedLayout>
    );
};

export default ManageSpecializations;
