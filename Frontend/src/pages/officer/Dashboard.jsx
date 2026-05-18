import { useEffect, useState } from "react";
import { AlertCircle, Building, BarChart2, FileText } from "lucide-react";
import Layout from "../../components/Layout";
import SummaryCard from "../../components/SummaryCard";
import StatusBadge from "../../components/StatusBadge";
import { contractsAPI, appealsAPI, declarationsAPI, propertiesAPI } from "../../services/api";
import { useAuth } from "../../context/AuthContext";
import { fmtDate } from "../../utils/dateUtils";

export default function OfficerDashboard() {
  const { user } = useAuth();
  const subCity = user?.subCityZone || "";

  const [pendingAppeals, setPendingAppeals] = useState([]);
  const [unverified, setUnverified] = useState([]);
  const [unverifiedProps, setUnverifiedProps] = useState([]);
  const [activeContracts, setActiveContracts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!subCity) return;

    Promise.all([
      appealsAPI.getPending(),
      declarationsAPI.getUnverified(subCity),
      propertiesAPI.getBySubCity(subCity),
      contractsAPI.getByStatus("ACTIVE"),
    ])
      .then(([appeals, decls, props, contracts]) => {
        setPendingAppeals(appeals);
        setUnverified(decls);
        setUnverifiedProps(props.filter(p => !p.isVerified));
        setActiveContracts(contracts);
      })
      .finally(() => setLoading(false));
  }, [subCity]);

  // Build a unified recent-activity feed from real data
  const recentActivity = [
    ...pendingAppeals.slice(0, 3).map(a => ({
      key: `appeal-${a.id}`,
      text: `Appeal submitted — ${a.tenantName} · ${a.appealType.replace(/_/g, " ")}`,
      date: a.createdAt,
    })),
    ...unverified.slice(0, 3).map(d => ({
      key: `decl-${d.id}`,
      text: `Declaration pending verification — Contract ${d.contractId}`,
      date: d.createdAt,
    })),
    ...unverifiedProps.slice(0, 2).map(p => ({
      key: `prop-${p.id}`,
      text: `Property awaiting verification — ${p.propertyName}, ${p.subCity}`,
      date: p.createdAt,
    })),
  ]
    .filter(item => item.date)
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .slice(0, 6);



  return (
    <Layout>
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Officer Dashboard</h2>
        <p className="text-gray-500 text-sm mt-1">
          Sub-city compliance overview —{" "}
          {subCity
            ? <span className="font-medium text-primary">{subCity}</span>
            : <span className="text-accent">No sub-city assigned</span>}
        </p>
      </div>

      {!subCity && (
        <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 text-sm rounded-lg px-4 py-3 mb-6">
          Your account has no sub-city zone assigned. Contact an administrator to update your profile.
        </div>
      )}

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <SummaryCard label="Pending Appeals" value={pendingAppeals.length} icon={AlertCircle} color={pendingAppeals.length > 0 ? "text-danger" : "text-gray-500"} />
        <SummaryCard label="Unverified Declarations" value={unverified.length} icon={BarChart2} color="text-accent" />
        <SummaryCard label="Properties to Verify" value={unverifiedProps.length} icon={Building} color="text-accent" />
        <SummaryCard label="Active Contracts" value={activeContracts.length} icon={FileText} color="text-success" />
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
        {loading ? (
          <div className="px-6 py-8 text-center text-gray-400 text-sm">Loading activity...</div>
        ) : recentActivity.length > 0 ? (
          <div className="divide-y divide-gray-100 text-sm">
            {recentActivity.map(item => (
              <div key={item.key} className="px-6 py-3 flex justify-between items-center text-gray-600">
                <span>{item.text}</span>
                <span className="text-xs text-gray-400 ml-4 whitespace-nowrap">{fmtDate(item.date)}</span>
              </div>
            ))}
          </div>
        ) : (
          <div className="px-6 py-8 text-center text-gray-400 text-sm">
            No recent activity in {subCity || "your sub-city"}
          </div>
        )}
      </div>
    </Layout>
  );
}