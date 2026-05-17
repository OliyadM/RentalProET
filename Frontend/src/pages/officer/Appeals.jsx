// Officer Appeals will go here
import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import StatusBadge from "../../components/StatusBadge";
import Modal from "../../components/Modal";
import Toast from "../../components/Toast";
import { appealsAPI } from "../../services/api";

function fmtDate(d) { if (!d) return "—"; const [y,m,day] = d.split("-"); return `${day}/${m}/${y}`; }

export default function OfficerAppeals() {
  const [appeals, setAppeals] = useState([]);
  const [tab, setTab] = useState("PENDING");
  const [toast, setToast] = useState(null);
  const [active, setActive] = useState(null);
  const [mode, setMode] = useState(null); // "resolve" | "reject"
  const [text, setText] = useState("");

  useEffect(() => {
    appealsAPI.getAll().then(setAppeals);
  }, []);

  const filtered = appeals.filter(a => a.status === tab);

  const resolve = async () => {
    await appealsAPI.resolve(active.id, "APPROVED", text);
    setAppeals(prev => prev.map(a => a.id === active.id ? { ...a, status: "RESOLVED", resolutionDecision: "APPROVED", resolutionNotes: text } : a));
    setActive(null); setText("");
    setToast("Appeal resolved");
  };

  const reject = async () => {
    await appealsAPI.reject(active.id, text);
    setAppeals(prev => prev.map(a => a.id === active.id ? { ...a, status: "REJECTED", resolutionNotes: text } : a));
    setActive(null); setText("");
    setToast("Appeal rejected");
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}
      {active && (
        <Modal title={mode === "resolve" ? "Resolve Appeal" : "Reject Appeal"} onClose={() => { setActive(null); setText(""); }}>
          <p className="text-sm text-gray-600 mb-3">
            {mode === "resolve" ? "Enter resolution decision and notes:" : "Enter rejection reason:"}
          </p>
          <textarea value={text} onChange={e => setText(e.target.value)} rows={4}
            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary resize-none mb-4" />
          <div className="flex gap-3">
            <button onClick={() => { setActive(null); setText(""); }}
              className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">Cancel</button>
            <button onClick={mode === "resolve" ? resolve : reject}
              className={`flex-1 text-white py-2 rounded-lg text-sm font-medium ${mode === "resolve" ? "bg-success hover:bg-green-700" : "bg-danger hover:bg-red-700"}`}>
              Confirm
            </button>
          </div>
        </Modal>
      )}

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Appeals</h2>
        <p className="text-gray-500 text-sm mt-1">{filtered.length} {tab.toLowerCase()}</p>
      </div>

      <div className="flex gap-2 mb-5">
        {["PENDING","RESOLVED","REJECTED"].map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-5 py-2 rounded-lg text-sm font-medium transition
              ${tab === t ? "bg-primary text-white" : "bg-white border border-gray-200 text-gray-600 hover:border-primary hover:text-primary"}`}>
            {t}
          </button>
        ))}
      </div>

      <div className="grid gap-4">
        {filtered.map(a => (
          <div key={a.id} className="bg-white rounded-xl shadow-sm p-5">
            <div className="flex justify-between items-start mb-3">
              <div>
                <p className="font-semibold text-gray-900">{a.tenantName}</p>
                <p className="text-xs text-gray-500">Contract: {a.contractId} · Submitted: {fmtDate(a.createdAt)}</p>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs bg-blue-50 text-primary px-2 py-1 rounded-full font-medium">
                  {a.appealType.replace(/_/g," ")}
                </span>
                <StatusBadge status={a.status} />
              </div>
            </div>
            <p className="text-sm text-gray-600 mb-4">{a.reason}</p>
            {a.resolutionNotes && (
              <div className="p-3 bg-gray-50 rounded-lg mb-3">
                <p className="text-xs text-gray-500 mb-1">Resolution</p>
                <p className="text-sm text-gray-700">{a.resolutionDecision || a.resolutionNotes}</p>
              </div>
            )}
            {a.status === "PENDING" && (
              <div className="flex gap-2">
                <button onClick={() => { setActive(a); setMode("resolve"); }}
                  className="bg-success text-white px-4 py-2 rounded-lg text-xs font-medium hover:bg-green-700">
                  Resolve
                </button>
                <button onClick={() => { setActive(a); setMode("reject"); }}
                  className="bg-danger text-white px-4 py-2 rounded-lg text-xs font-medium hover:bg-red-700">
                  Reject
                </button>
              </div>
            )}
          </div>
        ))}
        {filtered.length === 0 && (
          <div className="text-center py-16 text-gray-400">No {tab.toLowerCase()} appeals</div>
        )}
      </div>
    </Layout>
  );
}