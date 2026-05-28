import { useEffect, useState } from "react";
import { CheckCircle, XCircle, FileText, Eye, Search, Filter } from "lucide-react";
import Layout from "../../components/Layout";
import Modal from "../../components/Modal";
import StatusBadge from "../../components/StatusBadge";
import Toast from "../../components/Toast";
import { contractsAPI } from "../../services/api";
import { fmtDate } from "../../utils/dateUtils";
import { openContractPrintView } from "../../utils/contractPdf";

const SUB_CITIES = [
  "Addis Ketema", "Akaky Kaliti", "Arada", "Bole", "Gullele",
  "Kirkos", "Kolfe Keranio", "Lideta", "Nifas Silk-Lafto", "Yeka"
];

export default function OfficerContracts() {
  const [contracts, setContracts] = useState([]);
  const [filteredContracts, setFilteredContracts] = useState([]);
  const [viewing, setViewing] = useState(null);
  const [approving, setApproving] = useState(null);
  const [rejecting, setRejecting] = useState(null);
  const [rejectionReason, setRejectionReason] = useState("");
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(true);

  // Filters
  const [searchText, setSearchText] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [subCityFilter, setSubCityFilter] = useState("");
  const [sortBy, setSortBy] = useState("newest");

  useEffect(() => {
    loadContracts();
  }, [statusFilter, subCityFilter, sortBy]);

  useEffect(() => {
    // Client-side search filtering
    if (searchText.trim()) {
      const search = searchText.toLowerCase();
      setFilteredContracts(
        contracts.filter(c =>
          c.landlordName?.toLowerCase().includes(search) ||
          c.tenantName?.toLowerCase().includes(search) ||
          c.propertyName?.toLowerCase().includes(search) ||
          c.propertyAddress?.toLowerCase().includes(search)
        )
      );
    } else {
      setFilteredContracts(contracts);
    }
  }, [searchText, contracts]);

  const loadContracts = async () => {
    setLoading(true);
    try {
      const filters = {
        status: statusFilter || undefined,
        subCity: subCityFilter || undefined,
        sort: sortBy === "newest" ? "newest,desc" : sortBy === "oldest" ? "oldest,asc" : "rent,desc"
      };
      const data = await contractsAPI.getForOfficer(filters);
      setContracts(data);
      setFilteredContracts(data);
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
          <div className="space-y-4 text-sm max-h-[70vh] overflow-y-auto">
            {/* Property */}
            <div className="pb-3 border-b border-gray-100">
              <p className="text-gray-500 text-xs uppercase mb-2">Property</p>
              <p className="font-medium">{viewing.propertyName}</p>
              <p className="text-gray-600 text-xs">{viewing.propertyAddress}</p>
              <p className="text-gray-600 text-xs mt-1">Unit: {viewing.unitNumber}</p>
            </div>

            {/* Parties */}
            <div className="pb-3 border-b border-gray-100">
              <p className="text-gray-500 text-xs uppercase mb-2">Parties</p>
              <div className="grid grid-cols-2 gap-3">
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
              </div>
            </div>

            {/* Term */}
            <div className="pb-3 border-b border-gray-100">
              <p className="text-gray-500 text-xs uppercase mb-2">Contract Term</p>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <p className="text-gray-500 text-xs">Start Date</p>
                  <p className="font-medium">{fmtDate(viewing.startDate)}</p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">End Date</p>
                  <p className="font-medium">{fmtDate(viewing.endDate)}</p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Notice Period</p>
                  <p className="font-medium">{viewing.noticePeriodDays || 30} days</p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Renewal Type</p>
                  <p className="font-medium">{viewing.renewalType?.replace(/_/g, ' ') || 'Renegotiate'}</p>
                </div>
              </div>
            </div>

            {/* Financial */}
            <div className="pb-3 border-b border-gray-100">
              <p className="text-gray-500 text-xs uppercase mb-2">Financial Terms</p>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <p className="text-gray-500 text-xs">Monthly Rent</p>
                  <p className="font-medium text-primary">{viewing.monthlyRent} ETB</p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Payment Due Date</p>
                  <p className="font-medium">Day {viewing.paymentDueDay || 1} of month</p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Payment Method</p>
                  <p className="font-medium">{viewing.paymentMethod?.replace(/_/g, ' ') || 'Bank Transfer'}</p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Security Deposit</p>
                  <p className="font-medium">{viewing.securityDepositAmount ? `${viewing.securityDepositAmount} ETB` : 'None'}</p>
                </div>
              </div>
            </div>

            {viewing.additionalClauses && (
              <div className="pb-3 border-b border-gray-100">
                <p className="text-gray-500 text-xs uppercase mb-2">Additional Terms</p>
                <p className="text-gray-700 text-xs">{viewing.additionalClauses}</p>
              </div>
            )}

            {/* Tenant signature — key evidence for officer review */}
            {viewing.tenantSignature && (
              <div className="pb-3 border-b border-gray-100">
                <p className="text-gray-500 text-xs uppercase mb-2">Tenant Signature</p>
                <div className="border border-gray-200 rounded-lg p-2 bg-gray-50 inline-block">
                  <img
                    src={viewing.tenantSignature}
                    alt="Tenant signature"
                    className="max-h-20 max-w-xs"
                  />
                </div>
                {viewing.tenantConfirmedAt && (
                  <p className="text-xs text-gray-400 mt-1">
                    Signed: {fmtDate(viewing.tenantConfirmedAt)}
                  </p>
                )}
              </div>
            )}

            <div className="flex gap-2">
              <button
                onClick={() => openContractPrintView(viewing)}
                className="flex items-center gap-2 text-primary text-sm hover:underline">
                <FileText size={14} /> View Full Contract
              </button>
              {viewing.contractDocumentUrl && (
                <a
                  href={viewing.contractDocumentUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 text-gray-500 text-sm hover:underline ml-4">
                  <FileText size={14} /> Uploaded Document
                </a>
              )}
            </div>
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
          {filteredContracts.length} contract{filteredContracts.length !== 1 ? 's' : ''} found
        </p>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl shadow-sm p-4 mb-5">
        <div className="flex items-center gap-2 mb-3">
          <Filter size={18} className="text-gray-400" />
          <h3 className="font-semibold text-gray-800">Search & Filters</h3>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
          {/* Search */}
          <div className="relative">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Search landlord, tenant, property..."
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary">
            <option value="">All Statuses</option>
            <option value="PENDING_OFFICER_REVIEW">Pending Review</option>
            <option value="ACTIVE">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="UNDER_APPEAL">Under Appeal</option>
          </select>

          {/* Sub-City Filter */}
          <select
            value={subCityFilter}
            onChange={(e) => setSubCityFilter(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary">
            <option value="">All Sub-Cities</option>
            {SUB_CITIES.map(sc => (
              <option key={sc} value={sc}>{sc}</option>
            ))}
          </select>

          {/* Sort */}
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary">
            <option value="newest">Newest First</option>
            <option value="oldest">Oldest First</option>
            <option value="rent">Highest Rent</option>
          </select>
        </div>

        {/* Active Filters Summary */}
        {(searchText || statusFilter || subCityFilter) && (
          <div className="mt-3 flex items-center gap-2 text-xs">
            <span className="text-gray-500">Active filters:</span>
            {searchText && (
              <span className="bg-blue-50 text-primary px-2 py-1 rounded">
                Search: "{searchText}"
              </span>
            )}
            {statusFilter && (
              <span className="bg-blue-50 text-primary px-2 py-1 rounded">
                Status: {statusFilter.replace(/_/g, ' ')}
              </span>
            )}
            {subCityFilter && (
              <span className="bg-blue-50 text-primary px-2 py-1 rounded">
                Sub-City: {subCityFilter}
              </span>
            )}
            <button
              onClick={() => {
                setSearchText("");
                setStatusFilter("");
                setSubCityFilter("");
              }}
              className="text-danger hover:underline ml-2">
              Clear all
            </button>
          </div>
        )}
      </div>

      {loading ? (
        <div className="bg-white rounded-xl shadow-sm p-8 text-center text-gray-400">
          Loading contracts...
        </div>
      ) : filteredContracts.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm p-8 text-center text-gray-400">
          <FileText size={40} className="mx-auto mb-3 opacity-30" />
          <p>No contracts found</p>
          {(searchText || statusFilter || subCityFilter) && (
            <p className="text-sm mt-2">Try adjusting your filters</p>
          )}
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
                <th className="px-6 py-3 text-left">Status</th>
                <th className="px-6 py-3 text-left">Submitted</th>
                <th className="px-6 py-3 text-left">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredContracts.map(c => (
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
                  <td className="px-6 py-4">
                    <StatusBadge status={c.status} />
                  </td>
                  <td className="px-6 py-4 text-gray-500 text-xs">
                    {c.tenantConfirmedAt ? fmtDate(c.tenantConfirmedAt) : '-'}
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button
                        onClick={() => setViewing(c)}
                        className="text-primary hover:bg-blue-50 p-1.5 rounded"
                        title="View Details">
                        <Eye size={16} />
                      </button>
                      {c.status === 'PENDING_OFFICER_REVIEW' && (
                        <>
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
                        </>
                      )}
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
