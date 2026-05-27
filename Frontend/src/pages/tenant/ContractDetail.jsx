// Tenant Contract Detail will go here
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { FileText, AlertTriangle } from "lucide-react";
import Layout from "../../components/Layout";
import StatusBadge from "../../components/StatusBadge";
import Toast from "../../components/Toast";
import Modal from "../../components/Modal";
import { contractsAPI, declarationsAPI } from "../../services/api";
import { fmtDate } from "../../utils/dateUtils";

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }

export default function TenantContractDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [contract, setContract] = useState(null);
  const [declarations, setDeclarations] = useState([]);
  const [toast, setToast] = useState(null);
  const [showReject, setShowReject] = useState(false);
  const [rejReason, setRejReason] = useState("");
  const [signature, setSignature] = useState("");

  useEffect(() => {
    contractsAPI.getById(id).then(setContract);
    declarationsAPI.getByContract(id).then(setDeclarations);
  }, [id]);

  const confirm = async () => {
    if (!signature.trim()) {
      setToast({ msg: "Please type your full name as signature", type: "error" });
      return;
    }
    try {
      const updated = await contractsAPI.confirm(id, signature);
      setContract(updated);
      setToast({ msg: "Contract confirmed successfully", type: "success" });
    } catch (err) {
      const msg = err.response?.data?.message || "Failed to confirm contract. Please try again.";
      setToast({ msg, type: "error" });
    }
  };

  const reject = async () => {
    if (!rejReason.trim()) {
      setToast({ msg: "Please provide a rejection reason", type: "error" });
      return;
    }
    try {
      const updated = await contractsAPI.reject(id, rejReason);
      setContract(updated);
      setShowReject(false);
      setToast({ msg: "Contract rejected", type: "success" });
    } catch (err) {
      const msg = err.response?.data?.message || "Failed to reject contract. Please try again.";
      setToast({ msg, type: "error" });
      setShowReject(false);
    }
  };

  if (!contract) return <Layout><p className="text-gray-400">Loading...</p></Layout>;

  return (
    <Layout>
      {toast && <Toast message={toast.msg} type={toast.type} onClose={() => setToast(null)} />}
      {showReject && (
        <Modal title="Reject Contract" onClose={() => setShowReject(false)}>
          <p className="text-sm text-gray-600 mb-3">Please provide a reason for rejecting this contract.</p>
          <textarea value={rejReason} onChange={e => setRejReason(e.target.value)} rows={4}
            placeholder="Enter rejection reason..."
            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary resize-none mb-4" />
          <div className="flex gap-3">
            <button onClick={() => setShowReject(false)}
              className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">Cancel</button>
            <button onClick={reject}
              className="flex-1 bg-danger text-white py-2 rounded-lg text-sm font-medium">Confirm Rejection</button>
          </div>
        </Modal>
      )}

      <div className="max-w-2xl">
        <div className="flex justify-between items-start mb-6">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Contract Detail</h2>
            <p className="text-gray-500 text-sm mt-1">{contract.propertyAddress}</p>
          </div>
          <StatusBadge status={contract.status} />
        </div>

        <div className="bg-white rounded-xl shadow-sm p-6 mb-5">
          <h3 className="font-semibold text-gray-800 mb-4">Contract Details</h3>
          
          {/* Parties */}
          <div className="mb-4 pb-4 border-b border-gray-100">
            <p className="text-xs text-gray-500 uppercase mb-2">Parties</p>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div><p className="text-gray-400 text-xs mb-1">Landlord</p><p className="font-medium">{contract.landlordName}</p></div>
              <div><p className="text-gray-400 text-xs mb-1">Tenant</p><p className="font-medium">{contract.tenantName}</p></div>
            </div>
          </div>

          {/* Property */}
          <div className="mb-4 pb-4 border-b border-gray-100">
            <p className="text-xs text-gray-500 uppercase mb-2">Property</p>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div><p className="text-gray-400 text-xs mb-1">Property Name</p><p className="font-medium">{contract.propertyName}</p></div>
              <div><p className="text-gray-400 text-xs mb-1">Unit Number</p><p className="font-medium">{contract.unitNumber}</p></div>
              <div className="col-span-2"><p className="text-gray-400 text-xs mb-1">Address</p><p className="font-medium">{contract.propertyAddress}</p></div>
            </div>
          </div>

          {/* Term */}
          <div className="mb-4 pb-4 border-b border-gray-100">
            <p className="text-xs text-gray-500 uppercase mb-2">Contract Term</p>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div><p className="text-gray-400 text-xs mb-1">Start Date</p><p className="font-medium">{fmtDate(contract.startDate)}</p></div>
              <div><p className="text-gray-400 text-xs mb-1">End Date</p><p className="font-medium">{fmtDate(contract.endDate)}</p></div>
              <div><p className="text-gray-400 text-xs mb-1">Notice Period</p><p className="font-medium">{contract.noticePeriodDays || 30} days</p></div>
              <div><p className="text-gray-400 text-xs mb-1">Renewal Type</p><p className="font-medium">{contract.renewalType?.replace(/_/g, ' ') || 'Renegotiate'}</p></div>
            </div>
          </div>

          {/* Financial */}
          <div className="mb-4">
            <p className="text-xs text-gray-500 uppercase mb-2">Financial Terms</p>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div><p className="text-gray-400 text-xs mb-1">Monthly Rent</p><p className="font-bold text-primary">{fmt(contract.monthlyRent)}</p></div>
              <div><p className="text-gray-400 text-xs mb-1">Payment Due Date</p><p className="font-medium">Day {contract.paymentDueDay || 1} of each month</p></div>
              <div><p className="text-gray-400 text-xs mb-1">Payment Method</p><p className="font-medium">{contract.paymentMethod?.replace(/_/g, ' ') || 'Bank Transfer'}</p></div>
              <div><p className="text-gray-400 text-xs mb-1">Security Deposit</p><p className="font-medium">{contract.securityDepositAmount ? fmt(contract.securityDepositAmount) : 'None'}</p></div>
            </div>
          </div>

          {contract.additionalClauses && (
            <div className="mt-4 p-3 bg-gray-50 rounded-lg">
              <p className="text-xs text-gray-500 mb-1">Additional Terms</p>
              <p className="text-sm text-gray-700">{contract.additionalClauses}</p>
            </div>
          )}
          
          <div className="mt-4 flex gap-2">
            {contract.contractDocumentUrl ? (
              <a href={contract.contractDocumentUrl} target="_blank" rel="noopener noreferrer"
                className="flex items-center gap-2 bg-primary text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-900">
                <FileText size={14} /> View Contract PDF
              </a>
            ) : (
              <button onClick={() => setToast({ msg: "Contract PDF will be available after officer approval", type: "info" })}
                className="flex items-center gap-2 border border-gray-300 text-gray-700 px-4 py-2 rounded-lg text-sm hover:bg-gray-50">
                <FileText size={14} /> Contract PDF (Pending)
              </button>
            )}
          </div>
        </div>

        {/* Confirm/Reject */}
        {contract.status === "PENDING_CONFIRMATION" && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-6 mb-5">
            <h3 className="font-semibold text-gray-800 mb-3">Action Required — Confirm This Contract</h3>
            <p className="text-sm text-gray-600 mb-4">Review the contract details above. Type your full name below to confirm.</p>
            <input value={signature} onChange={e => setSignature(e.target.value)}
              placeholder="Type your full name as digital signature"
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary mb-4" />
            <div className="flex gap-3">
              <button onClick={confirm}
                className="flex-1 bg-success text-white py-2.5 rounded-lg text-sm font-semibold hover:bg-green-700">
                ✓ Confirm Contract
              </button>
              <button onClick={() => setShowReject(true)}
                className="flex-1 bg-danger text-white py-2.5 rounded-lg text-sm font-semibold hover:bg-red-700">
                ✗ Reject Contract
              </button>
            </div>
          </div>
        )}

        {/* Declarations */}
        {(contract.status === "ACTIVE" || contract.status === "UNDER_APPEAL") && (
          <div className="bg-white rounded-xl shadow-sm">
            <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
              <h3 className="font-semibold text-gray-800">Rent Declarations</h3>
              <button onClick={() => navigate("/tenant/appeals")}
                className="text-sm text-primary font-medium hover:underline flex items-center gap-1">
                <AlertTriangle size={14} /> Create Appeal
              </button>
            </div>
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                <tr>
                  <th className="px-6 py-3 text-left">Period</th>
                  <th className="px-6 py-3 text-left">Declared Rent</th>
                  <th className="px-6 py-3 text-left">Benchmark</th>
                  <th className="px-6 py-3 text-left">Anomaly</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {declarations.map(d => (
                  <tr key={d.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">{fmtDate(d.declarationPeriod)}</td>
                    <td className="px-6 py-4 font-medium">{fmt(d.declaredRent)}</td>
                    <td className="px-6 py-4 text-gray-500">{fmt(d.aiBenchmarkRent)}</td>
                    <td className="px-6 py-4">
                      {d.isAnomaly
                        ? <span className="text-danger text-xs flex items-center gap-1"><AlertTriangle size={11} /> Flagged</span>
                        : <span className="text-success text-xs">Clean</span>}
                    </td>
                  </tr>
                ))}
                {declarations.length === 0 && (
                  <tr><td colSpan={4} className="px-6 py-6 text-center text-gray-400">No declarations yet</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  );
}