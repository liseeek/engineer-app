import { ROLES } from './roles';

const permissions = {
    [ROLES.ADMIN]: [
        { label: 'Add Worker', path: '/addWorker', icon: 'fa-regular fa-id-card' },
        { label: 'Add Location', path: '/addLocation', icon: 'fa-solid fa-house-medical' },
        { label: 'Delete Location', path: '/deleteLocation', icon: 'fa-solid fa-trash' },
        { label: 'Send Invitation', path: '/send-invitation', icon: 'fa-solid fa-envelope' },
        { label: 'Manage Users', path: '/manage-users', icon: 'fa-solid fa-users' },
    ],
    [ROLES.PATIENT]: [
        { label: 'Visits', path: '/visits', icon: 'fa-solid fa-calendar-days' },
        { label: 'Booking', path: '/booking', icon: 'fa-regular fa-calendar-check' },
        { label: 'Mainpage', path: '/mainpage', icon: 'fa-solid fa-house' },
    ],
    [ROLES.WORKER]: [
        { label: 'Request doctor', path: '/addDoctor', icon: 'fa-solid fa-user-plus' },
        { label: 'Delete Doctor', path: '/deleteDoctor', icon: 'fa-solid fa-trash' },
        { label: 'Update Doctor', path: '/updateDoctorLocation', icon: 'fa-solid fa-pen-to-square' },
        { label: 'Availability', path: '/addDoctorAvailability', icon: 'fa-solid fa-plus' },
        { label: 'Manage Visits', path: '/manageVisits', icon: 'fa-regular fa-calendar-check' },
    ],
    [ROLES.DOCTOR]: [
        { label: 'My Schedule', path: '/doctor/schedule', icon: 'fa-solid fa-calendar-days' },
        { label: 'Facility requests', path: '/doctor/facility-requests', icon: 'fa-solid fa-hospital' },
    ],
};

export default permissions;