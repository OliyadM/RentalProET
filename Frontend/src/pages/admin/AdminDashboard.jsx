import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  Settings, Users, Globe, Plus, CheckCircle, XCircle,
  Save, RefreshCw, ShieldCheck,
} from "lucide-react";
import Layout from "../../components/Layout";
import Modal from "../../components/Modal";
import Toast from "../../components/Toast";
import { adminAPI } from "../../services/api";
import { fmtDate } from "../../utils/dateUtils";

// ── Tab IDs ───────────────────────────────────────────────────────────────────
const TABS = [
  { id: "config",   label: "System Settings",  icon: Settings },
  { id: "officers", label: "Manage Officers",  icon: Users },
  { id: "metrics",  label: "Global Metrics",   icon: Globe },
];

const SUB_CITIES = [
  "Bole", "Kirkos", "Yeka", "Arada", "Lideta", "Kolfe", "Nifas Silk", "Akaky",
];

// ── Helpers ───────────────────────────────────────────────────────────────────
function SectionCard({ title, children }) {
  return (
    <div className="bg-white rounded-xl shadow-sm p-6 mb-5">
      <h3 className="text-base font-semibold text-gray-800 mb-4">{title}</h3>
      {children}
    </div>
  );
}

function StatBox({ label, value, sub }) {
  return (
    <div className="bg-gray-50 rounded-lg p-4 text-center">
      <p className="text-2xl font-bold text-primary">{value}</p>
      <p className="text-xs font-medium text-gray-700 mt-1">{label}</p>
      {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
    </div>
  );
}

// ── Main component ────────────────────────────────────────────────────────────
export default function AdminDashboard() {
  const location = useLocation();
  const navigate = useNavigate();

  // Derive active tab from ?tab= query param, default to "config"
  const searchParams = new URLSearchParams(location.search);
  const tabParam = searchParams.get("tab") || "config";
  const [activeTab, setActiveTab] = useState(tabParam);

  // Sync tab state when URL changes (sidebar nav clicks)
  useEffect(() => {
    setActiveTab(searchParams.get("tab") || "config");
  }, [location.search]);

  const switchTab = (id) => {
    setActiveTab(id);
    navigate(id === "config" ? "/admin/dashboard" : `/admin/dashboard?tab=${id}`, { replace: true });
  };

  const [toast, setToast] = useState(null);

  return (
    <Layout>
      {toast && <Toast message={toast.msg} type={toast.type} onClose={() => setToast(null)} />}

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Admin Control Panel</h2>
        <p className="text-gray-500 text-sm mt-1">System-wide configuration and officer management</p>
      </div>

      {/* Tab bar */}
      <div className="flex gap-1 mb-6 border-b border-gray-200">
        {TABS.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            onClick={() => switchTab(id)}
            className={`flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 transition
              ${activeTab === id
                ? "border-primary text-primary"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"}`}
          >
            <Icon size={15} />
            {label}
          </button>
        ))}
      </div>

      {/* Tab panels */}
      {activeTab === "config"   && <SystemConfigTab setToast={setToast} />}
      {activeTab === "officers" && <OfficerDirectoryTab setToast={setToast} />}
      {activeTab === "metrics"  && <GlobalMetricsTab />}
    </Layout>
  );
}

// ── Tab 1: System Configuration ───────────────────────────────────────────────
function SystemConfigTab({ setToast }) {
  const [config, setConfig] = useState(null);
  const [saving, setSaving] = useState(false);

  // Platform settings
  const [anomalyThreshold, setAnomalyThreshold] = useState("");
  const [maxRentCap, setMaxRentCap] = useState("");
  const [minContractYears, setMinContractYears] = useState("");

  // Tax rule metadata
  const [taxRuleVersion, setTaxRuleVersion] = useState("");

  // Business flat rate
  const [businessRate, setBusinessRate] = useState("");

  // Residential deduction
  const [deductionPercent, setDeductionPercent] = useState("");

  // Progressive bands — array of {minIncome, maxIncome, ratePercent, deductibleAmount}
  const [bands, setBands] = useState([]);

  useEffect(() => {
    adminAPI.getConfig().then((data) => {
      setConfig(data);
      setAnomalyThreshold(String(data.anomalyThresholdPercent));
      setMaxRentCap(String(data.maxRentIncreaseCapPercent));
      setMinContractYears(String(data.minimumContractYears ?? 2));
      setTaxRuleVersion(data.taxRuleVersion || "1395/2025");
      setBusinessRate(String(data.businessFlatTaxRatePercent ?? 30));
      setDeductionPercent(String(data.residentialDeductionPercent ?? 40));
      setBands(data.taxBands?.map(b => ({
        minIncome: String(b.minIncome),
        maxIncome: b.maxIncome >= 1e17 ? "" : String(b.maxIncome),
        ratePercent: String(b.ratePercent),
        deductibleAmount: String(b.deductibleAmount),
        label: b.label,
      })) || []);
    });
  }, []);

  const updateBand = (idx, field, value) => {
    setBands(prev => prev.map((b, i) => i === idx ? { ...b, [field]: value } : b));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        anomalyThresholdPercent:  parseFloat(anomalyThreshold),
        maxRentIncreaseCapPercent: parseFloat(maxRentCap),
        minimumContractYears:     parseInt(minContractYears, 10),
        taxRuleVersion,
        businessFlatTaxRatePercent: parseFloat(businessRate),
        residentialDeductionPercent: parseFloat(deductionPercent),
        taxBands: bands.map((b, i) => ({
          minIncome:       parseFloat(b.minIncome) || 0,
          maxIncome:       i === bands.length - 1 ? 999999999 : parseFloat(b.maxIncome) || 0,
          ratePercent:     parseFloat(b.ratePercent) || 0,
          deductibleAmount: parseFloat(b.deductibleAmount) || 0,
        })),
      };
      const updated = await adminAPI.updateConfig(payload);
      setConfig(updated);
      setToast({ msg: "Tax configuration saved successfully", type: "success" });
    } catch (err) {
      const data = err.response?.data;
      const msg = typeof data === "object" && !data?.message
        ? Object.values(data).join(" · ")
        : data?.message || "Failed to save configuration";
      setToast({ msg, type: "error" });
    } finally {
      setSaving(false);
    }
  };

  if (!config || bands.length === 0) {
    return <div className="text-gray-400 text-sm py-8 text-center">Loading configuration...</div>;
  }

  const inputCls = "border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary";

  return (
    <div className="max-w-3xl">
      <form onSubmit={handleSave} className="space-y-5">

        {/* ── Platform Settings ─────────────────────────────────────────── */}
        <SectionCard title="Platform Settings">
          <p className="text-xs text-gray-400 mb-4">Changes take effect immediately. Existing declarations are not retroactively recalculated.</p>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Anomaly Threshold (%)</label>
              <input type="number" value={anomalyThreshold} onChange={e => setAnomalyThreshold(e.target.value)}
                min="1" max="100" step="0.5" required className={inputCls + " w-full"} />
              <p className="text-xs text-gray-400 mt-1">Declarations deviating more than this % from AI benchmark are flagged</p>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Max Rent Increase Cap (%)</label>
              <input type="number" value={maxRentCap} onChange={e => setMaxRentCap(e.target.value)}
                min="0" max="100" step="0.5" required className={inputCls + " w-full"} />
              <p className="text-xs text-gray-400 mt-1">Maximum allowed rent increase per renewal cycle</p>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Min Contract Duration (Years)</label>
              <input type="number" value={minContractYears} onChange={e => setMinContractYears(e.target.value)}
                min="1" max="10" step="1" required className={inputCls + " w-full"} />
              <p className="text-xs text-gray-400 mt-1">Landlords cannot create shorter contracts</p>
            </div>
          </div>
        </SectionCard>

        {/* ── Tax Rule Metadata ─────────────────────────────────────────── */}
        <SectionCard title="Tax Rule Reference">
          <div className="flex items-center gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Proclamation / Version</label>
              <input type="text" value={taxRuleVersion} onChange={e => setTaxRuleVersion(e.target.value)}
                placeholder="e.g. 1395/2025" required className={inputCls + " w-48"} />
            </div>
            <p className="text-xs text-gray-400 mt-4">
              Stored on every declaration so auditors know which law was applied. Update this when a new proclamation takes effect.
            </p>
          </div>
        </SectionCard>

        {/* ── Business Entity Tax ───────────────────────────────────────── */}
        <SectionCard title="Business Entity — Flat Tax Rate">
          <div className="flex items-center gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Flat Rate (%)</label>
              <div className="flex items-center gap-2">
                <input type="number" value={businessRate} onChange={e => setBusinessRate(e.target.value)}
                  min="0" max="100" step="0.5" required className={inputCls + " w-28"} />
                <span className="text-sm text-gray-500">%</span>
              </div>
            </div>
            <p className="text-xs text-gray-400 mt-4">
              Applied to the full annual gross rent for landlords registered as a <strong>Business</strong> entity. No deductions apply.
            </p>
          </div>
        </SectionCard>

        {/* ── Individual Residential Deduction ─────────────────────────── */}
        <SectionCard title="Individual Entity — Residential Maintenance Deduction">
          <div className="flex items-center gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Deduction (%)</label>
              <div className="flex items-center gap-2">
                <input type="number" value={deductionPercent} onChange={e => setDeductionPercent(e.target.value)}
                  min="0" max="100" step="1" required className={inputCls + " w-28"} />
                <span className="text-sm text-gray-500">%</span>
              </div>
            </div>
            <p className="text-xs text-gray-400 mt-4">
              Eligible property types: <strong>House, Apartment Building, Mixed-Use Building</strong>.<br/>
              Formula: <code className="bg-gray-100 px-1 rounded text-xs">Taxable Income = Annual Gross − (Annual Gross × Deduction%)</code>
            </p>
          </div>
        </SectionCard>

        {/* ── Progressive Tax Bands ─────────────────────────────────────── */}
        <SectionCard title="Individual Entity — Progressive Tax Bands">
          <p className="text-xs text-gray-400 mb-4">
            Formula per band: <code className="bg-gray-100 px-1 rounded text-xs">Annual Tax = (Taxable Income × Rate%) − Deductible Amount</code>
            <br/>The deductible amount ensures smooth transitions between brackets. Leave "Max Income" blank for the last (open-ended) band.
          </p>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
                <tr>
                  <th className="px-3 py-2 text-left">Band</th>
                  <th className="px-3 py-2 text-left">Min Income (ETB/year)</th>
                  <th className="px-3 py-2 text-left">Max Income (ETB/year)</th>
                  <th className="px-3 py-2 text-left">Rate (%)</th>
                  <th className="px-3 py-2 text-left">Deductible (ETB)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {bands.map((band, idx) => (
                  <tr key={idx} className="hover:bg-gray-50">
                    <td className="px-3 py-2 font-medium text-gray-600">Band {idx + 1}</td>
                    <td className="px-3 py-2">
                      <input type="number" value={band.minIncome}
                        onChange={e => updateBand(idx, "minIncome", e.target.value)}
                        min="0" step="1" required className={inputCls + " w-36"} />
                    </td>
                    <td className="px-3 py-2">
                      {idx === bands.length - 1
                        ? <span className="text-gray-400 text-xs italic px-3">No limit (open-ended)</span>
                        : <input type="number" value={band.maxIncome}
                            onChange={e => updateBand(idx, "maxIncome", e.target.value)}
                            min="0" step="1" required className={inputCls + " w-36"} />
                      }
                    </td>
                    <td className="px-3 py-2">
                      <div className="flex items-center gap-1">
                        <input type="number" value={band.ratePercent}
                          onChange={e => updateBand(idx, "ratePercent", e.target.value)}
                          min="0" max="100" step="0.5" required className={inputCls + " w-20"} />
                        <span className="text-gray-400">%</span>
                      </div>
                    </td>
                    <td className="px-3 py-2">
                      <input type="number" value={band.deductibleAmount}
                        onChange={e => updateBand(idx, "deductibleAmount", e.target.value)}
                        min="0" step="1" required className={inputCls + " w-28"} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </SectionCard>

        {/* ── Save ─────────────────────────────────────────────────────── */}
        <div className="flex items-center gap-3">
          <button type="submit" disabled={saving}
            className="flex items-center gap-2 bg-primary text-white px-6 py-2.5 rounded-lg text-sm font-semibold hover:bg-blue-900 transition disabled:opacity-50">
            {saving ? <RefreshCw size={15} className="animate-spin" /> : <Save size={15} />}
            {saving ? "Saving..." : "Save All Configuration"}
          </button>
          {config.updatedAt && (
            <p className="text-xs text-gray-400">Last updated: {fmtDate(config.updatedAt)}</p>
          )}
        </div>
      </form>
    </div>
  );
}

// ── Tab 2: Officer Directory ──────────────────────────────────────────────────
function OfficerDirectoryTab({ setToast }) {
  const [officers, setOfficers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [toggling, setToggling] = useState(null); // id of officer being toggled

  const [form, setForm] = useState({
    firstName: "", lastName: "", email: "",
    phoneNumber: "", password: "", subCityZone: "",
  });
  const [creating, setCreating] = useState(false);

  const fetchOfficers = () => {
    setLoading(true);
    adminAPI.getOfficers()
      .then(setOfficers)
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchOfficers(); }, []);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreating(true);
    try {
      const officer = await adminAPI.createOfficer(form);
      setOfficers(prev => [officer, ...prev]);
      setShowModal(false);
      setForm({ firstName: "", lastName: "", email: "", phoneNumber: "", password: "", subCityZone: "" });
      setToast({ msg: `Officer ${officer.firstName} ${officer.lastName} provisioned successfully`, type: "success" });
    } catch (err) {
      const data = err.response?.data;
      const msg = typeof data === "object" && !data?.message
        ? Object.values(data).join(" · ")
        : data?.message || "Failed to create officer";
      setToast({ msg, type: "error" });
    } finally {
      setCreating(false);
    }
  };

  const handleToggle = async (officer) => {
    setToggling(officer.id);
    try {
      const updated = await adminAPI.toggleOfficerStatus(officer.id, !officer.isActive);
      setOfficers(prev => prev.map(o => o.id === updated.id ? updated : o));
      setToast({
        msg: `${updated.firstName} ${updated.lastName} has been ${updated.isActive ? "activated" : "deactivated"}`,
        type: "success",
      });
    } catch {
      setToast({ msg: "Failed to update officer status", type: "error" });
    } finally {
      setToggling(null);
    }
  };

  return (
    <>
      {showModal && (
        <Modal title="Register New Officer" onClose={() => setShowModal(false)}>
          <form onSubmit={handleCreate} className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">First Name *</label>
                <input value={form.firstName} onChange={set("firstName")} required
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Last Name *</label>
                <input value={form.lastName} onChange={set("lastName")} required
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Email *</label>
              <input type="email" value={form.email} onChange={set("email")} required
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Phone Number *</label>
              <input value={form.phoneNumber} onChange={set("phoneNumber")} required placeholder="09XXXXXXXX"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Initial Password *</label>
              <input type="password" value={form.password} onChange={set("password")} required minLength={6}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Assigned Sub-city *</label>
              <select value={form.subCityZone} onChange={set("subCityZone")} required
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
                <option value="">Select sub-city</option>
                {SUB_CITIES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <div className="flex gap-3 pt-2">
              <button type="button" onClick={() => setShowModal(false)}
                className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">
                Cancel
              </button>
              <button type="submit" disabled={creating}
                className="flex-1 bg-primary text-white py-2 rounded-lg text-sm font-medium
                  hover:bg-blue-900 disabled:opacity-50">
                {creating ? "Creating..." : "Create Officer"}
              </button>
            </div>
          </form>
        </Modal>
      )}

      <div className="flex justify-between items-center mb-5">
        <div>
          <h3 className="text-base font-semibold text-gray-800">Government Officers</h3>
          <p className="text-sm text-gray-500 mt-0.5">{officers.length} officers registered</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-primary text-white px-5 py-2.5 rounded-lg
            text-sm font-medium hover:bg-blue-900 transition"
        >
          <Plus size={16} /> Register Officer
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        {loading ? (
          <div className="px-6 py-12 text-center text-gray-400 text-sm">Loading officers...</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="px-6 py-3 text-left">Officer</th>
                <th className="px-6 py-3 text-left">Email</th>
                <th className="px-6 py-3 text-left">Sub-city</th>
                <th className="px-6 py-3 text-left">Status</th>
                <th className="px-6 py-3 text-left">Joined</th>
                <th className="px-6 py-3 text-left">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {officers.map(o => (
                <tr key={o.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <div className="w-7 h-7 rounded-full bg-primary flex items-center justify-center
                        text-white text-xs font-bold select-none flex-shrink-0">
                        {o.firstName?.[0]}{o.lastName?.[0]}
                      </div>
                      <span className="font-medium text-gray-900">{o.firstName} {o.lastName}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-gray-600">{o.email}</td>
                  <td className="px-6 py-4">
                    <span className="text-xs bg-blue-50 text-primary px-2 py-1 rounded-full font-medium">
                      {o.subCityZone || "—"}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    {o.isActive
                      ? <span className="flex items-center gap-1 text-success text-xs font-medium">
                          <CheckCircle size={13} /> Active
                        </span>
                      : <span className="flex items-center gap-1 text-danger text-xs font-medium">
                          <XCircle size={13} /> Inactive
                        </span>}
                  </td>
                  <td className="px-6 py-4 text-gray-500">{fmtDate(o.createdAt)}</td>
                  <td className="px-6 py-4">
                    <button
                      onClick={() => handleToggle(o)}
                      disabled={toggling === o.id}
                      className={`px-3 py-1.5 rounded-lg text-xs font-medium transition disabled:opacity-50
                        ${o.isActive
                          ? "bg-red-50 text-danger hover:bg-red-100"
                          : "bg-green-50 text-success hover:bg-green-100"}`}
                    >
                      {toggling === o.id ? "..." : o.isActive ? "Deactivate" : "Activate"}
                    </button>
                  </td>
                </tr>
              ))}
              {officers.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-10 text-center text-gray-400">
                    No officers registered yet. Click "Register Officer" to add one.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}

// ── Tab 3: Global Metrics ─────────────────────────────────────────────────────
function GlobalMetricsTab() {
  return (
    <div className="max-w-3xl">
      <SectionCard title="Platform Overview">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
          <StatBox label="Total Officers" value="—" sub="Across all sub-cities" />
          <StatBox label="Registered Properties" value="—" sub="All landlords" />
          <StatBox label="Active Contracts" value="—" sub="System-wide" />
          <StatBox label="Pending Appeals" value="—" sub="Awaiting review" />
        </div>
        <div className="flex items-center gap-2 p-4 bg-blue-50 border border-blue-200 rounded-lg">
          <ShieldCheck size={18} className="text-primary flex-shrink-0" />
          <p className="text-sm text-blue-700">
            City-wide aggregate metrics endpoint (<code className="text-xs bg-blue-100 px-1 rounded">GET /api/analytics/city-summary</code>) is planned for a future release.
            Connect this tab once the backend endpoint is available.
          </p>
        </div>
      </SectionCard>
    </div>
  );
}
