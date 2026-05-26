// Landlord Properties will go here
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, CheckCircle, Clock, Building } from "lucide-react";
import Layout from "../../components/Layout";
import { useAuth } from "../../context/AuthContext";
import { propertiesAPI, unitsAPI } from "../../services/api";

export default function LandlordProperties() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [properties, setProperties] = useState([]);
  const [unitCounts, setUnitCounts] = useState({});

  useEffect(() => {
    propertiesAPI.getMyProperties(user.id).then(ps => {
      setProperties(ps);
      ps.forEach(p => {
        unitsAPI.getByProperty(p.id).then(us => {
          setUnitCounts(prev => ({ ...prev, [p.id]: us.length }));
        });
      });
    });
  }, [user.id]);

  return (
    <Layout>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">My Properties</h2>
          <p className="text-gray-500 text-sm mt-1">{properties.length} properties registered</p>
        </div>
        <button onClick={() => navigate("/landlord/properties/add")}
          className="flex items-center gap-2 bg-primary text-white px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900 transition">
          <Plus size={16} /> Add Property
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
        {properties.map(p => (
          <div key={p.id} className="bg-white rounded-xl shadow-sm p-5 hover:shadow-md transition">
            <div className="flex justify-between items-start mb-3">
              <div>
                <h3 className="font-semibold text-gray-900">{p.propertyName}</h3>
                <p className="text-xs text-gray-500 mt-0.5">{p.address}</p>
              </div>
              <span className="text-xs bg-blue-50 text-primary px-2 py-1 rounded-full font-medium">
                {p.propertyType.replace("_", " ")}
              </span>
            </div>
            <p className="text-sm text-gray-600 mb-1">{p.subCity} — {p.woreda}</p>
            <div className="flex items-center gap-1 mb-4">
              {p.status === 'ACTIVE'
                ? <><CheckCircle size={14} className="text-success" /><span className="text-xs text-success font-medium">Active</span></>
                : p.status === 'REJECTED'
                ? <><Clock size={14} className="text-red-500" /><span className="text-xs text-red-500 font-medium">Rejected</span></>
                : <><Clock size={14} className="text-accent" /><span className="text-xs text-accent font-medium">Pending Officer Review</span></>
              }
            </div>
            <p className="text-xs text-gray-500 mb-4">{unitCounts[p.id] || 0} units registered</p>
            <div className="flex gap-2">
              <button onClick={() => navigate(`/landlord/properties/${p.id}`)}
                className="flex-1 text-sm border border-primary text-primary py-2 rounded-lg hover:bg-blue-50 transition font-medium">
                View Details
              </button>
              <button 
                onClick={() => navigate(`/landlord/units/add/${p.id}`)}
                disabled={p.status !== 'ACTIVE'}
                className={`flex-1 text-sm py-2 rounded-lg transition font-medium ${
                  p.status === 'ACTIVE' 
                    ? 'bg-primary text-white hover:bg-blue-900' 
                    : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                }`}
                title={p.status !== 'ACTIVE' ? 'Property must be verified by an officer before adding units' : ''}>
                Add Unit
              </button>
            </div>
          </div>
        ))}
        {properties.length === 0 && (
          <div className="col-span-3 text-center py-16 text-gray-400">
            <Building size={40} className="mx-auto mb-3 opacity-30" />
            <p>No properties yet. Add your first property.</p>
          </div>
        )}
      </div>
    </Layout>
  );
}