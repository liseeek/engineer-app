import React, { useState } from 'react';
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout';
import styles from '../../../components/Adding.module.css';

import { request } from "../../../helpers/axiosHelper";
import Box from '@mui/material/Box';
import { TextField, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { toast, ToastContainer } from "react-toastify";

const ManageUsers = () => {
    const [userId, setUserId] = useState('');
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);

    const handleDeleteClick = () => {
        if (!userId.trim()) {
            toast.error('Please enter a user ID');
            return;
        }
        setUserToDelete(userId);
        setOpenDeleteDialog(true);
    };

    const handleConfirmDelete = async () => {
        try {
            await request('delete', `/v1/users/${userToDelete}`);
            toast.success("User deleted successfully!");
            setUserId('');
            setOpenDeleteDialog(false);
            setUserToDelete(null);
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to delete user. Please try again later.");
            setOpenDeleteDialog(false);
        }
    };

    return (
        <AuthenticatedLayout>
                <div className={styles.addingContainer}>
                    <Box
                        sx={{
                            width: '90%',
                            maxWidth: '600px',
                            padding: '20px',
                            backgroundColor: '#fff',
                            borderRadius: '8px',
                            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                            margin: '0 auto',
                        }}
                    >
                        <h1 className={styles.addingHeader}>Manage Users</h1>
                        <Box sx={{ mt: 2 }}>
                            <TextField
                                label="User ID"
                                type="number"
                                fullWidth
                                margin="normal"
                                value={userId}
                                onChange={(e) => setUserId(e.target.value)}
                                placeholder="Enter user ID to delete"
                            />
                            <Button
                                variant="contained"
                                color="error"
                                startIcon={<DeleteIcon />}
                                onClick={handleDeleteClick}
                                sx={{ mt: 2 }}
                                fullWidth
                            >
                                Delete User
                            </Button>
                        </Box>
                    </Box>
                </div>

            <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
                <DialogTitle>Confirm Deletion</DialogTitle>
                <DialogContent>
                    Are you sure you want to delete user with ID: {userToDelete}? This action cannot be undone.
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
