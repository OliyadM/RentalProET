// Create Contract will go here
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import { contractsAPI, unitsAPI, propertiesAPI } from "../../services/api";
import { useAuth } from "../../context/AuthContext";

export default function CreateContract() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [units, setUnits] = useState([]);
  const [toast, setToast] = useState(null);
  const [form, setForm] = useState({
    unitId: "", tenantEmail: "",
    startDate: "", endDate: "", monthlyRent: "", termsAndConditions: "",
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
        termsAndConditions: form.termsAndConditions || null,
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
            <label className="block text-sm font-medium text-gray-700 mb-1">Terms and Conditions</label>
            <textarea value={form.termsAndConditions} onChange={set("termsAndConditions")} rows={3}
              placeholder="Enter rental terms..."
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