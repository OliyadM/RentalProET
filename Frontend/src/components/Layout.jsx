import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import {
  Home, Building, FileText, LogOut, BarChart2, AlertCircle,
  Settings, Users, Globe, UserCircle,
} from "lucide-react";
import NotificationBell from "./NotificationBell";

const navByRole = {
  LANDLORD: [
    { label: "Dashboard",   path: "/landlord/dashboard",  icon: Home },
    { label: "Properties",  path: "/landlord/properties", icon: Building },
    { label: "Contracts",   path: "/landlord/contracts",  icon: FileText },
    { label: "My Profile",  path: "/profile",             icon: UserCircle },
  ],
  TENANT: [
    { label: "Dashboard",   path: "/tenant/dashboard",    icon: Home },
    { label: "My Contracts",path: "/tenant/contracts",    icon: FileText },
    { label: "My Appeals",  path: "/tenant/appeals",      icon: AlertCircle },
    { label: "My Profile",  path: "/profile",             icon: UserCircle },
  ],
  SUBCITY_STAFF: [
    { label: "Dashboard",   path: "/officer/dashboard",   icon: Home },
    { label: "Properties",  path: "/officer/properties",  icon: Building },
    { label: "Contracts",   path: "/officer/contracts",   icon: FileText },
    { label: "Declarations",path: "/officer/declarations",icon: BarChart2 },
    { label: "Appeals",     path: "/officer/appeals",     icon: AlertCircle },
    { label: "Verify Profiles", path: "/officer/profile-verification", icon: Users },
    { label: "My Profile",  path: "/profile",             icon: UserCircle },
  ],
  ADMINISTRATOR: [
    { label: "System Settings", path: "/admin/dashboard",              icon: Settings },
    { label: "Manage Officers", path: "/admin/dashboard?tab=officers", icon: Users },
    { label: "Global Metrics",  path: "/admin/dashboard?tab=metrics",  icon: Globe },
    { label: "My Profile",  path: "/profile",             icon: UserCircle },
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

  // Derive a human-readable page title from the current path
  const currentNav = navItems.find(item =>
    location.pathname.startsWith(item.path.split("?")[0])
  );
  const pageTitle = currentNav?.label ?? "RentalPro ET";

  return (
    <div className="flex h-screen bg-gray-50">

      {/* ── Sidebar ─────────────────────────────────────────────────────────── */}
      <aside className="w-64 bg-primary text-white flex flex-col fixed h-full z-20">
        <div className="p-6 border-b border-blue-800">
          <h1 className="text-xl font-bold">RentalPro ET</h1>
          <p className="text-xs text-blue-300 mt-1">{user?.role?.replace(/_/g, " ")}</p>
        </div>

        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = location.pathname === item.path.split("?")[0] &&
              (!item.path.includes("?") || location.search === `?${item.path.split("?")[1]}`);
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm transition-all
                  ${active
                    ? "bg-white text-primary font-semibold"
                    : "text-blue-100 hover:bg-blue-800"}`}
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
            className="flex items-center gap-2 text-sm text-blue-200 hover:text-white transition"
          >
            <LogOut size={16} /> Logout
          </button>
        </div>
      </aside>

      {/* ── Main area ───────────────────────────────────────────────────────── */}
      <div className="ml-64 flex-1 flex flex-col min-h-screen">

        {/* Top header bar */}
        <header className="sticky top-0 z-10 bg-white border-b border-gray-200
          flex items-center justify-between px-8 py-3 shadow-sm">

          {/* Left: current page label */}
          <p className="text-sm font-semibold text-gray-700">{pageTitle}</p>

          {/* Right: bell + user chip */}
          <div className="flex items-center gap-3">
            <NotificationBell />

            <div className="flex items-center gap-2 pl-3 border-l border-gray-200">
              {/* Avatar initials */}
              <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center
                text-white text-xs font-bold select-none">
                {user?.firstName?.[0]}{user?.lastName?.[0]}
              </div>
              <div className="hidden sm:block leading-tight">
                <p className="text-xs font-semibold text-gray-800">
                  {user?.firstName} {user?.lastName}
                </p>
                <p className="text-[10px] text-gray-400">{user?.role?.replace(/_/g, " ")}</p>
              </div>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto">
          <div className="p-8">{children}</div>
        </main>
      </div>
    </div>
  );
}
