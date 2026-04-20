import { createTheme } from '@mui/material/styles';

const theme = createTheme({
    palette: {
        primary: {
            main: '#6BCBB8',
            contrastText: '#ffffff',
        },
        secondary: {
            main: '#0090f8',
            contrastText: '#ffffff',
        },
        error: {
            main: '#ff4646',
        },
        success: {
            main: '#4caf50',
        },
        background: {
            default: '#0090f8',
            paper: '#ffffff',
        },
        text: {
            primary: '#1f2937',
            secondary: '#4b5563',
        },
    },
    shape: {
        borderRadius: 12,
    },
    typography: {
        fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
        button: {
            textTransform: 'none',
            fontWeight: 700,
        },
    },
    components: {
        MuiButton: {
            defaultProps: {
                disableElevation: true,
            },
            styleOverrides: {
                root: {
                    borderRadius: 999,
                },
            },
        },
        MuiChip: {
            styleOverrides: {
                root: {
                    borderRadius: 999,
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 16,
                },
            },
        },
        MuiDialog: {
            styleOverrides: {
                paper: {
                    borderRadius: 16,
                },
            },
        },
        MuiTextField: {
            defaultProps: {
                variant: 'outlined',
            },
        },
    },
});

export default theme;
