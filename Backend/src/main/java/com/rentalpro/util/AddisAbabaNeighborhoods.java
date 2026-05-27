package com.rentalpro.util;

import com.rentalpro.model.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Utility class for Addis Ababa neighborhood geographic centroids.
 * Used as fallback when properties don't have exact GPS coordinates.
 */
public class AddisAbabaNeighborhoods {

    @Data
    @AllArgsConstructor
    public static class Coordinates {
        private double latitude;
        private double longitude;
    }

    // Hardcoded centroids for major Addis Ababa neighborhoods/sub-cities
    private static final Map<String, Coordinates> CENTROIDS = Map.ofEntries(
        // Major neighborhoods and sub-cities
        Map.entry("Bole", new Coordinates(9.0054, 38.7636)),
        Map.entry("Ayat", new Coordinates(9.0417, 38.8312)),
        Map.entry("Megenagna", new Coordinates(9.0192, 38.7869)),
        Map.entry("Piassa", new Coordinates(9.0320, 38.7469)),
        Map.entry("Merkato", new Coordinates(9.0320, 38.7200)),
        Map.entry("CMC", new Coordinates(8.9806, 38.7578)),
        Map.entry("Gerji", new Coordinates(9.0450, 38.7950)),
        Map.entry("Kazanchis", new Coordinates(9.0250, 38.7600)),
        Map.entry("Sarbet", new Coordinates(9.0100, 38.7400)),
        Map.entry("Lebu", new Coordinates(8.9500, 38.7200)),
        
        // Sub-cities (official administrative divisions)
        Map.entry("Addis Ketema", new Coordinates(9.0350, 38.7300)),
        Map.entry("Akaky Kaliti", new Coordinates(8.8800, 38.7800)),
        Map.entry("Arada", new Coordinates(9.0350, 38.7450)),
        Map.entry("Gulele", new Coordinates(9.0500, 38.7300)),
        Map.entry("Kirkos", new Coordinates(8.9950, 38.7550)),
        Map.entry("Kolfe Keranio", new Coordinates(8.9900, 38.6900)),
        Map.entry("Lideta", new Coordinates(9.0200, 38.7300)),
        Map.entry("Nifas Silk-Lafto", new Coordinates(8.9500, 38.7400)),
        Map.entry("Yeka", new Coordinates(9.0450, 38.8100))
    );

    // Fallback: Addis Ababa city center (Meskel Square area)
    private static final Coordinates DEFAULT_CENTROID = new Coordinates(9.0320, 38.7469);

    /**
     * Get coordinates for a property.
     * Uses exact GPS if available, otherwise falls back to neighborhood centroid.
     * 
     * @param property The property to get coordinates for
     * @return Coordinates (either exact GPS or centroid)
     */
    public static Coordinates getCoordinates(Property property) {
        // Priority 1: Use exact GPS coordinates if available
        if (property.getLatitude() != null && property.getLongitude() != null) {
            return new Coordinates(property.getLatitude(), property.getLongitude());
        }
        
        // Priority 2: Use neighborhood/sub-city centroid
        String subCity = property.getSubCity();
        if (subCity != null) {
            Coordinates centroid = CENTROIDS.get(subCity);
            if (centroid != null) {
                return centroid;
            }
        }
        
        // Priority 3: Fallback to Addis Ababa city center
        return DEFAULT_CENTROID;
    }

    /**
     * Check if a property has exact GPS coordinates
     */
    public static boolean hasExactGPS(Property property) {
        return property.getLatitude() != null && property.getLongitude() != null;
    }

    /**
     * Get centroid for a specific sub-city/neighborhood
     */
    public static Coordinates getCentroid(String subCity) {
        return CENTROIDS.getOrDefault(subCity, DEFAULT_CENTROID);
    }
}
