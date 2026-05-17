// Tenant Appeals will go here
import { useEffect, useState } from "react";
import { Plus, AlertCircle } from "lucide-react";
import Layout from "../../components/Layout";
import StatusBadge from "../../components/StatusBadge";
import Modal from "../../components/Modal";
import Toast from "../../components/Toast";
import { useAuth } from "../../context/AuthContext";
import { appealsAPI, contractsAPI } from "../../services/api";

function fmtDate(d) { if (!d) return "—"; const [y,m,day] = d.split("-"); return `${day}/${m}/${y}`; }

export default function TenantAppeals() {
  const { user } = useAuth();
  const [appeals, setAppeals] = useState([]);
  const [contracts, setContracts] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [toast, setToast] = useState(null);
  const [form, setForm] = useState({ contractId: "", appealType: "RENT_INCREASE", reason: "", evidenceUrls: "" });

  useEffect(() => {
    appealsAPI.getMyAppeals().then(setAppeals);
    contractsAPI.getTenantContracts().then(cs =>
      setContracts(cs.filter(c => c.status === "ACTIVE" || c.status === "UNDER_APPEAL"))
    );
  }, []);

  const set = f => e => setForm({ ...form, [f]: e.target.value });

  const submit = async () => {
    try {
      const appealData = {
        contractId: form.contractId,
        appealType: form.appealType,
        reason: form.reason,
        evidenceDocuments: form.evidenceUrls || null
      };
      const appeal = await appealsAPI.create(appealData);
      setAppeals(prev => [appeal, ...prev]);
      setShowModal(false);
      setForm({ contractId: "", appealType: "RENT_INCREASE", reason: "", evidenceUrls: "" });
      setToast("Appeal submitted successfully");
    } catch (error) {
      setToast("Failed to submit appeal");
    }
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}
      {showModal && (
        <Modal title="Submit New Appeal" onClose={() => setShowModal(false)}>
          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Contract *</label>
              <select value={form.contractId} onChange={set("contractId")} required
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
                <option value="">Select contract</option>
                {contracts.map(c => <option key={c.id} value={c.id}>{c.propertyAddress}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Appeal Type *</label>
              <select value={form.appealType} onChange={set("appealType")}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
                <option value="RENT_INCREASE">Rent Increase</option>
                <option value="EVICTION">Eviction</option>
                <option value="CONTRACT_TERMINATION">Contract Termination</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Reason *</label>
              <textarea value={form.reason} onChange={set("reason")} rows={4} maxLength={2000}
                placeholder="Describe the reason for your appeal..."
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary resize-none" />
              <p className="text-xs text-gray-400 mt-1">{form.reason.length}/2000</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Evidence (optional)</label>
              <input value={form.evidenceUrls} onChange={set("evidenceUrls")}
                placeholder="URLs or references — file upload coming soon"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div className="flex gap-3 pt-1">
              <button onClick={() => setShowModal(false)}
                className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">Cancel</button>
              <button onClick={submit}
                className="flex-1 bg-primary text-white py-2 rounded-lg text-sm font-medium hover:bg-blue-900">Submit Appeal</button>
            </div>
          </div>
        </Modal>
      )}

      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">My Appeals</h2>
          <p className="text-gray-500 text-sm mt-1">{appeals.length} total appeals</p>
        </div>
        <button onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-primary text-white px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900">
          <Plus size={16} /> Submit Appeal
        </button>
      </div>

      <div className="grid gap-4">
        {appeals.map(a => (
          <div key={a.id} className="bg-white rounded-xl shadow-sm p-5">
            <div className="flex justify-between items-start mb-3">
              <div className="flex items-center gap-2">
                <AlertCircle size={16} className="text-accent" />
                <span className="font-semibold text-gray-800">{a.appealType.replace(/_/g, " ")}</span>
              </div>
              <StatusBadge status={a.status} />
            </div>
            <p className="text-sm text-gray-600 mb-3 line-clamp-3">{a.reason}</p>
            <p className="text-xs text-gray-400 mb-3">Submitted: {fmtDate(a.createdAt)}</p>
            {a.resolutionNotes && (
              <div className="p-3 bg-green-50 border border-green-200 rounded-lg">
                <p className="text-xs font-medium text-success mb-1">Resolution</p>
                <p className="text-sm text-gray-700">{a.resolutionDecision}</p>
                <p className="text-xs text-gray-500 mt-1">{a.resolutionNotes}</p>
              </div>
            )}
          </div>
        ))}
        {appeals.length === 0 && (
          <div className="text-center py-16 text-gray-400">
            <AlertCircle size={40} className="mx-auto mb-3 opacity-30" />
            <p>No appeals submitted yet</p>
          </div>
        )}
      </div>
    </Layout>
  );
}