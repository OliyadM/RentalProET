// Officer Declarations will go here
import { useEffect, useState } from "react";
import { AlertTriangle } from "lucide-react";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import Modal from "../../components/Modal";
import { declarationsAPI } from "../../services/api";
import { useAuth } from "../../context/AuthContext";
import { fmtDate } from "../../utils/dateUtils";

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }
function fmtPct(n) { return `${(Number(n || 0) * 100).toFixed(2)}%`; }
function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export default function OfficerDeclarations() {
  const { user } = useAuth();
  const subCity = user?.subCityZone || "";

  const [declarations, setDeclarations] = useState([]);
  const [tab, setTab] = useState("ANOMALIES");
  const [toast, setToast] = useState(null);
  const [verifying, setVerifying] = useState(null);
  const [notes, setNotes] = useState("");
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    if (!subCity) return;
    if (tab === "ANOMALIES") declarationsAPI.getAnomalies(subCity).then(setDeclarations);
    else declarationsAPI.getUnverified(subCity).then(setDeclarations);
  }, [tab, subCity]);

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

  const exportCompliancePdf = async () => {
    if (!subCity) return;
    setExporting(true);
    try {
      const filter = tab === "ANOMALIES" ? "ANOMALIES" : "UNVERIFIED";
      const blob = await declarationsAPI.downloadComplianceReportPdf(subCity, filter);
      const filename = `compliance-report-${subCity}-${filter.toLowerCase()}.pdf`;
      downloadBlob(blob, filename);
    } catch (e) {
      setToast(e.message || "Failed to export compliance report PDF");
    } finally {
      setExporting(false);
    }
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
        <p className="text-gray-500 text-sm mt-1">
          {subCity
            ? <>Showing data for <span className="font-medium text-primary">{subCity}</span></>
            : <span className="text-accent">No sub-city assigned — contact an administrator</span>}
        </p>
        <p className="text-xs text-gray-400 mt-1">
          Tax values are advisory estimates based on rule version shown in each row.
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-2 mb-5">
        {["ANOMALIES","UNVERIFIED"].map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-5 py-2 rounded-lg text-sm font-medium transition
              ${tab === t ? "bg-primary text-white" : "bg-white border border-gray-200 text-gray-600 hover:border-primary hover:text-primary"}`}>
            {t === "ANOMALIES" ? "Anomaly Flags" : "Unverified"}
          </button>
        ))}
        <button
          onClick={exportCompliancePdf}
          disabled={exporting || !subCity}
          className="ml-auto px-4 py-2 rounded-lg text-sm font-medium border border-primary text-primary hover:bg-blue-50 disabled:opacity-50"
        >
          {exporting ? "Exporting..." : "Export Compliance PDF"}
        </button>
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
              <th className="px-6 py-3 text-left">Tax / Month</th>
              <th className="px-6 py-3 text-left">Tax / Year</th>
              <th className="px-6 py-3 text-left">Tax Rule</th>
              <th className="px-6 py-3 text-left">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {declarations.map(d => {
              const dev = d.aiBenchmarkRent
                ? (((d.declaredRent - d.aiBenchmarkRent) / d.aiBenchmarkRent) * 100).toFixed(1)
                : "0.0";
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
                  <td className="px-6 py-4 font-medium text-primary">{fmt(d.estimatedTax)}</td>
                  <td className="px-6 py-4">{d.annualTax != null ? fmt(d.annualTax) : "N/A"}</td>
                  <td className="px-6 py-4">
                    <p className="text-xs font-medium text-gray-700">{d.taxRuleVersion || "N/A"}</p>
                    {d.effectiveTaxRate != null && (
                      <p className="text-xs text-gray-500">Rate: {fmtPct(d.effectiveTaxRate)}</p>
                    )}
                    {d.mixedUseDeductionWarning && (
                      <p className="text-xs text-yellow-700 mt-1">Mixed-use split check required</p>
                    )}
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
              <tr><td colSpan={10} className="px-6 py-10 text-center text-gray-400">No items found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}