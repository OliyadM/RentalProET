import { useEffect, useState } from "react";
import { CheckCircle, XCircle, FileText, Eye } from "lucide-react";
import Layout from "../../components/Layout";
import Modal from "../../components/Modal";
import Toast from "../../components/Toast";
import { contractsAPI } from "../../services/api";
import { fmtDate } from "../../utils/dateUtils";

export default function OfficerContracts() {
  const [contracts, setContracts] = useState([]);
  const [viewing, setViewing] = useState(null);
  const [approving, setApproving] = useState(null);
  const [rejecting, setRejecting] = useState(null);
  const [rejectionReason, setRejectionReason] = useState("");
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadContracts();
  }, []);

  const loadContracts = async () => {
    try {
      const data = await contractsAPI.getPendingReview();
      setContracts(data);
    } catch (error) {
      setToast("Failed to load contracts");
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async () => {
    try {
      await contractsAPI.approve(approving.id);
      setContracts(prev => prev.filter(c => c.id !== approving.id));
      setApproving(null);
      setToast("Contract approved successfully");
    } catch (error) {
      setToast(error.response?.data?.message || "Failed to approve contract");
    }
  };

  const handleReject = async () => {
    if (!rejectionReason.trim()) {
      setToast("Please provide a rejection reason");
      return;
    }

    try {
      await contractsAPI.rejectByOfficer(rejecting.id, rejectionReason);
      setContracts(prev => prev.filter(c => c.id !== rejecting.id));
      setRejecting(null);
      setRejectionReason("");
      setToast("Contract rejected");
    } catch (error) {
      setToast(error.response?.data?.message || "Failed to reject contract");
    }
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}

      {/* Approve Modal */}
      {approving && (
        <Modal title="Approve Contract" onClose={() => setApproving(null)}>
          <p className="text-sm text-gray-600 mb-4">
            Confirm approval of contract for <strong>{approving.propertyAddress}</strong>?
          </p>
          <p className="text-xs text-gray-500 mb-4">
            Landlord: {approving.landlordName} | Tenant: {approving.tenantName}
          </p>
          <div className="flex gap-3">
            <button
              onClick={() => setApproving(null)}
              className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">
              Cancel
            </button>
            <button
              onClick={handleApprove}
              className="flex-1 bg-success text-white py-2 rounded-lg text-sm font-medium">
              Approve Contract
            </button>
          </div>
        </Modal>
      )}

      {/* Reject Modal */}
      {rejecting && (
        <Modal title="Reject Contract" onClose={() => { setRejecting(null); setRejectionReason(""); }}>
          <p className="text-sm text-gray-600 mb-4">
            Reject contract for <strong>{rejecting.propertyAddress}</strong>?
          </p>
          <textarea
            value={rejectionReason}
            onChange={(e) => setRejectionReason(e.target.value)}
            placeholder="Provide reason for rejection..."
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mb-4"
            rows={4}
          />
          <div className="flex gap-3">
            <button
              onClick={() => { setRejecting(null); setRejectionReason(""); }}
              className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">
              Cancel
            </button>
            <button
              onClick={handleReject}
              className="flex-1 bg-danger text-white py-2 rounded-lg text-sm font-medium">
              Reject Contract
            </button>
          </div>
        </Modal>
      )}

      {/* View Details Modal */}
      {viewing && (
        <Modal title="Contract Details" onClose={() => setViewing(null)}>
          <div className="space-y-3 text-sm">
            <div>
              <p className="text-gray-500 text-xs">Property</p>
              <p className="font-medium">{viewing.propertyName}</p>
              <p className="text-gray-600 text-xs">{viewing.propertyAddress}</p>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <p className="text-gray-500 text-xs">Unit</p>
                <p className="font-medium">{viewing.unitNumber}</p>
              </div>
              <div>
                <p className="text-gray-500 text-xs">Monthly Rent</p>
                <p className="font-medium">{viewing.monthlyRent} ETB</p>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <p className="text-gray-500 text-xs">Start Date</p>
                <p className="font-medium">{fmtDate(viewing.startDate)}</p>
              </div>
              <div>
                <p className="text-gray-500 text-xs">End Date</p>
                <p className="font-medium">{fmtDate(viewing.endDate)}</p>
              </div>
            </div>
            <div>
              <p className="text-gray-500 text-xs">Landlord</p>
              <p className="font-medium">{viewing.landlordName}</p>
              <p className="text-gray-600 text-xs">{viewing.landlordEmail}</p>
            </div>
            <div>
              <p className="text-gray-500 text-xs">Tenant</p>
              <p className="font-medium">{viewing.tenantName}</p>
              <p className="text-gray-600 text-xs">{viewing.tenantEmail}</p>
            </div>
            {viewing.additionalClauses && (
              <div>
                <p className="text-gray-500 text-xs">Additional Clauses</p>
                <p className="text-gray-700 text-xs">{viewing.additionalClauses}</p>
              </div>
            )}
            {viewing.contractDocumentUrl && (
              <div>
                <p className="text-gray-500 text-xs mb-1">Contract Document</p>
                <a
                  href={viewing.contractDocumentUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary text-xs hover:underline">
                  View Document
                </a>
              </div>
            )}
          </div>
          <button
            onClick={() => setViewing(null)}
            className="w-full mt-4 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">
            Close
          </button>
        </Modal>
      )}

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Contract Review</h2>
        <p className="text-gray-500 text-sm mt-1">
          {contracts.length} contracts pending officer approval
        </p>
      </div>

      {loading ? (
        <div className="bg-white rounded-xl shadow-sm p-8 text-center text-gray-400">
          Loading contracts...
        </div>
      ) : contracts.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm p-8 text-center text-gray-400">
          <FileText size={40} className="mx-auto mb-3 opacity-30" />
          <p>No contracts pending review</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="px-6 py-3 text-left">Property</th>
                <th className="px-6 py-3 text-left">Unit</th>
                <th className="px-6 py-3 text-left">Landlord</th>
                <th className="px-6 py-3 text-left">Tenant</th>
                <th className="px-6 py-3 text-left">Rent</th>
                <th className="px-6 py-3 text-left">Submitted</th>
                <th className="px-6 py-3 text-left">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {contracts.map(c => (
                <tr key={c.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <p className="font-medium text-gray-900">{c.propertyName}</p>
                    <p className="text-xs text-gray-500">{c.propertyAddress}</p>
                  </td>
                  <td className="px-6 py-4 text-gray-600">{c.unitNumber}</td>
                  <td className="px-6 py-4">
                    <p className="text-gray-900">{c.landlordName}</p>
                  </td>
                  <td className="px-6 py-4">
                    <p className="text-gray-900">{c.tenantName}</p>
                  </td>
                  <td className="px-6 py-4 font-medium">{c.monthlyRent} ETB</td>
                  <td className="px-6 py-4 text-gray-500 text-xs">
                    {fmtDate(c.tenantConfirmedAt)}
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button
                        onClick={() => setViewing(c)}
                        className="text-primary hover:bg-blue-50 p-1.5 rounded"
                        title="View Details">
                        <Eye size={16} />
                      </button>
                      <button
                        onClick={() => setApproving(c)}
                        className="text-success hover:bg-green-50 p-1.5 rounded"
                        title="Approve">
                        <CheckCircle size={16} />
                      </button>
                      <button
                        onClick={() => setRejecting(c)}
                        className="text-danger hover:bg-red-50 p-1.5 rounded"
                        title="Reject">
                        <XCircle size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
