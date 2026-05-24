import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, Check, CheckCheck, X } from "lucide-react";
import { notificationsAPI } from "../services/api";
import { useAuth } from "../context/AuthContext";

const POLL_INTERVAL_MS = 30_000; // 30 seconds

// ── Relative time helper ──────────────────────────────────────────────────────
function relativeTime(isoString) {
  if (!isoString) return "";
  const diff = Date.now() - new Date(isoString).getTime();
  const mins = Math.floor(diff / 60_000);
  if (mins < 1)  return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)  return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  return `${days}d ago`;
}

// ── Route resolver — maps NotificationType + user role → navigation path ─────
function resolveNavPath(notification, userRole) {
  const { type, relatedEntityId } = notification;
  if (!relatedEntityId) return null;

  const contractTypes = [
    "CONTRACT_CONFIRMED",
    "CONTRACT_REJECTED",
    "CONTRACT_SUBMITTED",
  ];
  const appealTypes = [
    "APPEAL_SUBMITTED",
    "APPEAL_RESOLVED",
    "APPEAL_REJECTED",
  ];

  if (contractTypes.includes(type)) {
    if (userRole === "LANDLORD")    return `/landlord/contracts/${relatedEntityId}`;
    if (userRole === "TENANT")      return `/tenant/contracts/${relatedEntityId}`;
  }

  if (appealTypes.includes(type)) {
    if (userRole === "TENANT")                                    return `/tenant/appeals`;
    if (userRole === "SUBCITY_STAFF" || userRole === "ADMINISTRATOR") return `/officer/appeals`;
  }

  if (type === "PROPERTY_VERIFIED") {
    return `/landlord/properties/${relatedEntityId}`;
  }

  // Account lifecycle types
  if (type === "PROFILE_PENDING_REVIEW") {
    // Officers navigate to the pending profiles list
    if (userRole === "SUBCITY_STAFF" || userRole === "ADMINISTRATOR") {
      return "/officer/profile-verification";
    }
    return null;
  }

  // ACCOUNT_CREATED, ACCOUNT_VERIFIED, ACCOUNT_REJECTED — informational,
  // navigate to the user's own profile page when that route exists
  if (["ACCOUNT_CREATED", "ACCOUNT_VERIFIED", "ACCOUNT_REJECTED"].includes(type)) {
    return null; // update to "/profile" once the profile page is built
  }

  return null;
}

// ── Single notification row ───────────────────────────────────────────────────
function NotificationRow({ notification, onMarkRead, onNavigate }) {
  const isUnread = !notification.isRead;

  return (
    <div
      className={`flex items-start gap-3 px-4 py-3 hover:bg-gray-50 transition cursor-pointer
        ${isUnread ? "border-l-2 border-primary bg-blue-50/40" : "border-l-2 border-transparent"}`}
      onClick={() => onNavigate(notification)}
    >
      {/* Unread dot */}
      <div className="mt-1.5 flex-shrink-0">
        {isUnread
          ? <span className="block w-2 h-2 rounded-full bg-primary" />
          : <span className="block w-2 h-2 rounded-full bg-transparent" />}
      </div>

      {/* Message + time */}
      <div className="flex-1 min-w-0">
        <p className={`text-sm leading-snug ${isUnread ? "font-medium text-gray-900" : "text-gray-500"}`}>
          {notification.message}
        </p>
        <p className="text-xs text-gray-400 mt-0.5">{relativeTime(notification.createdAt)}</p>
      </div>

      {/* Mark-as-read button — only shown for unread items */}
      {isUnread && (
        <button
          onClick={(e) => { e.stopPropagation(); onMarkRead(notification.id); }}
          title="Mark as read"
          className="flex-shrink-0 p-1 rounded hover:bg-blue-100 text-primary transition"
        >
          <Check size={14} />
        </button>
      )}
    </div>
  );
}

// ── Main component ────────────────────────────────────────────────────────────
export default function NotificationBell() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);

  const unreadCount = notifications.filter(n => !n.isRead).length;

  // ── Fetch ───────────────────────────────────────────────────────────────────
  const fetchNotifications = async () => {
    try {
      const data = await notificationsAPI.getMyNotifications();
      setNotifications(data);
    } catch {
      // Silently fail — bell should never crash the whole layout
    }
  };

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, []);

  // ── Close on outside click ──────────────────────────────────────────────────
  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    if (open) document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  // ── Actions ─────────────────────────────────────────────────────────────────
  const handleMarkOne = async (id) => {
    try {
      await notificationsAPI.markAsRead(id);
      setNotifications(prev =>
        prev.map(n => n.id === id ? { ...n, isRead: true } : n)
      );
    } catch {
      // ignore
    }
  };

  const handleMarkAll = async () => {
    setLoading(true);
    try {
      await notificationsAPI.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  const handleNavigate = (notification) => {
    // Mark as read on click
    if (!notification.isRead) handleMarkOne(notification.id);

    const path = resolveNavPath(notification, user?.role);
    if (path) {
      setOpen(false);
      navigate(path);
    }
  };

  // ── Render ──────────────────────────────────────────────────────────────────
  return (
    <div className="relative" ref={dropdownRef}>
      {/* Bell button */}
      <button
        onClick={() => setOpen(prev => !prev)}
        className="relative p-2 rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 transition"
        aria-label="Notifications"
      >
        <Bell size={20} />

        {/* Unread badge */}
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] px-1
            flex items-center justify-center
            bg-red-500 text-white text-[10px] font-bold rounded-full leading-none">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown */}
      {open && (
        <div className="absolute right-0 top-full mt-2 w-80 bg-white rounded-xl shadow-xl
          border border-gray-100 z-50 overflow-hidden">

          {/* Header */}
          <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
            <div className="flex items-center gap-2">
              <Bell size={15} className="text-primary" />
              <span className="text-sm font-semibold text-gray-800">Notifications</span>
              {unreadCount > 0 && (
                <span className="text-xs bg-primary text-white px-1.5 py-0.5 rounded-full font-medium">
                  {unreadCount} new
                </span>
              )}
            </div>
            <div className="flex items-center gap-1">
              {unreadCount > 0 && (
                <button
                  onClick={handleMarkAll}
                  disabled={loading}
                  title="Mark all as read"
                  className="flex items-center gap-1 text-xs text-primary hover:text-blue-800
                    px-2 py-1 rounded hover:bg-blue-50 transition disabled:opacity-50"
                >
                  <CheckCheck size={13} />
                  {loading ? "..." : "Mark all read"}
                </button>
              )}
              <button
                onClick={() => setOpen(false)}
                className="p-1 rounded hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition"
              >
                <X size={14} />
              </button>
            </div>
          </div>

          {/* Notification list */}
          <div className="max-h-80 overflow-y-auto divide-y divide-gray-50">
            {notifications.length === 0 ? (
              <div className="px-4 py-10 text-center">
                <Bell size={28} className="mx-auto mb-2 text-gray-300" />
                <p className="text-sm text-gray-400">No notifications yet</p>
              </div>
            ) : (
              notifications.map(n => (
                <NotificationRow
                  key={n.id}
                  notification={n}
                  onMarkRead={handleMarkOne}
                  onNavigate={handleNavigate}
                />
              ))
            )}
          </div>

          {/* Footer */}
          {notifications.length > 0 && (
            <div className="px-4 py-2.5 border-t border-gray-100 bg-gray-50 text-center">
              <p className="text-xs text-gray-400">
                {notifications.length} notification{notifications.length !== 1 ? "s" : ""} total
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
