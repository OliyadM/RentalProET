// Create Contract will go here
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import FileUpload from "../../components/FileUpload";
import { contractsAPI, unitsAPI, propertiesAPI } from "../../services/api";
import { useAuth } from "../../context/AuthContext";

export default function CreateContract() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [units, setUnits] = useState([]);
  const [toast, setToast] = useState(null);
  const [form, setForm] = useState({
    unitId: "", tenantEmail: "",
    startDate: "", endDate: "", monthlyRent: "", 
    paymentFrequency: "Monthly",
    paymentDueDay: "1",
    paymentMethod: "BANK_TRANSFER",
    securityDepositAmount: "",
    noticePeriodDays: "30",
    renewalType: "RENEGOTIATE",
    contractDocumentUrl: "",
    additionalClauses: "",
  });

  useEffect(() => {
    propertiesAPI.getMyProperties(user.id).then(async ps => {
      const all = await Promise.all(ps.map(p => unitsAPI.getByProperty(p.id)));
      setUnits(all.flat());
    });
  }, [user.id]);

  const set = f => e => setForm({ ...form, [f]: e.target.value });

  const submit = async (status) => {
    try {
      // Validate required fields
      if (!form.unitId || !form.tenantEmail || !form.startDate || !form.endDate || !form.monthlyRent) {
        setToast("Please fill in all required fields");
        return;
      }

      // Prepare contract data matching backend ContractRequest
      const contractData = {
        unitId: form.unitId,
        tenantEmail: form.tenantEmail,
        startDate: form.startDate,
        endDate: form.endDate,
        monthlyRent: parseFloat(form.monthlyRent),
        paymentFrequency: form.paymentFrequency,
        paymentDueDay: parseInt(form.paymentDueDay),
        paymentMethod: form.paymentMethod,
        securityDepositAmount: form.securityDepositAmount ? parseFloat(form.securityDepositAmount) : null,
        noticePeriodDays: parseInt(form.noticePeriodDays),
        renewalType: form.renewalType,
        contractDocumentUrl: form.contractDocumentUrl || null,
        additionalClauses: form.additionalClauses || null,
      };

      // Create contract as DRAFT first
      const response = await contractsAPI.create(contractData);
      
      // If user clicked "Submit for Confirmation", submit it
      if (status === "PENDING_CONFIRMATION") {
        await contractsAPI.submit(response.id);
        setToast("Contract submitted for tenant confirmation");
      } else {
        setToast("Contract saved as draft");
      }
      
      setTimeout(() => navigate("/landlord/contracts"), 1500);
    } catch (error) {
      console.error("Contract creation error:", error);
      const errorMsg = error.response?.data?.message || error.message || "Failed to create contract";
      setToast(errorMsg);
    }
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}
      <div className="max-w-2xl">
        <h2 className="text-2xl font-bold text-gray-900 mb-1">Create Contract</h2>
        <p className="text-gray-500 text-sm mb-6">Register a new tenant rental agreement</p>

        <div className="bg-white rounded-xl shadow-sm p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Unit *</label>
            <select value={form.unitId} onChange={set("unitId")} required
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
              <option value="">Select a unit</option>
              {units.map(u => <option key={u.id} value={u.id}>{u.unitNumber} — {u.floorArea}m²</option>)}
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tenant Email *</label>
            <input type="email" value={form.tenantEmail} onChange={set("tenantEmail")} required
              placeholder="tenant@example.com"
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            <p className="text-xs text-gray-500 mt-1">
              Tenant must be registered with this email address
            </p>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Start Date *</label>
              <input type="date" value={form.startDate} onChange={set("startDate")} required
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">End Date *</label>
              <input type="date" value={form.endDate} onChange={set("endDate")} required
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Monthly Rent (ETB) *</label>
            <input type="number" value={form.monthlyRent} onChange={set("monthlyRent")} required min="1"
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Payment Frequency *</label>
            <select value={form.paymentFrequency} onChange={set("paymentFrequency")} required
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
              <option value="Monthly">Monthly</option>
              <option value="Quarterly">Quarterly</option>
              <option value="Annually">Annually</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Payment Due Day *</label>
              <input type="number" value={form.paymentDueDay} onChange={set("paymentDueDay")} required min="1" max="31"
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              <p className="text-xs text-gray-500 mt-1">Day of month (1-31)</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Payment Method *</label>
              <select value={form.paymentMethod} onChange={set("paymentMethod")} required
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
                <option value="BANK_TRANSFER">Bank Transfer</option>
                <option value="CASH">Cash</option>
                <option value="MOBILE_MONEY">Mobile Money</option>
                <option value="CHECK">Check</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Security Deposit (ETB)</label>
            <input type="number" value={form.securityDepositAmount} onChange={set("securityDepositAmount")} min="0"
              placeholder="Optional - typically 1-2 months rent"
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Notice Period (Days) *</label>
              <input type="number" value={form.noticePeriodDays} onChange={set("noticePeriodDays")} required min="1"
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              <p className="text-xs text-gray-500 mt-1">Standard: 30-60 days</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Renewal Terms *</label>
              <select value={form.renewalType} onChange={set("renewalType")} required
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
                <option value="RENEGOTIATE">Renegotiate</option>
                <option value="AUTO_RENEW">Auto-Renew</option>
                <option value="FIXED_TERM">Fixed Term Only</option>
              </select>
            </div>
          </div>

          <FileUpload
            label="Contract Document (Optional)"
            value={form.contractDocumentUrl}
            onChange={(url) => setForm({ ...form, contractDocumentUrl: url })}
            folder="contracts"
            helperText="Upload signed rental agreement document (optional)"
          />
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Additional Clauses</label>
            <textarea value={form.additionalClauses} onChange={set("additionalClauses")} rows={3}
              placeholder="Enter any additional terms or special conditions..."
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary resize-none" />
          </div>

          <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-700">
            💡 The tenant's name and contact details will be automatically retrieved from their registered account
          </div>

          <div className="flex gap-3 pt-2">
            <button onClick={() => submit("DRAFT")}
              className="flex-1 border border-gray-300 text-gray-700 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50">
              Save as Draft
            </button>
            <button onClick={() => submit("PENDING_CONFIRMATION")}
              className="flex-1 bg-primary text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900">
              Submit for Confirmation
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
}