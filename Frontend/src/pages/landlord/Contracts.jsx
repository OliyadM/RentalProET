// Landlord Contracts will go here
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus } from "lucide-react";
import Layout from "../../components/Layout";
import StatusBadge from "../../components/StatusBadge";
import { useAuth } from "../../context/AuthContext";
import { contractsAPI } from "../../services/api";

const TABS = ["ALL","DRAFT","PENDING_CONFIRMATION","ACTIVE","UNDER_APPEAL","TERMINATED"];
function fmt(n) { return "ETB " + Number(n).toLocaleString(); }
function fmtDate(d) { const [y,m,day] = d.split("-"); return `${day}/${m}/${y}`; }

export default function LandlordContracts() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [contracts, setContracts] = useState([]);
  const [tab, setTab] = useState("ALL");

  useEffect(() => {
    contractsAPI.getLandlordContracts().then(setContracts);
  }, []);

  const filtered = tab === "ALL" ? contracts : contracts.filter(c => c.status === tab);

  return (
    <Layout>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Contracts</h2>
          <p className="text-gray-500 text-sm mt-1">{contracts.length} total contracts</p>
        </div>
        <button onClick={() => navigate("/landlord/contracts/create")}
          className="flex items-center gap-2 bg-primary text-white px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900 transition">
          <Plus size={16} /> Create Contract
        </button>
      </div>

      <div className="flex gap-1 mb-5 overflow-x-auto">
        {TABS.map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-2 rounded-lg text-xs font-medium whitespace-nowrap transition
              ${tab === t ? "bg-primary text-white" : "bg-white text-gray-600 border border-gray-200 hover:border-primary hover:text-primary"}`}>
            {t.replace(/_/g, " ")}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
            <tr>
              <th className="px-6 py-3 text-left">Property / Unit</th>
              <th className="px-6 py-3 text-left">Tenant</th>
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
                <td className="px-6 py-4 text-gray-800 max-w-[200px] truncate">{c.propertyAddress}</td>
                <td className="px-6 py-4 text-gray-600">{c.tenantName}</td>
                <td className="px-6 py-4 font-medium">{fmt(c.monthlyRent)}</td>
                <td className="px-6 py-4"><StatusBadge status={c.status} /></td>
                <td className="px-6 py-4 text-gray-500">{fmtDate(c.startDate)}</td>
                <td className="px-6 py-4 text-gray-500">{fmtDate(c.endDate)}</td>
                <td className="px-6 py-4">
                  <button onClick={() => navigate(`/landlord/contracts/${c.id}`)}
                    className="text-primary text-xs font-medium hover:underline">View</button>
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr><td colSpan={7} className="px-6 py-10 text-center text-gray-400">No contracts found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}