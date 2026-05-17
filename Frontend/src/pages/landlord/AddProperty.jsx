// Add Property will go here
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import { propertiesAPI } from "../../services/api";
import { useAuth } from "../../context/AuthContext";
import { MapPin } from "lucide-react";

const subCities = ["Bole","Kirkos","Yeka","Arada","Lideta","Kolfe","Nifas Silk","Akaky"];

export default function AddProperty() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [toast, setToast] = useState(null);
  const [form, setForm] = useState({
    propertyName: "", address: "", subCity: "Bole", woreda: "",
    propertyType: "RESIDENTIAL", totalArea: "", yearBuilt: "",
    latitude: "", longitude: "",
  });

  const set = f => e => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Convert string values to proper types
      const propertyData = {
        propertyName: form.propertyName,
        address: form.address,
        subCity: form.subCity,
        woreda: form.woreda,
        propertyType: form.propertyType,
        totalArea: form.totalArea ? parseFloat(form.totalArea) : null,
        yearBuilt: form.yearBuilt ? parseInt(form.yearBuilt) : null,
        latitude: form.latitude ? parseFloat(form.latitude) : null,
        longitude: form.longitude ? parseFloat(form.longitude) : null,
      };
      
      await propertiesAPI.create(propertyData);
      setToast("Property registered successfully");
      setTimeout(() => navigate("/landlord/properties"), 1500);
    } catch (error) {
      console.error("Error creating property:", error);
      setToast("Failed to create property. Please try again.");
    }
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}
      <div className="max-w-2xl">
        <h2 className="text-2xl font-bold text-gray-900 mb-1">Add New Property</h2>
        <p className="text-gray-500 text-sm mb-6">Fill in all required details for your property</p>

        <div className="bg-white rounded-xl shadow-sm p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Property Name *</label>
              <input value={form.propertyName} onChange={set("propertyName")} required
                placeholder="e.g. Bole Apartments Building A"
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Address *</label>
              <input value={form.address} onChange={set("address")} required
                placeholder="Street address"
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Sub-city *</label>
                <select value={form.subCity} onChange={set("subCity")}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
                  {subCities.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Woreda *</label>
                <input value={form.woreda} onChange={set("woreda")} required placeholder="e.g. Woreda 03"
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Property Type *</label>
                <select value={form.propertyType} onChange={set("propertyType")}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary">
                  <option value="RESIDENTIAL">Residential</option>
                  <option value="COMMERCIAL">Commercial</option>
                  <option value="MIXED_USE">Mixed Use</option>
                  <option value="INDUSTRIAL">Industrial</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Total Area (m²) *</label>
                <input type="number" value={form.totalArea} onChange={set("totalArea")} required min="1"
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Year Built</label>
              <input type="number" value={form.yearBuilt} onChange={set("yearBuilt")} min="1900" max="2024"
                placeholder="e.g. 2010"
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Latitude (optional)</label>
                <input value={form.latitude} onChange={set("latitude")} placeholder="9.0054"
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Longitude (optional)</label>
                <input value={form.longitude} onChange={set("longitude")} placeholder="38.7636"
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
              </div>
            </div>
            <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-lg border border-dashed border-gray-300">
              <MapPin size={20} className="text-gray-400" />
              <p className="text-sm text-gray-500">Map integration coming soon — enter coordinates manually above</p>
            </div>
            <div className="flex gap-3 pt-2">
              <button type="button" onClick={() => navigate("/landlord/properties")}
                className="flex-1 border border-gray-300 text-gray-700 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50 transition">
                Cancel
              </button>
              <button type="submit"
                className="flex-1 bg-primary text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900 transition">
                Register Property
              </button>
            </div>
          </form>
        </div>
      </div>
    </Layout>
  );
}