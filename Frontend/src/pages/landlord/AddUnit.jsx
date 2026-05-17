// Add Unit will go here
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import { unitsAPI } from "../../services/api";

export default function AddUnit() {
  const { propertyId } = useParams();
  const navigate = useNavigate();
  const [toast, setToast] = useState(null);
  const [form, setForm] = useState({
    unitNumber: "", floorArea: "", floorLevel: "", numberOfRooms: "",
    hasParking: false, hasElevator: false, amenities: "",
  });

  const set = f => e => setForm({ ...form, [f]: e.target.value });
  const toggle = f => () => setForm({ ...form, [f]: !form[f] });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const unitData = {
        unitNumber: form.unitNumber,
        floorArea: parseFloat(form.floorArea),
        floorLevel: parseInt(form.floorLevel),
        numberOfRooms: parseInt(form.numberOfRooms),
        hasParking: form.hasParking,
        hasElevator: form.hasElevator,
        amenities: form.amenities,
      };
      
      await unitsAPI.create(propertyId, unitData);
      setToast("Unit added successfully");
      setTimeout(() => navigate(`/landlord/properties/${propertyId}`), 1500);
    } catch (error) {
      console.error("Error creating unit:", error);
      setToast("Failed to add unit. Please try again.");
    }
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}
      <div className="max-w-xl">
        <h2 className="text-2xl font-bold text-gray-900 mb-1">Add New Unit</h2>
        <p className="text-gray-500 text-sm mb-6">Add a rental unit to this property</p>

        <div className="bg-white rounded-xl shadow-sm p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Unit Number *</label>
              <input value={form.unitNumber} onChange={set("unitNumber")} required placeholder="e.g. A-101"
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Floor Area (m²) *</label>
                <input type="number" value={form.floorArea} onChange={set("floorArea")} required min="1"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Floor Level *</label>
                <input type="number" value={form.floorLevel} onChange={set("floorLevel")} required min="0"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Rooms *</label>
                <input type="number" value={form.numberOfRooms} onChange={set("numberOfRooms")} required min="1"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
            </div>
            <div className="flex gap-6">
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" checked={form.hasParking} onChange={toggle("hasParking")}
                  className="w-4 h-4 rounded accent-primary" />
                <span className="text-sm text-gray-700">Has Parking</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" checked={form.hasElevator} onChange={toggle("hasElevator")}
                  className="w-4 h-4 rounded accent-primary" />
                <span className="text-sm text-gray-700">Has Elevator</span>
              </label>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Amenities</label>
              <input value={form.amenities} onChange={set("amenities")} placeholder="Water, Electricity, Internet"
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              <p className="text-xs text-gray-400 mt-1">Comma separated list</p>
            </div>
            <div className="flex gap-3 pt-2">
              <button type="button" onClick={() => navigate(`/landlord/properties/${propertyId}`)}
                className="flex-1 border border-gray-300 text-gray-700 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50">Cancel</button>
              <button type="submit"
                className="flex-1 bg-primary text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900">Add Unit</button>
            </div>
          </form>
        </div>
      </div>
    </Layout>
  );
}