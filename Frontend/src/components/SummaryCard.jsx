// SummaryCard component will go here
export default function SummaryCard({ label, value, icon: Icon, color = "text-primary" }) {
  return (
    <div className="bg-white rounded-xl shadow-sm p-5 flex items-center gap-4">
      {Icon && (
        <div className={`p-3 rounded-full bg-gray-100 ${color}`}>
          <Icon size={22} />
        </div>
      )}
      <div>
        <p className="text-sm text-gray-500">{label}</p>
        <p className={`text-2xl font-bold ${color}`}>{value}</p>
      </div>
    </div>
  );
}