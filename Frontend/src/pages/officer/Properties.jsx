// Officer Properties will go here
import { useEffect, useState } from "react";
import { CheckCircle } from "lucide-react";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import Modal from "../../components/Modal";
import { propertiesAPI } from "../../services/api";
import { useAuth } from "../../context/AuthContext";

export default function OfficerProperties() {
  const { user } = useAuth();
  const subCity = user?.subCityZone || "";

  const [properties, setProperties] = useState([]);
  const [toast, setToast] = useState(null);
  const [verifying, setVerifying] = useState(null);

  useEffect(() => {
    if (!subCity) return;
    propertiesAPI.getBySubCity(subCity).then(setProperties);
  }, [subCity]);

  const verify = async () => {
    await propertiesAPI.verify(verifying.id);
    setProperties(prev =>
      prev.map(p => p.id === verifying.id ? { ...p, status: 'ACTIVE' } : p)
    );
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
        <p className="text-gray-500 text-sm mt-1">
          {properties.length} properties in <span className="font-medium text-primary">{subCity}</span>
        </p>
      </div>

      {!subCity && (
        <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 text-sm rounded-lg px-4 py-3 mb-5">
          No sub-city assigned to your account. Contact an administrator.
        </div>
      )}

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
            {properties.map(p => (
              <tr key={p.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <p className="font-medium text-gray-900">{p.propertyName}</p>
                  <p className="text-xs text-gray-500">{p.address}</p>
                </td>
                <td className="px-6 py-4 text-gray-600">{p.ownerName}</td>
                <td className="px-6 py-4">{p.subCity}</td>
                <td className="px-6 py-4">
                  <span className="text-xs bg-blue-50 text-primary px-2 py-1 rounded-full">
                    {p.propertyType.replace("_", " ")}
                  </span>
                </td>
                <td className="px-6 py-4">
                  {p.status === 'ACTIVE'
                    ? <span className="flex items-center gap-1 text-success text-xs font-medium"><CheckCircle size={13} /> Active</span>
                    : p.status === 'REJECTED'
                    ? <span className="text-xs text-red-500 font-medium">Rejected</span>
                    : <span className="text-xs text-accent font-medium">Pending Review</span>}
                </td>
                <td className="px-6 py-4">
                  {p.status !== 'ACTIVE' && (
                    <button onClick={() => setVerifying(p)}
                      className="bg-success text-white px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-green-700">
                      Verify
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {properties.length === 0 && subCity && (
              <tr>
                <td colSpan={6} className="px-6 py-10 text-center text-gray-400">
                  No properties found in {subCity}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}