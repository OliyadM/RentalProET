// Officer Properties will go here
import { useEffect, useState } from "react";
import { CheckCircle } from "lucide-react";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import Modal from "../../components/Modal";
import { propertiesAPI } from "../../services/api";

const subCities = ["All","Bole","Kirkos","Yeka","Arada","Lideta","Kolfe","Nifas Silk","Akaky"];

export default function OfficerProperties() {
  const [properties, setProperties] = useState([]);
  const [filter, setFilter] = useState("All");
  const [toast, setToast] = useState(null);
  const [verifying, setVerifying] = useState(null);

  useEffect(() => {
    propertiesAPI.getAll().then(setProperties);
  }, []);

  const filtered = filter === "All" ? properties : properties.filter(p => p.subCity === filter);

  const verify = async () => {
    await propertiesAPI.verify(verifying.id);
    setProperties(prev => prev.map(p => p.id === verifying.id ? { ...p, isVerified: true } : p));
    setVerifying(null);
    setToast("Property verified successfully");
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}
      {verifying && (
        <Modal title="Verify Property" onClose={() => setVerifying(null)}>
          <p className="text-sm text-gray-600 mb-4">
            Confirm verification of <strong>{verifying.propertyName}</strong> in {verifying.subCity}?
          </p>
          <div className="flex gap-3">
            <button onClick={() => setVerifying(null)}
              className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg text-sm">Cancel</button>
            <button onClick={verify}
              className="flex-1 bg-success text-white py-2 rounded-lg text-sm font-medium">Verify Property</button>
          </div>
        </Modal>
      )}

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Properties</h2>
        <p className="text-gray-500 text-sm mt-1">{filtered.length} properties</p>
      </div>

      <div className="flex gap-2 mb-5 overflow-x-auto">
        {subCities.map(s => (
          <button key={s} onClick={() => setFilter(s)}
            className={`px-4 py-2 rounded-lg text-xs font-medium whitespace-nowrap transition
              ${filter === s ? "bg-primary text-white" : "bg-white text-gray-600 border border-gray-200 hover:border-primary hover:text-primary"}`}>
            {s}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
            <tr>
              <th className="px-6 py-3 text-left">Property</th>
              <th className="px-6 py-3 text-left">Owner</th>
              <th className="px-6 py-3 text-left">Sub-city</th>
              <th className="px-6 py-3 text-left">Type</th>
              <th className="px-6 py-3 text-left">Status</th>
              <th className="px-6 py-3 text-left">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {filtered.map(p => (
              <tr key={p.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <p className="font-medium text-gray-900">{p.propertyName}</p>
                  <p className="text-xs text-gray-500">{p.address}</p>
                </td>
                <td className="px-6 py-4 text-gray-600">{p.ownerName}</td>
                <td className="px-6 py-4">{p.subCity}</td>
                <td className="px-6 py-4">
                  <span className="text-xs bg-blue-50 text-primary px-2 py-1 rounded-full">
                    {p.propertyType.replace("_"," ")}
                  </span>
                </td>
                <td className="px-6 py-4">
                  {p.isVerified
                    ? <span className="flex items-center gap-1 text-success text-xs font-medium"><CheckCircle size={13} /> Verified</span>
                    : <span className="text-xs text-accent font-medium">Pending</span>}
                </td>
                <td className="px-6 py-4">
                  {!p.isVerified && (
                    <button onClick={() => setVerifying(p)}
                      className="bg-success text-white px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-green-700">
                      Verify
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}