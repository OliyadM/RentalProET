// Property Detail will go here
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Plus, CheckCircle, Clock, ParkingCircle, ArrowUp } from "lucide-react";
import Layout from "../../components/Layout";
import { propertiesAPI, unitsAPI } from "../../services/api";

export default function PropertyDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [property, setProperty] = useState(null);
  const [units, setUnits] = useState([]);

  useEffect(() => {
    propertiesAPI.getById(id).then(setProperty);
    unitsAPI.getByProperty(id).then(setUnits);
  }, [id]);

  if (!property) return <Layout><p className="text-gray-400">Loading...</p></Layout>;

  return (
    <Layout>
      <div className="flex justify-between items-start mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{property.propertyName}</h2>
          <p className="text-gray-500 text-sm mt-1">{property.address}, {property.subCity}</p>
        </div>
        <button onClick={() => navigate(`/landlord/units/add/${id}`)}
          className="flex items-center gap-2 bg-primary text-white px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900 transition">
          <Plus size={16} /> Add Unit
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
          <div><p className="text-gray-400 text-xs mb-1">Property Type</p><p className="font-medium">{property.propertyType.replace("_"," ")}</p></div>
          <div><p className="text-gray-400 text-xs mb-1">Sub-city</p><p className="font-medium">{property.subCity}</p></div>
          <div><p className="text-gray-400 text-xs mb-1">Woreda</p><p className="font-medium">{property.woreda}</p></div>
          <div><p className="text-gray-400 text-xs mb-1">Total Area</p><p className="font-medium">{property.totalArea} m²</p></div>
          <div><p className="text-gray-400 text-xs mb-1">Year Built</p><p className="font-medium">{property.yearBuilt || "—"}</p></div>
          <div><p className="text-gray-400 text-xs mb-1">Status</p>
            <div className="flex items-center gap-1">
              {property.isVerified
                ? <><CheckCircle size={14} className="text-success" /><span className="text-success font-medium text-xs">Verified</span></>
                : <><Clock size={14} className="text-accent" /><span className="text-accent font-medium text-xs">Pending</span></>}
            </div>
          </div>
          {property.latitude && (
            <div><p className="text-gray-400 text-xs mb-1">GPS</p><p className="font-medium">{property.latitude}, {property.longitude}</p></div>
          )}
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm">
        <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
          <h3 className="font-semibold text-gray-800">Units ({units.length})</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="px-6 py-3 text-left">Unit #</th>
                <th className="px-6 py-3 text-left">Floor</th>
                <th className="px-6 py-3 text-left">Area (m²)</th>
                <th className="px-6 py-3 text-left">Rooms</th>
                <th className="px-6 py-3 text-left">Features</th>
                <th className="px-6 py-3 text-left">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {units.map(u => (
                <tr key={u.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-medium">{u.unitNumber}</td>
                  <td className="px-6 py-4 text-gray-600">Floor {u.floorLevel}</td>
                  <td className="px-6 py-4">{u.floorArea} m²</td>
                  <td className="px-6 py-4">{u.numberOfRooms} rooms</td>
                  <td className="px-6 py-4">
                    <div className="flex gap-1 flex-wrap">
                      {u.hasParking && <span className="text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded-full">Parking</span>}
                      {u.hasElevator && <span className="text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded-full">Elevator</span>}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <button onClick={() => navigate(`/landlord/contracts?unitId=${u.id}`)}
                      className="text-primary text-xs hover:underline font-medium">
                      View Contracts
                    </button>
                  </td>
                </tr>
              ))}
              {units.length === 0 && (
                <tr><td colSpan={6} className="px-6 py-8 text-center text-gray-400">No units yet. Add a unit above.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </Layout>
  );
}