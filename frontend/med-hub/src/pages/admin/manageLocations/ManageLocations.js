import React, { useCallback, useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';

import { request } from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import { TextField, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import { toast, ToastContainer } from "react-toastify";
import { DataGrid, GridActionsCellItem } from '@mui/x-data-grid';

const ManageLocations = () => {
    const [rows, setRows] = useState([]);
    const [rowCount, setRowCount] = useState(0);
    const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 20 });
    const [searchInput, setSearchInput] = useState('');
    const [search, setSearch] = useState('');
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
    const [locationToDelete, setLocationToDelete] = useState(null);
    const [loading, setLoading] = useState(false);

    const [openEditDialog, setOpenEditDialog] = useState(false);
    const [editData, setEditData] = useState({
        locationId: null,
        locationName: '',
        description: '',
        yearEstablished: '',
        phoneNumber: '',
        email: ''
    });

    const [openAddDialog, setOpenAddDialog] = useState(false);
    const [newData, setNewData] = useState({
        locationName: '',
        address: '',
        city: '',
        country: ''
    });

    const fetchLocations = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: String(paginationModel.page),
                size: String(paginationModel.pageSize),
            });
            if (search.trim()) {
                params.append('search', search.trim());
            }
            const response = await request('get', `/v1/locations?${params.toString()}`);
            const data = response.data;
            const content = data.content ?? [];
            setRows(
                content.map((loc) => ({
                    id: loc.locationId,
                    locationId: loc.locationId,
                    locationName: loc.locationName ?? '',
                    address: loc.address ?? '',
                    city: loc.city ?? '',
                    country: loc.country ?? '',
                    description: loc.description ?? '',
                    yearEstablished: loc.yearEstablished ?? '',
                    phoneNumber: loc.phoneNumber ?? '',
                    email: loc.email ?? '',
                }))
            );
            setRowCount(typeof data.totalElements === 'number' ? data.totalElements : content.length);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to load locations.');
        } finally {
            setLoading(false);
        }
    }, [paginationModel.page, paginationModel.pageSize, search]);

    useEffect(() => {
        fetchLocations();
    }, [fetchLocations]);

    const handleSearchClick = () => {
        setSearch(searchInput);
        setPaginationModel((m) => ({ ...m, page: 0 }));
    };

    const handleDeleteClick = (id) => () => {
        const loc = rows.find(r => r.id === id);
        setLocationToDelete(loc);
        setOpenDeleteDialog(true);
    };

    const handleConfirmDelete = async () => {
        try {
            await request('delete', `/v1/locations/${locationToDelete.locationId}`);
            toast.success('Location deleted successfully!');
            setOpenDeleteDialog(false);
            setLocationToDelete(null);
            await fetchLocations();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to delete location. It might have active assignments.');
            setOpenDeleteDialog(false);
        }
    };

    const handleEditClick = (id) => () => {
        const loc = rows.find(r => r.id === id);
        setEditData({
            locationId: loc.locationId,
            locationName: loc.locationName,
            description: loc.description,
            yearEstablished: loc.yearEstablished,
            phoneNumber: loc.phoneNumber,
            email: loc.email
        });
        setOpenEditDialog(true);
    };

    const handleSaveEdit = async () => {
        try {
            await request('patch', `/v1/locations/${editData.locationId}`, {
                description: editData.description,
                yearEstablished: editData.yearEstablished ? parseInt(editData.yearEstablished) : null,
                phoneNumber: editData.phoneNumber,
                email: editData.email
            });
            toast.success('Location updated successfully!');
            setOpenEditDialog(false);
            await fetchLocations();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to update location.');
        }
    };

    const handleAddLocation = async () => {
        try {
            await request('post', '/v1/locations', newData);
            toast.success('Location added successfully!');
            setOpenAddDialog(false);
            setNewData({ locationName: '', address: '', city: '', country: '' });
            await fetchLocations();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to add location.');
        }
    };

    const columns = [
        { field: 'locationId', headerName: 'ID', width: 80 },
        { field: 'locationName', headerName: 'Location Name', width: 200, flex: 1 },
        { field: 'city', headerName: 'City', width: 140 },
        { field: 'address', headerName: 'Address', width: 200, flex: 0.8 },
        { field: 'country', headerName: 'Country', width: 120 },
        {
            field: 'actions',
            headerName: 'Actions',
            width: 120,
            type: 'actions',
            getActions: ({ id }) => [
                <GridActionsCellItem
                    key="edit"
                    icon={<EditIcon />}
                    label="Edit"
                    onClick={handleEditClick(id)}
                    color="inherit"
                />,
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
                        maxWidth: '1150px',
                        padding: '20px',
                        backgroundColor: '#fff',
                        borderRadius: '8px',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                        margin: '0 auto',
                    }}
                >
                    <h1 className={styles.addingHeader}>Manage Locations</h1>
                    <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                        <TextField
                            label="Search by location name"
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
                            paginationMode="server"
                            rowCount={rowCount}
                            loading={loading}
                            paginationModel={paginationModel}
                            onPaginationModelChange={setPaginationModel}
                            pageSizeOptions={[10, 20, 50]}
                            disableRowSelectionOnClick
                        />
                    </Box>
                </Box>
            </div>

            {/* Add Dialog */}
            <Dialog open={openAddDialog} onClose={() => setOpenAddDialog(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Add New Location</DialogTitle>
                <DialogContent>
                    <TextField
                        margin="dense"
                        label="Location Name"
                        fullWidth
                        variant="outlined"
                        value={newData.locationName}
                        onChange={(e) => setNewData({ ...newData, locationName: e.target.value })}
                        required
                    />
                    <TextField
                        margin="dense"
                        label="Address"
                        fullWidth
                        variant="outlined"
                        value={newData.address}
                        onChange={(e) => setNewData({ ...newData, address: e.target.value })}
                        required
                    />
                    <TextField
                        margin="dense"
                        label="City"
                        fullWidth
                        variant="outlined"
                        value={newData.city}
                        onChange={(e) => setNewData({ ...newData, city: e.target.value })}
                        required
                    />
                    <TextField
                        margin="dense"
                        label="Country"
                        fullWidth
                        variant="outlined"
                        value={newData.country}
                        onChange={(e) => setNewData({ ...newData, country: e.target.value })}
                        required
                    />
                </DialogContent>
                <DialogActions sx={{ p: 2, gap: 1 }}>
                    <Button onClick={() => setOpenAddDialog(false)} sx={{ borderRadius: '8px', textTransform: 'none' }}>Cancel</Button>
                    <Button 
                        onClick={handleAddLocation} 
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
                        Add Location
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Edit Dialog */}
            <Dialog open={openEditDialog} onClose={() => setOpenEditDialog(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Edit Location: {editData.locationName}</DialogTitle>
                <DialogContent>
                    <TextField
                        margin="dense"
                        label="Description"
                        type="text"
                        fullWidth
                        multiline
                        rows={3}
                        variant="outlined"
                        value={editData.description}
                        onChange={(e) => setEditData({ ...editData, description: e.target.value })}
                    />
                    <TextField
                        margin="dense"
                        label="Year Established"
                        type="number"
                        fullWidth
                        variant="outlined"
                        value={editData.yearEstablished}
                        onChange={(e) => setEditData({ ...editData, yearEstablished: e.target.value })}
                    />
                    <TextField
                        margin="dense"
                        label="Phone Number"
                        type="text"
                        fullWidth
                        variant="outlined"
                        value={editData.phoneNumber}
                        onChange={(e) => setEditData({ ...editData, phoneNumber: e.target.value })}
                    />
                    <TextField
                        margin="dense"
                        label="Email"
                        type="email"
                        fullWidth
                        variant="outlined"
                        value={editData.email}
                        onChange={(e) => setEditData({ ...editData, email: e.target.value })}
                    />
                </DialogContent>
                <DialogActions sx={{ p: 2, gap: 1 }}>
                    <Button onClick={() => setOpenEditDialog(false)} sx={{ borderRadius: '8px', textTransform: 'none' }}>Cancel</Button>
                    <Button 
                        onClick={handleSaveEdit} 
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
                        Save Changes
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Delete Dialog */}
            <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
                <DialogTitle>Confirm deletion</DialogTitle>
                <DialogContent>
                    Are you sure you want to delete location <strong>{locationToDelete?.locationName}</strong>? This action cannot be undone.
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

export default ManageLocations;
