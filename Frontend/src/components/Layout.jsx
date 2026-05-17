// Layout component will go here
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import {
  Home, Building, FileText, Bell, LogOut, Users, BarChart2, AlertCircle,
} from "lucide-react";

const navByRole = {
  LANDLORD: [
    { label: "Dashboard", path: "/landlord/dashboard", icon: Home },
    { label: "Properties", path: "/landlord/properties", icon: Building },
    { label: "Contracts", path: "/landlord/contracts", icon: FileText },
  ],
  TENANT: [
    { label: "Dashboard", path: "/tenant/dashboard", icon: Home },
    { label: "My Contracts", path: "/tenant/contracts", icon: FileText },
    { label: "My Appeals", path: "/tenant/appeals", icon: AlertCircle },
  ],
  SUBCITY_STAFF: [
    { label: "Dashboard", path: "/officer/dashboard", icon: Home },
    { label: "Properties", path: "/officer/properties", icon: Building },
    { label: "Declarations", path: "/officer/declarations", icon: BarChart2 },
    { label: "Appeals", path: "/officer/appeals", icon: AlertCircle },
  ],
  ADMINISTRATOR: [
    { label: "Dashboard", path: "/officer/dashboard", icon: Home },
    { label: "Properties", path: "/officer/properties", icon: Building },
    { label: "Declarations", path: "/officer/declarations", icon: BarChart2 },
    { label: "Appeals", path: "/officer/appeals", icon: AlertCircle },
  ],
};

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const navItems = navByRole[user?.role] || [];

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="w-64 bg-primary text-white flex flex-col fixed h-full">
        <div className="p-6 border-b border-blue-800">
          <h1 className="text-xl font-bold">RentalPro ET</h1>
          <p className="text-xs text-blue-300 mt-1">{user?.role?.replace(/_/g, " ")}</p>
        </div>
        <nav className="flex-1 p-4 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm transition-all
                  ${active ? "bg-white text-primary font-semibold" : "text-blue-100 hover:bg-blue-800"}`}
              >
                <Icon size={18} />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="p-4 border-t border-blue-800">
          <p className="text-sm font-medium">{user?.firstName} {user?.lastName}</p>
          <p className="text-xs text-blue-300 mb-3">{user?.email}</p>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 text-sm text-blue-200 hover:text-white"
          >
            <LogOut size={16} /> Logout
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="ml-64 flex-1 overflow-y-auto">
        <div className="p-8">{children}</div>
      </main>
    </div>
  );
}