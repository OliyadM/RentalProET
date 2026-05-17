import { useEffect, useState } from "react";
import { AlertCircle, Building, BarChart2, FileText } from "lucide-react";
import Layout from "../../components/Layout";
import SummaryCard from "../../components/SummaryCard";
import { contractsAPI, appealsAPI, declarationsAPI, propertiesAPI } from "../../services/api";

export default function OfficerDashboard() {
  const [pending, setPending] = useState([]);
  const [unverified, setUnverified] = useState([]);
  const [props, setProps] = useState([]);
  const [contracts, setContracts] = useState([]);

  useEffect(() => {
    appealsAPI.getPending().then(setPending);
    declarationsAPI.getUnverified().then(setUnverified);
    propertiesAPI.getAll().then(ps => setProps(ps.filter(p => !p.isVerified)));
    contractsAPI.getAll().then(setContracts);
  }, []);

  const active = contracts.filter(c => c.status === "ACTIVE").length;

  return (
    <Layout>
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Officer Dashboard</h2>
        <p className="text-gray-500 text-sm mt-1">Sub-city compliance overview</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <SummaryCard label="Pending Appeals" value={pending.length} icon={AlertCircle} color={pending.length > 0 ? "text-danger" : "text-gray-500"} />
        <SummaryCard label="Unverified Declarations" value={unverified.length} icon={BarChart2} color="text-accent" />
        <SummaryCard label="Properties to Verify" value={props.length} icon={Building} color="text-accent" />
        <SummaryCard label="Active Contracts" value={active} icon={FileText} color="text-success" />
      </div>

      <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-5 mb-6">
        <div className="flex items-center gap-2 mb-2">
          <div className="w-3 h-3 rounded-full bg-yellow-400" />
          <span className="text-sm font-medium text-yellow-800">GIS Spatial Heatmap</span>
        </div>
        <div className="h-40 bg-white rounded-lg border border-dashed border-yellow-300 flex items-center justify-center">
          <div className="text-center text-gray-400">
            <BarChart2 size={32} className="mx-auto mb-2 opacity-40" />
            <p className="text-sm">GIS Spatial Heatmap — Integration in progress</p>
            <p className="text-xs mt-1">Rent density and anomaly frequency by sub-city</p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm">
        <div className="px-6 py-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-800">Recent Activity</h3>
        </div>
        <div className="divide-y divide-gray-100 text-sm">
          <div className="px-6 py-3 text-gray-600">Appeal submitted — Tigist Alemu · Rent Increase · 15/03/2024</div>
          <div className="px-6 py-3 text-gray-600">Declaration flagged — Unit B-001, Kirkos · HIGH anomaly</div>
          <div className="px-6 py-3 text-gray-600">Property verified — Yeka Residence, Mulugeta Tadesse</div>
          <div className="px-6 py-3 text-gray-600">Contract confirmed — Tigist Alemu · Bole Road Unit A-101</div>
        </div>
      </div>
    </Layout>
  );
}