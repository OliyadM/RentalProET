import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";

import Login from "./pages/Login";
import Register from "./pages/Register";

import LandlordDashboard from "./pages/landlord/Dashboard";
import LandlordProperties from "./pages/landlord/Properties";
import AddProperty from "./pages/landlord/AddProperty";
import PropertyDetail from "./pages/landlord/PropertyDetail";
import AddUnit from "./pages/landlord/AddUnit";
import LandlordContracts from "./pages/landlord/Contracts";
import CreateContract from "./pages/landlord/CreateContract";
import LandlordContractDetail from "./pages/landlord/ContractDetail";
import AddDeclaration from "./pages/landlord/AddDeclaration";

import TenantDashboard from "./pages/tenant/Dashboard";
import TenantContracts from "./pages/tenant/Contracts";
import TenantContractDetail from "./pages/tenant/ContractDetail";
import TenantAppeals from "./pages/tenant/Appeals";

import OfficerDashboard from "./pages/officer/Dashboard";
import OfficerProperties from "./pages/officer/Properties";
import OfficerDeclarations from "./pages/officer/Declarations";
import OfficerAppeals from "./pages/officer/Appeals";

import AdminDashboard from "./pages/admin/AdminDashboard";

import Profile from "./pages/Profile";
import ProfileVerification from "./pages/officer/ProfileVerification";

function ProtectedRoute({ children, roles }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/login" />;
  return children;
}

export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Landlord */}
      <Route path="/landlord/dashboard" element={<ProtectedRoute roles={["LANDLORD"]}><LandlordDashboard /></ProtectedRoute>} />
      <Route path="/landlord/properties" element={<ProtectedRoute roles={["LANDLORD"]}><LandlordProperties /></ProtectedRoute>} />
      <Route path="/landlord/properties/add" element={<ProtectedRoute roles={["LANDLORD"]}><AddProperty /></ProtectedRoute>} />
      <Route path="/landlord/properties/:id" element={<ProtectedRoute roles={["LANDLORD"]}><PropertyDetail /></ProtectedRoute>} />
      <Route path="/landlord/units/add/:propertyId" element={<ProtectedRoute roles={["LANDLORD"]}><AddUnit /></ProtectedRoute>} />
      <Route path="/landlord/contracts" element={<ProtectedRoute roles={["LANDLORD"]}><LandlordContracts /></ProtectedRoute>} />
      <Route path="/landlord/contracts/create" element={<ProtectedRoute roles={["LANDLORD"]}><CreateContract /></ProtectedRoute>} />
      <Route path="/landlord/contracts/:id" element={<ProtectedRoute roles={["LANDLORD"]}><LandlordContractDetail /></ProtectedRoute>} />
      <Route path="/landlord/declarations/add/:contractId" element={<ProtectedRoute roles={["LANDLORD"]}><AddDeclaration /></ProtectedRoute>} />

      {/* Tenant */}
      <Route path="/tenant/dashboard" element={<ProtectedRoute roles={["TENANT"]}><TenantDashboard /></ProtectedRoute>} />
      <Route path="/tenant/contracts" element={<ProtectedRoute roles={["TENANT"]}><TenantContracts /></ProtectedRoute>} />
      <Route path="/tenant/contracts/:id" element={<ProtectedRoute roles={["TENANT"]}><TenantContractDetail /></ProtectedRoute>} />
      <Route path="/tenant/appeals" element={<ProtectedRoute roles={["TENANT"]}><TenantAppeals /></ProtectedRoute>} />

      {/* Officer — SUBCITY_STAFF only (ADMINISTRATOR has their own module) */}
      <Route path="/officer/dashboard" element={<ProtectedRoute roles={["SUBCITY_STAFF"]}><OfficerDashboard /></ProtectedRoute>} />
      <Route path="/officer/properties" element={<ProtectedRoute roles={["SUBCITY_STAFF"]}><OfficerProperties /></ProtectedRoute>} />
      <Route path="/officer/declarations" element={<ProtectedRoute roles={["SUBCITY_STAFF"]}><OfficerDeclarations /></ProtectedRoute>} />
      <Route path="/officer/appeals" element={<ProtectedRoute roles={["SUBCITY_STAFF"]}><OfficerAppeals /></ProtectedRoute>} />

      {/* Administrator — isolated admin module */}
      <Route path="/admin/dashboard" element={<ProtectedRoute roles={["ADMINISTRATOR"]}><AdminDashboard /></ProtectedRoute>} />

      {/* Profile - accessible by all authenticated users */}
      <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />

      {/* Profile Verification - Officer only */}
      <Route path="/officer/profile-verification" element={<ProtectedRoute roles={["SUBCITY_STAFF"]}><ProfileVerification /></ProtectedRoute>} />

      {/* Default redirect */}
      <Route path="/" element={
        user?.role === "LANDLORD"      ? <Navigate to="/landlord/dashboard" /> :
        user?.role === "TENANT"        ? <Navigate to="/tenant/dashboard" /> :
        user?.role === "SUBCITY_STAFF" ? <Navigate to="/officer/dashboard" /> :
        user?.role === "ADMINISTRATOR" ? <Navigate to="/admin/dashboard" /> :
        <Navigate to="/login" />
      } />
    </Routes>
  );
}