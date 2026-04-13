import React, { useCallback, useEffect, useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';

import { request } from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import { TextField, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';
import { toast, ToastContainer } from "react-toastify";
import { DataGrid, GridActionsCellItem } from '@mui/x-data-grid';

const ManageUsers = () => {
    const [rows, setRows] = useState([]);
    const [rowCount, setRowCount] = useState(0);
    const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 20 });
    const [searchInput, setSearchInput] = useState('');
    const [search, setSearch] = useState('');
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);

    const fetchUsers = useCallback(async () => {
        try {
            const params = new URLSearchParams({
                page: String(paginationModel.page),
                size: String(paginationModel.pageSize),
            });
            if (search.trim()) {
                params.append('search', search.trim());
            }
            const response = await request('get', `/v1/users?${params.toString()}`);
            const data = response.data;
            const content = data.content ?? [];
            setRows(
                content.map((u) => ({
                    id: u.userId,
                    userId: u.userId,
                    name: u.name ?? '',
                    surname: u.surname ?? '',
                    email: u.email ?? '',
                    authority: u.authority ?? '',
                }))
            );
            setRowCount(typeof data.totalElements === 'number' ? data.totalElements : content.length);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to load users.');
        }
    }, [paginationModel.page, paginationModel.pageSize, search]);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    const handleSearchClick = () => {
        setSearch(searchInput);
        setPaginationModel((m) => ({ ...m, page: 0 }));
    };

    const handleDeleteClick = (id) => () => {
        setUserToDelete(id);
        setOpenDeleteDialog(true);
    };

    const handleConfirmDelete = async () => {
        try {
            await request('delete', `/v1/users/${userToDelete}`);
            toast.success('User deleted successfully!');
            setOpenDeleteDialog(false);
            setUserToDelete(null);
            await fetchUsers();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to delete user. Please try again later.');
            setOpenDeleteDialog(false);
        }
    };

    const columns = [
        { field: 'userId', headerName: 'ID', width: 80 },
        { field: 'name', headerName: 'Name', width: 140, flex: 0.5 },
        { field: 'surname', headerName: 'Surname', width: 140, flex: 0.5 },
        { field: 'email', headerName: 'Email', minWidth: 220, flex: 1 },
        { field: 'authority', headerName: 'Role', width: 160 },
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
                        maxWidth: '1150px',
                        padding: '20px',
                        backgroundColor: '#fff',
                        borderRadius: '8px',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                        margin: '0 auto',
                    }}
                >
                    <h1 className={styles.addingHeader}>Manage Users</h1>
                    <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                        <TextField
                            label="Search by email or name"
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSearchClick()}
                            sx={{ minWidth: 280, flex: 1 }}
                            size="small"
                        />
                        <Button variant="contained" startIcon={<SearchIcon />} onClick={handleSearchClick}>
                            Search
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
                            paginationModel={paginationModel}
                            onPaginationModelChange={setPaginationModel}
                            pageSizeOptions={[10, 20, 50]}
                            disableRowSelectionOnClick
                        />
                    </Box>
                </Box>
            </div>

            <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
                <DialogTitle>Confirm deletion</DialogTitle>
                <DialogContent>
                    Are you sure you want to delete user ID {userToDelete}? This cannot be undone.
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDeleteDialog(false)}>Cancel</Button>
                    <Button onClick={handleConfirmDelete} variant="contained" color="error">
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>

            <ToastContainer position="top-center" autoClose={4000} />
        </AuthenticatedLayout>
    );
};

export default ManageUsers;
