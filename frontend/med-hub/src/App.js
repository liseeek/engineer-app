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
import './global.css'
import Unauthorized from "./helpers/unauthorized";


function App() {
  return (
  <Router>
    <div className="App">
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/mainpage" element={<MainPage />} />
          <Route element={<ProtectedRoute requiredRoles={['ROLE_ADMIN']} />}>
            <Route path="/addWorker" element={<AddWorker />} />
            <Route path="/addLocation" element={<AddLocation />} />
            <Route path="/deleteLocation" element={<DeleteLocation />} />
          </Route>
          <Route element={<ProtectedRoute requiredRoles={['ROLE_WORKER']} />}>
            <Route path="/addDoctor" element={<AddDoctor />} />
            <Route path="/deleteDoctor" element={<DeleteDoctor />} />
            <Route path="/updateDoctorLocation" element={<UpdateDoctorLocation />} />
            <Route path="/addDoctorAvailability" element={<AddDoctorAvailability />} />
            <Route path="/manageVisits" element={<ManageVisits />} />
          </Route>
          <Route element={<ProtectedRoute requiredRoles={['ROLE_USER']} />}>
            <Route path="/visits" element={<Visits />} />
            <Route path="/booking" element={<Booking />} />
          </Route>
        </Route>
      </Routes>

    </div>
  </Router>
    
  );
}

export default App;
