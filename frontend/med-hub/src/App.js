import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import ProtectedRoute from "./helpers/protectedRoute";
import Register from './pages/default/register/Register';
import Login from './pages/default/login/Login';
import MainPage from './pages/user/mainpage/MainPage';
import AddDoctor from './pages/worker/addDoctor/AddDoctor';
import Booking from './pages/user/booking/Booking';
import Visits from './pages/user/visits/Visits';

import ManageVisits from "./pages/worker/manageVisits/ManageVisits";
import ManageLocations from "./pages/admin/manageLocations/ManageLocations";
import DeleteDoctor from "./pages/worker/deleteDoctor/DeleteDoctor";
import UpdateDoctorLocation from "./pages/worker/updateDoctorLocation/UpdateDoctorLocation";
import AddDoctorAvailability from "./pages/worker/addDoctorAvailability/AddDoctorAvailability";
import RegisterInvitation from "./pages/default/registerInvitation/RegisterInvitation";
import RegisterDoctor from "./pages/default/registerDoctor/RegisterDoctor";
import SendInvitation from "./pages/admin/sendInvitation/SendInvitation";
import DoctorSchedule from "./pages/doctor/schedule/DoctorSchedule";
import DoctorFacilityRequests from "./pages/doctor/facilityRequests/DoctorFacilityRequests";
import DoctorOwnProfile from "./pages/doctor/profile/DoctorOwnProfile";
import ManageUsers from "./pages/admin/manageUsers/ManageUsers";
import DoctorProfile from "./pages/user/doctorProfile/DoctorProfile";
import LocationProfile from "./pages/user/locationProfile/LocationProfile";
import VerifyDoctors from "./pages/admin/verifyDoctors/VerifyDoctors";
import ManageSpecializations from "./pages/admin/manageSpecializations/ManageSpecializations";
import './global.css'
import Unauthorized from "./helpers/unauthorized";
import { ROLES } from './helpers/roles';
import { AuthProvider } from './context/AuthContext';
import Footer from "./components/Footer";


function App() {
  return (
    <Router>
      <AuthProvider>
      <div className="App">
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/register/doctor" element={<RegisterDoctor />} />
          <Route path="/register-invitation/:token" element={<RegisterInvitation />} />
          <Route path="/unauthorized" element={<Unauthorized />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/mainpage" element={<MainPage />} />
            <Route element={<ProtectedRoute requiredRoles={[ROLES.ADMIN]} />}>

              <Route path="/manage-locations" element={<ManageLocations />} />
              <Route path="/send-invitation" element={<SendInvitation />} />
              <Route path="/manage-users" element={<ManageUsers />} />
              <Route path="/verify-doctors" element={<VerifyDoctors />} />
              <Route path="/manage-specializations" element={<ManageSpecializations />} />
            </Route>
            <Route element={<ProtectedRoute requiredRoles={[ROLES.WORKER]} />}>
              <Route path="/addDoctor" element={<AddDoctor />} />
              <Route path="/deleteDoctor" element={<DeleteDoctor />} />
              <Route path="/updateDoctorLocation" element={<UpdateDoctorLocation />} />
              <Route path="/addDoctorAvailability" element={<AddDoctorAvailability />} />
              <Route path="/manageVisits" element={<ManageVisits />} />
            </Route>
            <Route element={<ProtectedRoute requiredRoles={[ROLES.PATIENT]} />}>
              <Route path="/visits" element={<Visits />} />
              <Route path="/booking" element={<Booking />} />
            </Route>
            <Route element={<ProtectedRoute requiredRoles={[ROLES.DOCTOR]} />}>
              <Route path="/doctor/schedule" element={<DoctorSchedule />} />
              <Route path="/doctor/facility-requests" element={<DoctorFacilityRequests />} />
              <Route path="/doctor/profile" element={<DoctorOwnProfile />} />
            </Route>
            <Route element={<ProtectedRoute requiredRoles={[ROLES.PATIENT, ROLES.WORKER, ROLES.ADMIN, ROLES.DOCTOR]} />}>
              <Route path="/doctors/:id" element={<DoctorProfile />} />
              <Route path="/locations/:id" element={<LocationProfile />} />
            </Route>
          </Route>
          <Route path="*" element={<Unauthorized />} />
        </Routes>
        <Footer />
      </div>
      </AuthProvider>
    </Router>

  );
}

export default App;
