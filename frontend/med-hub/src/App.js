import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import ProtectedRoute from "./helpers/protectedRoute";
import Register from './pages/default/register/Register';
import Login from './pages/default/login/Login';
import MainPage from './pages/user/mainpage/MainPage';
import AddDoctor from './pages/worker/addDoctor/AddDoctor';
import Booking from './pages/user/booking/Booking';
import Visits from './pages/user/visits/Visits';
import AddWorker from "./pages/admin/addWorker/AddWorker";
import ManageVisits from "./pages/worker/manageVisits/ManageVisits";
import AddLocation from "./pages/admin/addLocation/AddLocation";
import DeleteLocation from "./pages/admin/deleteLocation/DeleteLocation";
import DeleteDoctor from "./pages/worker/deleteDoctor/DeleteDoctor";
import UpdateDoctorLocation from "./pages/worker/updateDoctorLocation/UpdateDoctorLocation";
import AddDoctorAvailability from "./pages/worker/addDoctorAvailability/AddDoctorAvailability";
import RegisterInvitation from "./pages/default/registerInvitation/RegisterInvitation";
import RegisterDoctor from "./pages/default/registerDoctor/RegisterDoctor";
import SendInvitation from "./pages/admin/sendInvitation/SendInvitation";
import DoctorSchedule from "./pages/doctor/schedule/DoctorSchedule";
import DoctorFacilityRequests from "./pages/doctor/facilityRequests/DoctorFacilityRequests";
import ManageUsers from "./pages/admin/manageUsers/ManageUsers";
import './global.css'
import Unauthorized from "./helpers/unauthorized";
import { ROLES } from './helpers/roles';
import { AuthProvider } from './context/AuthContext';


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
              <Route path="/addWorker" element={<AddWorker />} />
              <Route path="/addLocation" element={<AddLocation />} />
              <Route path="/deleteLocation" element={<DeleteLocation />} />
              <Route path="/send-invitation" element={<SendInvitation />} />
              <Route path="/manage-users" element={<ManageUsers />} />
            </Route>
            <Route element={<ProtectedRoute requiredRoles={[ROLES.WORKER]} />}>
              <Route path="/addDoctor" element={<AddDoctor />} />
              <Route path="/deleteDoctor" element={<DeleteDoctor />} />
              <Route path="/updateDoctorLocation" element={<UpdateDoctorLocation />} />
              <Route path="/addDoctorAvailability" element={<AddDoctorAvailability />} />
              <Route path="/manageVisits" element={<ManageVisits />} />
            </Route>
            <Route element={<ProtectedRoute requiredRoles={[ROLES.USER]} />}>
              <Route path="/visits" element={<Visits />} />
              <Route path="/booking" element={<Booking />} />
            </Route>
            <Route element={<ProtectedRoute requiredRoles={[ROLES.DOCTOR]} />}>
              <Route path="/doctor/schedule" element={<DoctorSchedule />} />
              <Route path="/doctor/facility-requests" element={<DoctorFacilityRequests />} />
            </Route>
          </Route>
        </Routes>

      </div>
      </AuthProvider>
    </Router>

  );
}

export default App;
