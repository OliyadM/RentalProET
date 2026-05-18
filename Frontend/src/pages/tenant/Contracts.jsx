import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import StatusBadge from "../../components/StatusBadge";
import { contractsAPI } from "../../services/api";
import { fmtDate } from "../../utils/dateUtils";

const TABS = ["ALL", "PENDING_CONFIRMATION", "ACTIVE", "DRAFT", "UNDER_APPEAL", "TERMINATED"];

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }

export default function TenantContracts() {
  const navigate = useNavigate();
  const [contracts, setContracts] = useState([]);
  const [tab, setTab] = useState("ALL");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    contractsAPI.getTenantContracts()
      .then(setContracts)
      .finally(() => setLoading(false));
  }, []);

  const filtered = tab === "ALL"
    ? contracts
    : contracts.filter(c => c.status === tab);

  return (
    <Layout>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">My Contracts</h2>
          <p className="text-gray-500 text-sm mt-1">{contracts.length} total contracts</p>
        </div>
      </div>

      {/* Status filter tabs */}
      <div className="flex gap-1 mb-5 overflow-x-auto">
        {TABS.map(t => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2 rounded-lg text-xs font-medium whitespace-nowrap transition
              ${tab === t
                ? "bg-primary text-white"
                : "bg-white text-gray-600 border border-gray-200 hover:border-primary hover:text-primary"
              }`}
          >
            {t.replace(/_/g, " ")}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        {loading ? (
          <div className="px-6 py-12 text-center text-gray-400 text-sm">Loading contracts...</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="px-6 py-3 text-left">Property / Unit</th>
                <th className="px-6 py-3 text-left">Landlord</th>
                <th className="px-6 py-3 text-left">Monthly Rent</th>
                <th className="px-6 py-3 text-left">Status</th>
                <th className="px-6 py-3 text-left">Start</th>
                <th className="px-6 py-3 text-left">End</th>
                <th className="px-6 py-3 text-left">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filtered.map(c => (
                <tr key={c.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-gray-800 max-w-[200px] truncate">
                    {c.propertyAddress}
                  </td>
                  <td className="px-6 py-4 text-gray-600">{c.landlordName}</td>
                  <td className="px-6 py-4 font-medium">{fmt(c.monthlyRent)}</td>
                  <td className="px-6 py-4">
                    <StatusBadge status={c.status} />
                  </td>
                  <td className="px-6 py-4 text-gray-500">{fmtDate(c.startDate)}</td>
                  <td className="px-6 py-4 text-gray-500">{fmtDate(c.endDate)}</td>
                  <td className="px-6 py-4">
                    <button
                      onClick={() => navigate(`/tenant/contracts/${c.id}`)}
                      className="text-primary text-xs font-medium hover:underline"
                    >
                      {c.status === "PENDING_CONFIRMATION" ? "Review & Sign" : "View"}
                    </button>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-10 text-center text-gray-400">
                    No {tab === "ALL" ? "" : tab.replace(/_/g, " ").toLowerCase() + " "}contracts found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
