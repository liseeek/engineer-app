import React from "react";
import { useNavigate } from "react-router-dom";
import { Box, Button, Typography } from "@mui/material";

const Unauthorized = () => {
    const navigate = useNavigate();

    return (
        <Box
            sx={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                height: "100vh",
                backgroundColor: "#f8f9fa",
            }}
        >
            <Box
                sx={{
                    textAlign: "center",
                    p: 3,
                    border: "1px solid #ccc",
                    borderRadius: "8px",
                    maxWidth: "400px",
                    boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
                    backgroundColor: "#fff",
                }}
            >
                <Typography variant="h4" gutterBottom>
                    Access Denied
                </Typography>
                <Typography variant="body1" color="textSecondary" sx={{ mb: 3 }}>
                    You do not have permission to access this page.
                </Typography>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => navigate("/")}
                >
                    Go to Home
                </Button>
            </Box>
        </Box>
    );
};

export default Unauthorized;
