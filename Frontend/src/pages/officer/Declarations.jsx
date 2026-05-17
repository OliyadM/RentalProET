// Officer Declarations will go here
import { useEffect, useState } from "react";
import { AlertTriangle } from "lucide-react";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import Modal from "../../components/Modal";
import { declarationsAPI } from "../../services/api";

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }
function fmtDate(d) { if (!d) return "—"; const [y,m,day] = d.split("-"); return `${day}/${m}/${y}`; }

export default function OfficerDeclarations() {
  const [declarations, setDeclarations] = useState([]);
  const [tab, setTab] = useState("ANOMALIES");
  const [toast, setToast] = useState(null);
  const [verifying, setVerifying] = useState(null);
  const [notes, setNotes] = useState("");

  useEffect(() => {
    if (tab === "ANOMALIES") declarationsAPI.getAnomalies().then(setDeclarations);
    else declarationsAPI.getUnverified().then(setDeclarations);
  }, [tab]);

  const verify = async () => {
    await declarationsAPI.verify(verifying.id, notes);
    setDeclarations(prev => prev.map(d => d.id === verifying.id ? { ...d, isVerified: true } : d));
    setVerifying(null);
    setNotes("");
    setToast("Declaration verified");
  };

  const getAnomalyBadge = (d) => {
    if (!d.isAnomaly) return null;
    const pct = Math.abs(((d.declaredRent - d.aiBenchmarkRent) / d.aiBenchmarkRent) * 100);
    const level = pct > 40 ? "HIGH" : pct > 20 ? "MEDIUM" : "LOW";
    const colors = { HIGH: "bg-red-100 text-red-800", MEDIUM: "bg-orange-100 text-orange-800", LOW: "bg-yellow-100 text-yellow-800" };
    return <span className={`text-xs font-semibold px-2 py-1 rounded-full ${colors[level]}`}>{level}</span>;
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}
      {verifying && (
        <Modal title="Verify Declaration" onClose={() => setVerifying(null)}>
          <p className="text-sm text-gray-600 mb-3">Add verification notes (optional)</p>
          <textarea value={notes} onChange={e => setNotes(e.target.value)} rows={3}
            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary resize-none mb-4" />
          <div className="flex gap-3">
            <button onClick={() => setVerifying(null)}
              className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">Cancel</button>
            <button onClick={verify}
              className="flex-1 bg-success text-white py-2 rounded-lg text-sm font-medium">Confirm Verification</button>
          </div>
        </Modal>
      )}

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Declarations</h2>
      </div>

      <div className="flex gap-2 mb-5">
        {["ANOMALIES","UNVERIFIED"].map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-5 py-2 rounded-lg text-sm font-medium transition
              ${tab === t ? "bg-primary text-white" : "bg-white border border-gray-200 text-gray-600 hover:border-primary hover:text-primary"}`}>
            {t === "ANOMALIES" ? "Anomaly Flags" : "Unverified"}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
            <tr>
              <th className="px-6 py-3 text-left">Contract</th>
              <th className="px-6 py-3 text-left">Period</th>
              <th className="px-6 py-3 text-left">Declared</th>
              <th className="px-6 py-3 text-left">Benchmark</th>
              <th className="px-6 py-3 text-left">Deviation</th>
              <th className="px-6 py-3 text-left">Anomaly</th>
              <th className="px-6 py-3 text-left">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {declarations.map(d => {
              const dev = (((d.declaredRent - d.aiBenchmarkRent) / d.aiBenchmarkRent) * 100).toFixed(1);
              return (
                <tr key={d.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-gray-700">{d.contractId}</td>
                  <td className="px-6 py-4">{fmtDate(d.declarationPeriod)}</td>
                  <td className="px-6 py-4 font-medium">{fmt(d.declaredRent)}</td>
                  <td className="px-6 py-4 text-gray-500">{fmt(d.aiBenchmarkRent)}</td>
                  <td className="px-6 py-4">
                    <span className={`font-medium ${Math.abs(dev) > 15 ? "text-danger" : "text-accent"}`}>
                      {dev > 0 ? "+" : ""}{dev}%
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    {d.isAnomaly ? getAnomalyBadge(d) : <span className="text-success text-xs">Clean</span>}
                  </td>
                  <td className="px-6 py-4">
                    {!d.isVerified && (
                      <button onClick={() => setVerifying(d)}
                        className="bg-primary text-white px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-blue-900">
                        Verify
                      </button>
                    )}
                    {d.isVerified && <span className="text-success text-xs font-medium">Verified</span>}
                  </td>
                </tr>
              );
            })}
            {declarations.length === 0 && (
              <tr><td colSpan={7} className="px-6 py-10 text-center text-gray-400">No items found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}