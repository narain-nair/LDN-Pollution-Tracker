import React, { useEffect, useRef } from "react";
import { MapContainer, TileLayer, CircleMarker, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.heat";

export default function Heatmap({ locations }) {
    // Step 1: Log the raw locations received
    console.log("Heatmap: total locations received:", locations.length);
  
    // Step 2: Filter out invalid coordinates, convert to numbers, and remove NaN

    locations.forEach((loc, i) => {
        console.log(`Location ${i}:`, loc, "lat type:", typeof loc.lat, "lng type:", typeof loc.lng);
    });

    const hasAirQualityData = (aqData) => {
        if (!aqData) return false; // null or undefined
        // Check if at least one pollutant is not null
        return Object.values(aqData).some(value => value != null);
      };

    const validLocations = locations
        .filter(loc => loc.latitude != null && loc.longitude != null)
        .filter(loc => hasAirQualityData(loc.airQualityData))
        .map(loc => ({
            ...loc,
            lat: parseFloat(loc.latitude),  // convert to number
            lng: parseFloat(loc.longitude),
        }))
        .filter(loc => !isNaN(loc.lat) && !isNaN(loc.lng));

    console.log("Heatmap: valid locations after parsing:", validLocations);
  
    return (
      <MapContainer
        center={[51.5074, -0.1278]}
        zoom={13} // increase zoom to see points
        style={{ height: "500px", width: "100%" }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap contributors'
        />
  
        {validLocations.map((loc, i) => {
          const pm25 = loc.airQualityData?.pm25 ?? 0;
          const o3 = loc.airQualityData?.o3 ?? 0;
          const no2 = loc.airQualityData?.no2 ?? 0;
          const so2 = loc.airQualityData?.so2 ?? 0;
          const co = loc.airQualityData?.co ?? 0;

          // Step 4: Log each marker before rendering
          console.log(`Rendering marker ${i}:`, {
            name: loc.name,
            lat: loc.lat,
            lng: loc.lng,
            pm25,
            o3,
            no2,
            so2,
            co
          });
  
          const color =
            pm25 <= 1 ? "green" :
            pm25 <= 2 ? "yellow" :
            pm25 <= 3 ? "orange" : "red";
  
          return (
            <CircleMarker
              key={i}
              center={[loc.lat, loc.lng]}
              radius={10 + pm25 * 2} // bigger radius for testing visibility
              fillColor={color}
              color="#000"
              weight={1}
              fillOpacity={0.8}
            >
              <Popup>
                {loc.name}<br />
                PM2.5: {pm25}
              </Popup>
            </CircleMarker>
          );
        })}
      </MapContainer>
    );
  }