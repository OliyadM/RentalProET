// Tenant Contracts will go here
// Officer Dashboard will go here
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FileText, Clock, AlertCircle } from "lucide-react";
import Layout from "../../components/Layout";
import SummaryCard from "../../components/SummaryCard";
import StatusBadge from "../../components/StatusBadge";
import { useAuth } from "../../context/AuthContext";
import { contractsAPI } from "../../services/api";

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }

export default function TenantDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [contracts, setContracts] = useState([]);

  useEffect(() => {
    contractsAPI.getTenantContracts(user.id).then(setContracts);
  }, [user.id]);

  const active = contracts.filter(c => c.status === "ACTIVE").length;
  const pending = contracts.filter(c => c.status === "PENDING_CONFIRMATION");
  const appeals = contracts.filter(c => c.status === "UNDER_APPEAL").length;

  return (
    <Layout>
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Welcome, {user.firstName}</h2>
        <p className="text-gray-500 text-sm mt-1">Your rental overview</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <SummaryCard label="Active Contracts" value={active} icon={FileText} color="text-success" />
        <SummaryCard label="Pending Confirmations" value={pending.length} icon={Clock} color="text-accent" />
        <SummaryCard label="My Appeals" value={appeals} icon={AlertCircle} color="text-danger" />
      </div>

      {pending.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm mb-6">
          <div className="px-6 py-4 border-b border-gray-100 flex items-center gap-2">
            <Clock size={16} className="text-accent" />
            <h3 className="font-semibold text-gray-800">Action Required — Pending Confirmations</h3>
          </div>
          <div className="divide-y divide-gray-100">
            {pending.map(c => (
              <div key={c.id} className="px-6 py-4 flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">{c.propertyAddress}</p>
                  <p className="text-sm text-gray-500">Monthly rent: {fmt(c.monthlyRent)}</p>
                </div>
                <div className="flex gap-2">
                  <button onClick={() => navigate(`/tenant/contracts/${c.id}`)}
                    className="bg-success text-white px-4 py-2 rounded-lg text-xs font-medium hover:bg-green-700">
                    Review & Confirm
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm">
        <div className="px-6 py-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-800">All My Contracts</h3>
        </div>
        <div className="divide-y divide-gray-100">
          {contracts.map(c => (
            <div key={c.id} onClick={() => navigate(`/tenant/contracts/${c.id}`)}
              className="px-6 py-4 flex items-center justify-between hover:bg-gray-50 cursor-pointer">
              <div>
                <p className="font-medium text-gray-900">{c.propertyAddress}</p>
                <p className="text-sm text-gray-500">{c.landlordName} · {fmt(c.monthlyRent)}/mo</p>
              </div>
              <StatusBadge status={c.status} />
            </div>
          ))}
        </div>
      </div>
    </Layout>
  );
}