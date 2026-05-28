// Landlord Contract Detail will go here
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { AlertTriangle, TrendingUp, FileText } from "lucide-react";
import Layout from "../../components/Layout";
import StatusBadge from "../../components/StatusBadge";
import Toast from "../../components/Toast";
import { contractsAPI, declarationsAPI, analyticsAPI } from "../../services/api";
import { fmtDate } from "../../utils/dateUtils";
import { openContractPrintView } from "../../utils/contractPdf";

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }
function pct(a, b) { return (((a - b) / b) * 100).toFixed(1); }

export default function LandlordContractDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [contract, setContract] = useState(null);
  const [declarations, setDeclarations] = useState([]);
  const [benchmark, setBenchmark] = useState(null);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    contractsAPI.getById(id).then(c => {
      setContract(c);
      if (c?.propertyId) {
        analyticsAPI.getBenchmark(c.propertyId).then(setBenchmark).catch(() => {});
      }
    });
    declarationsAPI.getByContract(id).then(setDeclarations).catch(() => setDeclarations([]));
  }, [id]);

  if (!contract) return <Layout><p className="text-gray-400">Loading...</p></Layout>;

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}
      <div className="max-w-3xl">
        <div className="flex justify-between items-start mb-6">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Contract Detail</h2>
            <p className="text-gray-500 text-sm mt-1">{contract.propertyAddress}</p>
          </div>
          <StatusBadge status={contract.status} />
        </div>

        {/* Contract Info */}
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
            {contract.status === "DRAFT" && (
              <button
                onClick={async () => {
                  try {
                    const updated = await contractsAPI.submit(id);
                    setContract(updated);
                    setToast("Contract submitted for tenant confirmation");
                  } catch (err) {
                    const msg = err.response?.data?.message || "Failed to submit contract. Please try again.";
                    setToast(msg);
                  }
                }}
                className="bg-primary text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-900">
                Submit for Confirmation
              </button>
            )}
            {contract.status === "ACTIVE" && (
              <button onClick={() => navigate(`/landlord/declarations/add/${id}`)}
                className="bg-primary text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-900">
                Add Declaration
              </button>
            )}
            {contract.contractDocumentUrl ? (
              <div className="flex gap-2">
                <button onClick={() => openContractPrintView(contract)}
                  className="flex items-center gap-2 border border-primary text-primary px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-50">
                  <FileText size={15} /> View Contract
                </button>
                <a href={contract.contractDocumentUrl} target="_blank" rel="noopener noreferrer"
                  className="flex items-center gap-2 border border-gray-300 text-gray-600 px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-50">
                  <FileText size={15} /> Uploaded Document
                </a>
              </div>
            ) : (
              <button onClick={() => openContractPrintView(contract)}
                className="flex items-center gap-2 border border-primary text-primary px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-50">
                <FileText size={15} /> View Contract
              </button>
            )}
          </div>
        </div>

        {/* AI Benchmark */}
        {benchmark && (
          <div className="bg-white rounded-xl shadow-sm p-6 mb-5 border-l-4 border-accent">
            <div className="flex items-center gap-2 mb-3">
              <TrendingUp size={18} className="text-accent" />
              <h3 className="font-semibold text-gray-800">AI Rent Benchmark</h3>
              <span className="text-xs bg-yellow-50 text-yellow-700 px-2 py-0.5 rounded-full">Advisory only — not legally binding</span>
            </div>
            <div className="grid grid-cols-3 gap-4 text-sm mb-3">
              <div className="text-center p-3 bg-gray-50 rounded-lg">
                <p className="text-xs text-gray-500 mb-1">Min</p>
                <p className="font-bold text-gray-800">{fmt(benchmark.minRent)}</p>
              </div>
              <div className="text-center p-3 bg-blue-50 rounded-lg border border-primary">
                <p className="text-xs text-primary mb-1">Suggested</p>
                <p className="font-bold text-primary text-lg">{fmt(benchmark.suggestedRent)}</p>
              </div>
              <div className="text-center p-3 bg-gray-50 rounded-lg">
                <p className="text-xs text-gray-500 mb-1">Max</p>
                <p className="font-bold text-gray-800">{fmt(benchmark.maxRent)}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <span className="text-gray-500">Confidence:</span>
              <div className="flex-1 bg-gray-200 rounded-full h-2">
                <div className="bg-success h-2 rounded-full" style={{ width: `${benchmark.confidenceScore * 100}%` }} />
              </div>
              <span className="font-medium">{Math.round(benchmark.confidenceScore * 100)}%</span>
              <StatusBadge status={benchmark.marketTrend} />
            </div>
            <p className="text-xs text-gray-500 mt-3">{benchmark.reasoning}</p>
          </div>
        )}

        {/* Declarations */}
        <div className="bg-white rounded-xl shadow-sm">
          <div className="px-6 py-4 border-b border-gray-100">
            <h3 className="font-semibold text-gray-800">Declarations ({declarations.length})</h3>
          </div>
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="px-6 py-3 text-left">Period</th>
                <th className="px-6 py-3 text-left">Declared</th>
                <th className="px-6 py-3 text-left">Benchmark</th>
                <th className="px-6 py-3 text-left">Deviation</th>
                <th className="px-6 py-3 text-left">Anomaly</th>
                <th className="px-6 py-3 text-left">Tax</th>
                <th className="px-6 py-3 text-left">Verified</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {declarations.map(d => {
                const dev = pct(d.declaredRent, d.aiBenchmarkRent);
                return (
                  <tr key={d.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">{fmtDate(d.declarationPeriod)}</td>
                    <td className="px-6 py-4 font-medium">{fmt(d.declaredRent)}</td>
                    <td className="px-6 py-4 text-gray-500">{fmt(d.aiBenchmarkRent)}</td>
                    <td className="px-6 py-4">
                      <span className={`font-medium ${Math.abs(dev) > 15 ? "text-danger" : Math.abs(dev) > 5 ? "text-accent" : "text-success"}`}>
                        {dev > 0 ? "+" : ""}{dev}%
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      {d.isAnomaly
                        ? <span className="flex items-center gap-1 text-danger text-xs"><AlertTriangle size={12} /> Flagged</span>
                        : <span className="text-success text-xs">Clean</span>}
                    </td>
                    <td className="px-6 py-4">{fmt(d.estimatedTax)}</td>
                    <td className="px-6 py-4">
                      <span className={`text-xs font-medium ${d.isVerified ? "text-success" : "text-gray-400"}`}>
                        {d.isVerified ? "Verified" : "Pending"}
                      </span>
                    </td>
                  </tr>
                );
              })}
              {declarations.length === 0 && (
                <tr><td colSpan={7} className="px-6 py-8 text-center text-gray-400">No declarations yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </Layout>
  );
}