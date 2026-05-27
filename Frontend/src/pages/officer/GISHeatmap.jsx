import { useEffect, useState } from "react";
import { MapContainer, TileLayer, CircleMarker, Popup, useMap } from "react-leaflet";
import { Layers, Filter, TrendingUp, AlertTriangle } from "lucide-react";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import { analyticsAPI } from "../../services/api";
import { useAuth } from "../../context/AuthContext";
import "leaflet/dist/leaflet.css";

// Fix Leaflet default icon issue with Vite
import L from "leaflet";
import icon from "leaflet/dist/images/marker-icon.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});
L.Marker.prototype.options.icon = DefaultIcon;

const PROPERTY_TYPES = ["APARTMENT", "VILLA", "CONDO", "HOUSE", "COMMERCIAL"];

// Sub-city coordinates (matching backend AddisAbabaNeighborhoods.java)
const SUB_CITY_COORDINATES = {
  "Bole": { lat: 9.0054, lng: 38.7636 },
  "Ayat": { lat: 9.0417, lng: 38.8312 },
  "Megenagna": { lat: 9.0192, lng: 38.7869 },
  "Piassa": { lat: 9.0320, lng: 38.7469 },
  "Merkato": { lat: 9.0320, lng: 38.7200 },
  "CMC": { lat: 8.9806, lng: 38.7578 },
  "Gerji": { lat: 9.0450, lng: 38.7950 },
  "Kazanchis": { lat: 9.0250, lng: 38.7600 },
  "Sarbet": { lat: 9.0100, lng: 38.7400 },
  "Lebu": { lat: 8.9500, lng: 38.7200 },
  "Addis Ketema": { lat: 9.0350, lng: 38.7300 },
  "Akaky Kaliti": { lat: 8.8800, lng: 38.7800 },
  "Arada": { lat: 9.0350, lng: 38.7450 },
  "Gulele": { lat: 9.0500, lng: 38.7300 },
  "Kirkos": { lat: 8.9950, lng: 38.7550 },
  "Kolfe Keranio": { lat: 8.9900, lng: 38.6900 },
  "Lideta": { lat: 9.0200, lng: 38.7300 },
  "Nifas Silk-Lafto": { lat: 8.9500, lng: 38.7400 },
  "Yeka": { lat: 9.0450, lng: 38.8100 }
};

// Default center (Addis Ababa - Meskel Square)
const DEFAULT_CENTER = { lat: 9.0320, lng: 38.7469 };

// Map center component to handle zoom/pan
function MapUpdater({ center, zoom }) {
  const map = useMap();
  useEffect(() => {
    if (center && zoom) {
      map.setView(center, zoom);
    }
  }, [center, zoom, map]);
  return null;
}

export default function GISHeatmap() {
  const { user } = useAuth();
  const [layer, setLayer] = useState("rent"); // rent | anomaly
  const [heatmapData, setHeatmapData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);
  
  // Filters
  const [propertyType, setPropertyType] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [minSeverity, setMinSeverity] = useState(0);

  // Get officer's assigned sub-city and coordinates
  const officerSubCity = user?.subCityZone || null;
  const subCityCoords = officerSubCity ? SUB_CITY_COORDINATES[officerSubCity] : null;
  
  // Map center and zoom based on officer's sub-city
  const mapCenter = subCityCoords 
    ? [subCityCoords.lat, subCityCoords.lng] 
    : [DEFAULT_CENTER.lat, DEFAULT_CENTER.lng];
  const mapZoom = subCityCoords ? 14 : 12;

  useEffect(() => {
    loadHeatmapData();
  }, [layer, propertyType, startDate, endDate, minSeverity]);

  const loadHeatmapData = async () => {
    setLoading(true);
    try {
      const filters = {
        propertyType: propertyType || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        minSeverity: layer === "anomaly" && minSeverity > 0 ? minSeverity : undefined,
        subCity: officerSubCity || undefined, // Auto-filter by officer's sub-city
      };

      let data;
      if (layer === "rent") {
        data = await analyticsAPI.getRentDensityHeatmap(filters);
      } else {
        data = await analyticsAPI.getAnomalyConcentration(filters);
      }
      
      setHeatmapData(data);
    } catch (error) {
      setToast("Failed to load heatmap data");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // Color scale for rent density (green to red)
  const getRentColor = (avgRent) => {
    if (avgRent < 5000) return "#10b981"; // green
    if (avgRent < 10000) return "#84cc16"; // lime
    if (avgRent < 15000) return "#eab308"; // yellow
    if (avgRent < 20000) return "#f97316"; // orange
    return "#ef4444"; // red
  };

  // Color scale for anomaly concentration (yellow to red)
  const getAnomalyColor = (severity) => {
    if (severity < 0.3) return "#fbbf24"; // yellow
    if (severity < 0.5) return "#f97316"; // orange
    if (severity < 0.7) return "#ef4444"; // red
    return "#dc2626"; // dark red
  };

  // Radius based on count
  const getRadius = (count) => {
    return Math.min(Math.max(count * 3, 8), 30);
  };

  const clearFilters = () => {
    setPropertyType("");
    setStartDate("");
    setEndDate("");
    setMinSeverity(0);
  };

  return (
    <Layout>
      {toast && <Toast message={toast} onClose={() => setToast(null)} />}

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">GIS Compliance Heatmap</h2>
        <p className="text-gray-500 text-sm mt-1">
          Geographic visualization of rent density and anomaly concentration
          {officerSubCity && (
            <span className="ml-2 text-primary font-medium">
              • Viewing: {officerSubCity}
            </span>
          )}
        </p>
      </div>

      {/* Controls */}
      <div className="bg-white rounded-xl shadow-sm p-4 mb-5">
        {/* Layer Toggle */}
        <div className="flex items-center gap-2 mb-4">
          <Layers size={18} className="text-gray-400" />
          <h3 className="font-semibold text-gray-800">Map Layer</h3>
        </div>
        
        <div className="flex gap-3 mb-4">
          <button
            onClick={() => setLayer("rent")}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-sm font-medium transition ${
              layer === "rent"
                ? "bg-primary text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}>
            <TrendingUp size={16} />
            Rent Density
          </button>
          <button
            onClick={() => setLayer("anomaly")}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-sm font-medium transition ${
              layer === "anomaly"
                ? "bg-danger text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}>
            <AlertTriangle size={16} />
            Anomaly Concentration
          </button>
        </div>

        {/* Filters */}
        <div className="flex items-center gap-2 mb-3">
          <Filter size={18} className="text-gray-400" />
          <h3 className="font-semibold text-gray-800">Filters</h3>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          {/* Assigned Sub-City (Read-only) */}
          {officerSubCity && (
            <div className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-gray-50 flex items-center justify-between">
              <span className="text-gray-700">{officerSubCity}</span>
              <span className="text-xs text-gray-500">Assigned</span>
            </div>
          )}

          <select
            value={propertyType}
            onChange={(e) => setPropertyType(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary">
            <option value="">All Property Types</option>
            {PROPERTY_TYPES.map(type => (
              <option key={type} value={type}>{type.replace("_", " ")}</option>
            ))}
          </select>

          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            placeholder="Start Date"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary"
          />

          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            placeholder="End Date"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary"
          />

          {layer === "anomaly" && (
            <div className="flex items-center gap-2">
              <label className="text-xs text-gray-600 whitespace-nowrap">Min Severity:</label>
              <input
                type="range"
                min="0"
                max="1"
                step="0.1"
                value={minSeverity}
                onChange={(e) => setMinSeverity(parseFloat(e.target.value))}
                className="flex-1"
              />
              <span className="text-xs font-medium text-gray-700 w-8">{minSeverity.toFixed(1)}</span>
            </div>
          )}
        </div>

        {(propertyType || startDate || endDate || minSeverity > 0) && (
          <div className="mt-3 flex items-center justify-between">
            <div className="flex items-center gap-2 text-xs">
              <span className="text-gray-500">Active filters:</span>
              {officerSubCity && <span className="bg-blue-50 text-primary px-2 py-1 rounded">{officerSubCity}</span>}
              {propertyType && <span className="bg-blue-50 text-primary px-2 py-1 rounded">{propertyType}</span>}
              {startDate && <span className="bg-blue-50 text-primary px-2 py-1 rounded">From: {startDate}</span>}
              {endDate && <span className="bg-blue-50 text-primary px-2 py-1 rounded">To: {endDate}</span>}
              {minSeverity > 0 && <span className="bg-blue-50 text-primary px-2 py-1 rounded">Severity ≥ {minSeverity.toFixed(1)}</span>}
            </div>
            <button
              onClick={clearFilters}
              className="text-danger text-xs hover:underline">
              Clear all
            </button>
          </div>
        )}
      </div>

      {/* Legend */}
      <div className="bg-white rounded-xl shadow-sm p-4 mb-5">
        <h3 className="font-semibold text-gray-800 mb-3 text-sm">Legend</h3>
        {layer === "rent" ? (
          <div className="flex items-center gap-4 text-xs">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#10b981" }}></div>
              <span>&lt; 5k ETB</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#84cc16" }}></div>
              <span>5k-10k</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#eab308" }}></div>
              <span>10k-15k</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#f97316" }}></div>
              <span>15k-20k</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#ef4444" }}></div>
              <span>&gt; 20k ETB</span>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-4 text-xs">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#fbbf24" }}></div>
              <span>Low (0.0-0.3)</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#f97316" }}></div>
              <span>Medium (0.3-0.5)</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#ef4444" }}></div>
              <span>High (0.5-0.7)</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded-full" style={{ backgroundColor: "#dc2626" }}></div>
              <span>Critical (&gt; 0.7)</span>
            </div>
            <span className="text-gray-500 ml-4">• Circle size = anomaly count</span>
          </div>
        )}
      </div>

      {/* Map */}
      <div className="bg-white rounded-xl shadow-sm overflow-hidden" style={{ height: "600px" }}>
        {loading ? (
          <div className="flex items-center justify-center h-full text-gray-400">
            Loading map data...
          </div>
        ) : heatmapData.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-400">
            <Layers size={48} className="mb-3 opacity-30" />
            <p>No data available for selected filters</p>
            <p className="text-sm mt-2">Try adjusting your filters or date range</p>
          </div>
        ) : (
          <MapContainer
            center={mapCenter}
            zoom={mapZoom}
            style={{ height: "100%", width: "100%" }}
            scrollWheelZoom={true}>
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <MapUpdater center={mapCenter} zoom={mapZoom} />
            
            {heatmapData.map((point, idx) => (
              <CircleMarker
                key={idx}
                center={[point.latitude, point.longitude]}
                radius={layer === "rent" ? getRadius(point.propertyCount) : getRadius(point.anomalyCount)}
                fillColor={layer === "rent" ? getRentColor(point.averageRent) : getAnomalyColor(point.averageSeverity)}
                fillOpacity={0.6}
                color="#fff"
                weight={2}>
                <Popup>
                  <div className="text-sm">
                    <p className="font-bold text-gray-900 mb-2">{point.subCity}</p>
                    {layer === "rent" ? (
                      <>
                        <p className="text-gray-600">Properties: <strong>{point.propertyCount}</strong></p>
                        <p className="text-gray-600">Avg Rent: <strong className="text-primary">{point.averageRent?.toFixed(0)} ETB</strong></p>
                        <p className="text-xs text-gray-500 mt-1">
                          {point.hasExactGPS ? "📍 Exact GPS" : "📌 Neighborhood centroid"}
                        </p>
                      </>
                    ) : (
                      <>
                        <p className="text-gray-600">Anomalies: <strong className="text-danger">{point.anomalyCount}</strong></p>
                        <p className="text-gray-600">Total Declarations: <strong>{point.totalDeclarations}</strong></p>
                        <p className="text-gray-600">Avg Severity: <strong>{point.averageSeverity?.toFixed(2)}</strong></p>
                        <p className="text-xs text-gray-500 mt-1">
                          {point.hasExactGPS ? "📍 Exact GPS" : "📌 Neighborhood centroid"}
                        </p>
                      </>
                    )}
                  </div>
                </Popup>
              </CircleMarker>
            ))}
          </MapContainer>
        )}
      </div>

      {/* Stats Summary */}
      {heatmapData.length > 0 && (
        <div className="mt-5 bg-white rounded-xl shadow-sm p-4">
          <h3 className="font-semibold text-gray-800 mb-3 text-sm">Summary Statistics</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <p className="text-gray-500 text-xs">Total Locations</p>
              <p className="font-bold text-lg">{heatmapData.length}</p>
            </div>
            {layer === "rent" ? (
              <>
                <div>
                  <p className="text-gray-500 text-xs">Total Properties</p>
                  <p className="font-bold text-lg">
                    {heatmapData.reduce((sum, p) => sum + p.propertyCount, 0)}
                  </p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Avg Rent (All)</p>
                  <p className="font-bold text-lg text-primary">
                    {(heatmapData.reduce((sum, p) => sum + p.averageRent, 0) / heatmapData.length).toFixed(0)} ETB
                  </p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Highest Avg Rent</p>
                  <p className="font-bold text-lg text-danger">
                    {Math.max(...heatmapData.map(p => p.averageRent)).toFixed(0)} ETB
                  </p>
                </div>
              </>
            ) : (
              <>
                <div>
                  <p className="text-gray-500 text-xs">Total Anomalies</p>
                  <p className="font-bold text-lg text-danger">
                    {heatmapData.reduce((sum, p) => sum + p.anomalyCount, 0)}
                  </p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Avg Severity</p>
                  <p className="font-bold text-lg">
                    {(heatmapData.reduce((sum, p) => sum + p.averageSeverity, 0) / heatmapData.length).toFixed(2)}
                  </p>
                </div>
                <div>
                  <p className="text-gray-500 text-xs">Highest Concentration</p>
                  <p className="font-bold text-lg text-danger">
                    {Math.max(...heatmapData.map(p => p.anomalyCount))} anomalies
                  </p>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </Layout>
  );
}
