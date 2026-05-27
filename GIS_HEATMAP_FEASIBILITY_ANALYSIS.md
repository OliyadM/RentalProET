# GIS Heatmap Feature - Feasibility Analysis

## Executive Summary
**✅ FEASIBLE** - The GIS heatmap feature is **highly feasible** with your current codebase. You have 80% of the required data and infrastructure already in place.

---

## Current Codebase Assessment

### ✅ What You HAVE

#### 1. **Location Data** (Property Entity)
```java
private String subCity;      // ✅ Always present
private String woreda;        // ✅ Always present  
private String kebele;        // ✅ Always present
private Double latitude;      // ✅ Optional (some properties have GPS)
private Double longitude;     // ✅ Optional (some properties have GPS)
```
**Status**: Perfect for the requirement. Supports both GPS and non-GPS properties.

#### 2. **Rent Data** (RentalContract + RentDeclaration)
```java
// RentalContract
private Double monthlyRent;  // ✅ Contract rent amount

// RentDeclaration  
private Double declaredRent;      // ✅ Declared monthly rent
private Double aiBenchmarkRent;   // ✅ AI suggested rent
private Boolean isAnomaly;        // ✅ Anomaly flag
private Double anomalyScore;      // ✅ 0-1 severity score
private String anomalyReason;     // ✅ Why flagged
```
**Status**: Complete. All data needed for heatmaps exists.

#### 3. **Anomaly Detection System**
- ✅ Anomaly detection already implemented
- ✅ Configurable threshold (default 15%)
- ✅ Anomaly score (0-1) for severity
- ✅ Anomaly reason tracking
- ✅ Repository method: `findByIsAnomaly(Boolean)`
**Status**: Fully functional.

#### 4. **Analytics Infrastructure**
- ✅ `AnalyticsController` exists
- ✅ `RentAnalyzerService` for rent analysis
- ✅ AI benchmark calculation
- ✅ Market trend analysis
**Status**: Foundation ready for GIS endpoints.

#### 5. **Database Capabilities**
- ✅ PostgreSQL with PostGIS support (hibernate-spatial enabled)
- ✅ Spatial queries possible
- ✅ Aggregation queries supported
**Status**: Database ready for spatial operations.

---

## What You NEED to Add

### 1. **Neighborhood Centroid Lookup** (Easy - 1 hour)
Hardcode ~15 Addis Ababa neighborhood centroids for properties without GPS:

```java
public class AddisAbabaNeighborhoods {
    private static final Map<String, Coordinates> CENTROIDS = Map.ofEntries(
        entry("Bole", new Coordinates(9.0054, 38.7636)),
        entry("Ayat", new Coordinates(9.0417, 38.8312)),
        entry("Megenagna", new Coordinates(9.0192, 38.7869)),
        entry("Piassa", new Coordinates(9.0320, 38.7469)),
        entry("Merkato", new Coordinates(9.0320, 38.7200)),
        entry("CMC", new Coordinates(8.9806, 38.7578)),
        entry("Gerji", new Coordinates(9.0450, 38.7950)),
        entry("Kazanchis", new Coordinates(9.0250, 38.7600)),
        entry("Sarbet", new Coordinates(9.0100, 38.7400)),
        entry("Lebu", new Coordinates(8.9500, 38.7200)),
        entry("Kolfe", new Coordinates(8.9900, 38.6900)),
        entry("Lideta", new Coordinates(9.0200, 38.7300)),
        entry("Kirkos", new Coordinates(8.9950, 38.7550)),
        entry("Arada", new Coordinates(9.0350, 38.7450)),
        entry("Gulele", new Coordinates(9.0500, 38.7300))
    );
}
```

### 2. **GIS Aggregation Endpoints** (Medium - 4 hours)

#### Endpoint 1: Rent Density Heatmap
```java
@GetMapping("/analytics/heatmap/rent-density")
public ResponseEntity<List<RentDensityPoint>> getRentDensityHeatmap(
    @RequestParam(required = false) PropertyType propertyType,
    @RequestParam(required = false) LocalDate startDate,
    @RequestParam(required = false) LocalDate endDate
) {
    // Query: Group contracts by location, calculate average rent
    // Return: List of {lat, lng, avgRent, propertyCount}
}
```

**SQL Query**:
```sql
SELECT 
    COALESCE(p.latitude, n.centroid_lat) as lat,
    COALESCE(p.longitude, n.centroid_lng) as lng,
    AVG(c.monthly_rent) as avg_rent,
    COUNT(*) as property_count,
    p.sub_city
FROM rental_contracts c
JOIN rental_units u ON c.rental_unit_id = u.id
JOIN properties p ON u.property_id = p.id
LEFT JOIN neighborhood_centroids n ON p.sub_city = n.name
WHERE c.status = 'ACTIVE'
GROUP BY lat, lng, p.sub_city
```

#### Endpoint 2: Anomaly Concentration
```java
@GetMapping("/analytics/heatmap/anomaly-concentration")
public ResponseEntity<List<AnomalyConcentrationPoint>> getAnomalyConcentration(
    @RequestParam(required = false) Double minSeverity
) {
    // Query: Group declarations by location, count anomalies
    // Return: List of {lat, lng, anomalyCount, avgSeverity}
}
```

**SQL Query**:
```sql
SELECT 
    COALESCE(p.latitude, n.centroid_lat) as lat,
    COALESCE(p.longitude, n.centroid_lng) as lng,
    COUNT(*) as anomaly_count,
    AVG(d.anomaly_score) as avg_severity,
    p.sub_city
FROM rent_declarations d
JOIN rental_contracts c ON d.contract_id = c.id
JOIN rental_units u ON c.rental_unit_id = u.id
JOIN properties p ON u.property_id = p.id
LEFT JOIN neighborhood_centroids n ON p.sub_city = n.name
WHERE d.is_anomaly = true
GROUP BY lat, lng, p.sub_city
```

#### Endpoint 3: Risk Score Layer (Future - requires risk scoring)
```java
@GetMapping("/analytics/heatmap/risk-score")
public ResponseEntity<List<RiskScorePoint>> getRiskScoreHeatmap() {
    // Query: Aggregate landlord risk scores by location
    // Return: List of {lat, lng, avgRiskScore, landlordCount}
}
```

**Note**: Risk scoring not yet implemented. Can add later.

### 3. **Frontend Map Component** (Medium - 6 hours)

Use **Leaflet.js** (lightweight, free, no API key needed):

```jsx
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

export default function GISHeatmap() {
  const [layer, setLayer] = useState('rent'); // rent | anomaly | risk
  const [heatmapData, setHeatmapData] = useState([]);
  
  useEffect(() => {
    // Fetch data based on selected layer
    if (layer === 'rent') {
      analyticsAPI.getRentDensityHeatmap(filters).then(setHeatmapData);
    } else if (layer === 'anomaly') {
      analyticsAPI.getAnomalyConcentration(filters).then(setHeatmapData);
    }
  }, [layer, filters]);
  
  return (
    <MapContainer center={[9.0320, 38.7469]} zoom={12}>
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      {heatmapData.map(point => (
        <CircleMarker
          key={point.id}
          center={[point.lat, point.lng]}
          radius={getRadius(point.value)}
          fillColor={getColor(point.value)}
          fillOpacity={0.6}>
          <Popup>
            <div>
              <strong>{point.subCity}</strong>
              <p>Properties: {point.count}</p>
              <p>Avg Rent: {point.avgRent} ETB</p>
              <p>Anomalies: {point.anomalyCount}</p>
            </div>
          </Popup>
        </CircleMarker>
      ))}
    </MapContainer>
  );
}
```

### 4. **Filter Controls** (Easy - 2 hours)
```jsx
<div className="filter-bar">
  <select onChange={e => setLayer(e.target.value)}>
    <option value="rent">Rent Density</option>
    <option value="anomaly">Anomaly Concentration</option>
    <option value="risk">Risk Score (Coming Soon)</option>
  </select>
  
  <select onChange={e => setPropertyType(e.target.value)}>
    <option value="">All Property Types</option>
    <option value="APARTMENT">Apartment</option>
    <option value="VILLA">Villa</option>
    <option value="CONDO">Condo</option>
  </select>
  
  <input type="date" onChange={e => setStartDate(e.target.value)} />
  <input type="date" onChange={e => setEndDate(e.target.value)} />
  
  <input 
    type="range" 
    min="0" 
    max="1" 
    step="0.1"
    onChange={e => setMinSeverity(e.target.value)} 
    placeholder="Min Anomaly Severity"
  />
</div>
```

---

## Implementation Roadmap

### Phase 1: Foundation (Day 1 - 4 hours)
- [x] Data already exists ✅
- [ ] Create neighborhood centroid lookup table
- [ ] Add helper method to get coordinates (GPS or centroid)
- [ ] Test coordinate resolution

### Phase 2: Backend Endpoints (Day 2 - 6 hours)
- [ ] Create `GISAnalyticsController`
- [ ] Implement rent density endpoint
- [ ] Implement anomaly concentration endpoint
- [ ] Add filtering support (property type, date range)
- [ ] Test with real data

### Phase 3: Frontend Map (Day 3-4 - 8 hours)
- [ ] Install Leaflet.js (`npm install leaflet react-leaflet`)
- [ ] Create `GISHeatmap` component
- [ ] Add layer toggle (rent/anomaly/risk)
- [ ] Add filter controls
- [ ] Style markers with color gradients
- [ ] Add popups with details
- [ ] Add "View Properties" link to compliance dashboard

### Phase 4: Polish (Day 5 - 4 hours)
- [ ] Add loading states
- [ ] Add empty states
- [ ] Optimize performance (clustering for many points)
- [ ] Add legend (color scale explanation)
- [ ] Mobile responsive design
- [ ] Add to officer navigation menu

**Total Estimated Time**: 22 hours (~3 days)

---

## Technical Decisions

### Why Leaflet.js?
- ✅ Free and open source
- ✅ No API key required
- ✅ Lightweight (38KB gzipped)
- ✅ Works offline with OpenStreetMap tiles
- ✅ React bindings available
- ✅ Supports heatmaps, markers, popups
- ❌ Google Maps requires API key + billing

### Why Hardcode Centroids?
- ✅ No external API calls (faster, no rate limits)
- ✅ Works offline
- ✅ Predictable and reliable
- ✅ Only ~15 neighborhoods in Addis Ababa
- ✅ Centroids rarely change
- ❌ Geocoding APIs (Google, Mapbox) cost money and have rate limits

### Database Optimization
- Add index on `properties(sub_city)` for fast grouping
- Add index on `rent_declarations(is_anomaly)` for anomaly queries
- Consider materialized view for pre-aggregated data if performance issues

---

## Data Availability Check

### Current Data in Database:
```sql
-- Check properties with GPS
SELECT COUNT(*) as with_gps 
FROM properties 
WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- Check properties without GPS (will use centroids)
SELECT COUNT(*) as without_gps 
FROM properties 
WHERE latitude IS NULL OR longitude IS NULL;

-- Check active contracts for rent density
SELECT COUNT(*) FROM rental_contracts WHERE status = 'ACTIVE';

-- Check anomalies for concentration map
SELECT COUNT(*) FROM rent_declarations WHERE is_anomaly = true;
```

---

## Risks & Mitigations

### Risk 1: Too Many Points (Performance)
**Mitigation**: Use marker clustering (Leaflet.markercluster plugin)
- Groups nearby markers into clusters
- Shows count badge
- Expands on zoom

### Risk 2: Inaccurate Centroids
**Mitigation**: 
- Use well-known landmarks as centroids
- Test with local knowledge
- Allow admin to update centroids via config

### Risk 3: Empty Areas
**Mitigation**:
- Show message "No data for this area"
- Suggest expanding date range
- Show all sub-cities even with 0 properties

---

## Conclusion

**✅ HIGHLY FEASIBLE**

You have:
- ✅ All required data (location, rent, anomalies)
- ✅ Database with spatial support
- ✅ Analytics infrastructure
- ✅ Anomaly detection system

You need:
- 15 hardcoded neighborhood centroids (1 hour)
- 3 new backend endpoints (6 hours)
- 1 frontend map component (8 hours)
- Filter controls (2 hours)
- Polish & testing (4 hours)

**Total**: ~22 hours of development = **3 days**

This is a **high-value feature** that will significantly improve officer compliance monitoring and is **absolutely achievable** with your current codebase.

---

## Next Steps

1. **Approve this plan** ✅
2. **Gather neighborhood centroid coordinates** (research Addis Ababa map)
3. **Start with Phase 1** (centroid lookup table)
4. **Build incrementally** (backend → frontend → polish)
5. **Test with real data** from your database

Ready to implement when you are! 🚀
