// Landlord Dashboard will go here
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Building, FileText, CheckCircle, Clock, Plus } from "lucide-react";
import Layout from "../../components/Layout";
import SummaryCard from "../../components/SummaryCard";
import StatusBadge from "../../components/StatusBadge";
import { useAuth } from "../../context/AuthContext";
import { propertiesAPI, contractsAPI, unitsAPI } from "../../services/api";
import { fmtDate } from "../../utils/dateUtils";

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }

export default function LandlordDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [props, setProps] = useState([]);
  const [contracts, setContracts] = useState([]);
  const [unitCount, setUnitCount] = useState(0);

  useEffect(() => {
    propertiesAPI.getMyProperties(user.id).then(setProps);
    contractsAPI.getLandlordContracts(user.id).then(setContracts);
  }, [user.id]);

  useEffect(() => {
    Promise.all(props.map(p => unitsAPI.getByProperty(p.id)))
      .then(all => setUnitCount(all.flat().length));
  }, [props]);

  const active = contracts.filter(c => c.status === "ACTIVE").length;
  const pending = contracts.filter(c => c.status === "PENDING_CONFIRMATION").length;

  return (
    <Layout>
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Welcome back, {user.firstName}</h2>
        <p className="text-gray-500 text-sm mt-1">Rental compliance overview</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <SummaryCard label="Total Properties" value={props.length} icon={Building} color="text-primary" />
        <SummaryCard label="Total Units" value={unitCount} icon={Building} color="text-primary" />
        <SummaryCard label="Active Contracts" value={active} icon={CheckCircle} color="text-success" />
        <SummaryCard label="Pending Confirmations" value={pending} icon={Clock} color="text-accent" />
      </div>

      <div className="flex gap-3 mb-8">
        <button onClick={() => navigate("/landlord/properties/add")}
          className="flex items-center gap-2 bg-primary text-white px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900 transition">
          <Plus size={16} /> Add New Property
        </button>
        <button onClick={() => navigate("/landlord/contracts")}
          className="flex items-center gap-2 border border-primary text-primary px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-50 transition">
          <FileText size={16} /> View All Contracts
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm">
        <div className="px-6 py-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-800">Recent Contracts</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="px-6 py-3 text-left">Property / Unit</th>
                <th className="px-6 py-3 text-left">Tenant</th>
                <th className="px-6 py-3 text-left">Monthly Rent</th>
                <th className="px-6 py-3 text-left">Status</th>
                <th className="px-6 py-3 text-left">End Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {contracts.slice(0, 5).map(c => (
                <tr key={c.id} onClick={() => navigate(`/landlord/contracts/${c.id}`)}
                  className="hover:bg-gray-50 cursor-pointer">
                  <td className="px-6 py-4 text-gray-800">{c.propertyAddress}</td>
                  <td className="px-6 py-4 text-gray-600">{c.tenantName}</td>
                  <td className="px-6 py-4 font-medium">{fmt(c.monthlyRent)}</td>
                  <td className="px-6 py-4"><StatusBadge status={c.status} /></td>
                  <td className="px-6 py-4 text-gray-500">{fmtDate(c.endDate)}</td>
                </tr>
              ))}
              {contracts.length === 0 && (
                <tr><td colSpan={5} className="px-6 py-8 text-center text-gray-400">No contracts yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </Layout>
  );
}