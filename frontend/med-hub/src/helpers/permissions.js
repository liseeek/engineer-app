import { ROLES } from './roles';

const permissions = {
    [ROLES.ADMIN]: [
        { label: 'Add Worker', path: '/addWorker', icon: 'fa-regular fa-id-card' },
        { label: 'Add Location', path: '/addLocation', icon: 'fa-solid fa-house-medical' },
        { label: 'Delete Location', path: '/deleteLocation', icon: 'fa-solid fa-trash' },
    ],
    [ROLES.USER]: [
        { label: 'Visits', path: '/visits', icon: 'fa-solid fa-calendar-days' },
        { label: 'Booking', path: '/booking', icon: 'fa-regular fa-calendar-check' },
        { label: 'Mainpage', path: '/mainPage', icon: 'fa-solid fa-house' },
    ],
    [ROLES.WORKER]: [
        { label: 'Add Doctor', path: '/addDoctor', icon: 'fa-solid fa-user' },
        { label: 'Delete Doctor', path: '/deleteDoctor', icon: 'fa-solid fa-trash' },
        { label: 'Update Doctor', path: '/updateDoctorLocation', icon: 'fa-solid fa-pen-to-square' },
        { label: 'Availability', path: '/addDoctorAvailability', icon: 'fa-solid fa-plus' },
        { label: 'Manage Visits', path: '/manageVisits', icon: 'fa-regular fa-calendar-check' },
    ],
};

export default permissions;