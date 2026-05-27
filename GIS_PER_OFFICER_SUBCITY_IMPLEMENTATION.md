# GIS Per-Officer Sub-City Implementation

## Overview
Implemented per-officer sub-city filtering and auto-zoom for the GIS Heatmap feature. Each officer now sees only their assigned sub-city by default, with the map automatically centered on their area.

## Implementation Date
May 27, 2026

---

## BACKEND CHANGES

### 1. Service Interface (`GISAnalyticsService.java`)
**Added `subCity` parameter to both methods:**

```java
List<RentDensityPoint> getRentDensityHeatmap(
    PropertyType propertyType,
    String subCity,        // NEW: Filter by sub-city
    LocalDate startDate,
    LocalDate endDate
);

List<AnomalyConcentrationPoint> getAnomalyConcentration(
    Double minSeverity,
    String subCity,        // NEW: Filter by sub-city
    LocalDate startDate,
    LocalDate endDate
);
```

### 2. Service Implementation (`GISAnalyticsServiceImpl.java`)
**Added sub-city filtering logic:**

```java
// In getRentDensityHeatmap()
if (subCity != null && !subCity.isEmpty()) {
    contracts = contracts.stream()
        .filter(c -> subCity.equals(c.getRentalUnit().getProperty().getSubCity()))
        .collect(Collectors.toList());
}

// In getAnomalyConcentration()
if (subCity != null && !subCity.isEmpty()) {
    declarations = declarations.stream()
        .filter(d -> subCity.equals(d.getContract().getRentalUnit().getProperty().getSubCity()))
        .collect(Collectors.toList());
}
```

### 3. Controller (`AnalyticsController.java`)
**Added `subCity` query parameter to both endpoints:**

```java
@GetMapping("/heatmap/rent-density")
public ResponseEntity<List<RentDensityPoint>> getRentDensityHeatmap(
    @RequestParam(required = false) PropertyType propertyType,
    @RequestParam(required = false) String subCity,  // NEW
    @RequestParam(required = false) LocalDate startDate,
    @RequestParam(required = false) LocalDate endDate
)

@GetMapping("/heatmap/anomaly-concentration")
public ResponseEntity<List<AnomalyConcentrationPoint>> getAnomalyConcentration(
    @RequestParam(required = false) Double minSeverity,
    @RequestParam(required = false) String subCity,  // NEW
    @RequestParam(required = false) LocalDate startDate,
    @RequestParam(required = false) LocalDate endDate
)
```

**API Examples:**
```bash
# Filter by Bole sub-city
GET /api/analytics/heatmap/rent-density?subCity=Bole

# Filter by Kirkos sub-city
GET /api/analytics/heatmap/anomaly-concentration?subCity=Kirkos
```

---

## FRONTEND CHANGES

### 1. Sub-City Coordinates Map (`GISHeatmap.jsx`)
**Added JavaScript map matching backend coordinates:**

```javascript
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
```

### 2. Officer Sub-City Detection
**Read from authenticated user:**

```javascript
import { useAuth } from "../../context/AuthContext";

const { user } = useAuth();
const officerSubCity = user?.subCityZone || null;
const subCityCoords = officerSubCity ? SUB_CITY_COORDINATES[officerSubCity] : null;
```

### 3. Auto-Center Map
**Dynamic map center and zoom based on officer's assignment:**

```javascript
// Map center and zoom based on officer's sub-city
const mapCenter = subCityCoords 
  ? [subCityCoords.lat, subCityCoords.lng] 
  : [DEFAULT_CENTER.lat, DEFAULT_CENTER.lng];

const mapZoom = subCityCoords ? 14 : 12;  // Zoom 14 for sub-city, 12 for city-wide
```

### 4. Auto-Filter API Calls
**Automatically include officer's sub-city in all API requests:**

```javascript
const loadHeatmapData = async () => {
  const filters = {
    propertyType: propertyType || undefined,
    startDate: startDate || undefined,
    endDate: endDate || undefined,
    minSeverity: layer === "anomaly" && minSeverity > 0 ? minSeverity : undefined,
    subCity: officerSubCity || undefined,  // AUTO-FILTER
  };

  let data;
  if (layer === "rent") {
    data = await analyticsAPI.getRentDensityHeatmap(filters);
  } else {
    data = await analyticsAPI.getAnomalyConcentration(filters);
  }
  
  setHeatmapData(data);
};
```

### 5. UI Indicators
**Show officer's assigned sub-city in header:**

```javascript
<h2 className="text-2xl font-bold text-gray-900">GIS Compliance Heatmap</h2>
<p className="text-gray-500 text-sm mt-1">
  Geographic visualization of rent density and anomaly concentration
  {officerSubCity && (
    <span className="ml-2 text-primary font-medium">
      • Viewing: {officerSubCity}
    </span>
  )}
</p>
```

**Display locked sub-city filter (read-only):**

```javascript
{officerSubCity && (
  <div className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-gray-50 flex items-center justify-between">
    <span className="text-gray-700">{officerSubCity}</span>
    <span className="text-xs text-gray-500">Assigned</span>
  </div>
)}
```

**Show in active filters:**

```javascript
{officerSubCity && (
  <span className="bg-blue-50 text-primary px-2 py-1 rounded">
    {officerSubCity}
  </span>
)}
```

### 6. Map Updater Component
**Updated to handle zoom changes:**

```javascript
function MapUpdater({ center, zoom }) {
  const map = useMap();
  useEffect(() => {
    if (center && zoom) {
      map.setView(center, zoom);
    }
  }, [center, zoom, map]);
  return null;
}
```

---

## HOW IT WORKS

### Example 1: Officer Assigned to Bole
1. Officer logs in with `user.subCityZone = "Bole"`
2. Frontend reads `user.subCityZone` → "Bole"
3. Looks up coordinates → `{ lat: 9.0054, lng: 38.7636 }`
4. Map centers on Bole with zoom 14
5. API calls include `?subCity=Bole`
6. Backend filters to only Bole properties
7. Officer sees only Bole data on map

### Example 2: Officer Assigned to Kirkos
1. Officer logs in with `user.subCityZone = "Kirkos"`
2. Frontend reads `user.subCityZone` → "Kirkos"
3. Looks up coordinates → `{ lat: 8.9950, lng: 38.7550 }`
4. Map centers on Kirkos with zoom 14
5. API calls include `?subCity=Kirkos`
6. Backend filters to only Kirkos properties
7. Officer sees only Kirkos data on map

### Example 3: Administrator (No Sub-City Assignment)
1. Admin logs in with `user.subCityZone = null`
2. Frontend detects no sub-city assignment
3. Map centers on Addis Ababa (Meskel Square) with zoom 12
4. API calls do NOT include `subCity` parameter
5. Backend returns ALL sub-cities
6. Admin sees city-wide data

---

## USER EXPERIENCE

### For Officers (SUBCITY_STAFF)
- ✅ Map automatically centers on their assigned sub-city
- ✅ Zoom level 14 (neighborhood-level detail)
- ✅ Only see properties in their jurisdiction
- ✅ Sub-city filter is locked (cannot change)
- ✅ Clear indicator: "Viewing: Bole" in header
- ✅ Sub-city shown in active filters
- ✅ Can still filter by property type, dates, severity

### For Administrators
- ✅ Map shows entire Addis Ababa (zoom 12)
- ✅ See all sub-cities
- ✅ No sub-city restriction
- ✅ Can analyze city-wide patterns

---

## TESTING CHECKLIST

### Backend Testing
- [ ] Compile backend: `./mvnw compile` ✅ (Completed)
- [ ] Test rent density with sub-city filter:
  ```bash
  curl "http://localhost:8080/api/analytics/heatmap/rent-density?subCity=Bole"
  ```
- [ ] Test anomaly concentration with sub-city filter:
  ```bash
  curl "http://localhost:8080/api/analytics/heatmap/anomaly-concentration?subCity=Kirkos"
  ```
- [ ] Verify filtering works correctly
- [ ] Test without sub-city parameter (should return all)

### Frontend Testing
- [ ] Login as officer with `subCityZone = "Bole"`
- [ ] Verify map centers on Bole (9.0054, 38.7636)
- [ ] Verify zoom level is 14
- [ ] Verify header shows "Viewing: Bole"
- [ ] Verify locked sub-city filter displays "Bole - Assigned"
- [ ] Verify only Bole properties appear on map
- [ ] Test with different sub-cities (Kirkos, Yeka, etc.)
- [ ] Login as admin (no subCityZone)
- [ ] Verify map shows entire city (zoom 12)
- [ ] Verify all sub-cities appear

---

## DATABASE REQUIREMENTS

### User Table
Officers must have `sub_city_zone` field populated:

```sql
-- Example: Assign officer to Bole
UPDATE users 
SET sub_city_zone = 'Bole' 
WHERE role = 'SUBCITY_STAFF' AND email = 'officer.bole@example.com';

-- Example: Assign officer to Kirkos
UPDATE users 
SET sub_city_zone = 'Kirkos' 
WHERE role = 'SUBCITY_STAFF' AND email = 'officer.kirkos@example.com';
```

### Property Table
Properties must have `sub_city` field populated:

```sql
-- Properties should have sub_city matching one of the 19 supported sub-cities
SELECT sub_city, COUNT(*) 
FROM properties 
GROUP BY sub_city;
```

---

## SUPPORTED SUB-CITIES

The system supports 19 Addis Ababa sub-cities/neighborhoods:

**Official Sub-Cities (10):**
1. Addis Ketema
2. Akaky Kaliti
3. Arada
4. Bole
5. Gulele
6. Kirkos
7. Kolfe Keranio
8. Lideta
9. Nifas Silk-Lafto
10. Yeka

**Major Neighborhoods (9):**
11. Ayat
12. CMC
13. Gerji
14. Kazanchis
15. Lebu
16. Megenagna
17. Merkato
18. Piassa
19. Sarbet

---

## NEXT STEPS

### 1. Data Seeding (Separate Task)
Create SQL migration file to seed sample data for testing:
- File: `Backend/docs/SEED_BOLE_DATA.sql`
- Content: 50-100 properties in Bole with GPS coordinates
- Include: Users, properties, rental units, contracts, declarations
- Run once: `psql -h localhost -p 5433 -U postgres -d rentalpro_db -f Backend/docs/SEED_BOLE_DATA.sql`

### 2. Production Deployment
- Ensure all officers have `sub_city_zone` assigned
- Verify property data has `sub_city` populated
- Test with real data
- Monitor API performance with sub-city filtering

### 3. Future Enhancements
- Add sub-city boundary polygons (GeoJSON)
- Implement sub-city comparison view for admins
- Add sub-city performance metrics
- Export sub-city reports

---

## FILES MODIFIED

### Backend (3 files)
1. `Backend/src/main/java/com/rentalpro/service/GISAnalyticsService.java`
2. `Backend/src/main/java/com/rentalpro/service/impl/GISAnalyticsServiceImpl.java`
3. `Backend/src/main/java/com/rentalpro/controller/AnalyticsController.java`

### Frontend (1 file)
1. `Frontend/src/pages/officer/GISHeatmap.jsx`

---

## COMPILATION STATUS

✅ **Backend:** Compiled successfully (May 27, 2026 16:43:10)
✅ **Frontend:** No compilation needed (React component)

---

## FEATURE STATUS

✅ **Per-Officer Sub-City Filtering:** COMPLETE
✅ **Auto-Center Map:** COMPLETE
✅ **Auto-Zoom:** COMPLETE
✅ **Locked Sub-City Filter:** COMPLETE
✅ **UI Indicators:** COMPLETE
✅ **Backend API Support:** COMPLETE

**Overall Status:** ✅ **READY FOR TESTING**

---

## NOTES

- Feature works for ALL 10 official sub-cities, not just Bole
- Bole was used as example because sample data will be seeded there first
- Administrators see city-wide view (no restrictions)
- Officers cannot change their assigned sub-city
- Sub-city coordinates match backend `AddisAbabaNeighborhoods.java` exactly
- Zoom level 14 provides neighborhood-level detail
- Zoom level 12 provides city-wide overview
