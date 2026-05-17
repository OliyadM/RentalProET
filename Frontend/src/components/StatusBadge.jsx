// StatusBadge component will go here
export default function StatusBadge({ status }) {
  const styles = {
    ACTIVE: "bg-green-100 text-green-800",
    CONFIRMED: "bg-green-100 text-green-800",
    DRAFT: "bg-gray-100 text-gray-800",
    PENDING_CONFIRMATION: "bg-yellow-100 text-yellow-800",
    UNDER_APPEAL: "bg-orange-100 text-orange-800",
    UNDER_REVIEW: "bg-blue-100 text-blue-800",
    REJECTED: "bg-red-100 text-red-800",
    TERMINATED: "bg-red-100 text-red-800",
    EXPIRED: "bg-gray-100 text-gray-800",
    PENDING: "bg-yellow-100 text-yellow-800",
    RESOLVED: "bg-green-100 text-green-800",
    LOW: "bg-yellow-100 text-yellow-800",
    MEDIUM: "bg-orange-100 text-orange-800",
    HIGH: "bg-red-100 text-red-800",
    STABLE: "bg-blue-100 text-blue-800",
  };
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${styles[status] || "bg-gray-100 text-gray-800"}`}>
      {status?.replace(/_/g, " ")}
    </span>
  );
}