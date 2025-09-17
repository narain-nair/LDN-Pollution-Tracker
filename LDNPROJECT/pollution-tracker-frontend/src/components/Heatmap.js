import React, { useEffect, useRef } from "react";
import { MapContainer, TileLayer, CircleMarker, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.heat";

export default function Heatmap({ locations }) {
  const mapRef = useRef();

  // Debug
  console.log("Heatmap.js rendered with locations:", locations);

  useEffect(() => {
    console.log("Heatmap useEffect running with locations:", locations);
    if (!mapRef.current) return;

    const map = mapRef.current;

    // Remove existing heat layers
    map.eachLayer((layer) => {
      if (layer instanceof L.LayerGroup && layer.getLayers().some(l => l._latlng)) {
        map.removeLayer(layer);
      }
    });

    // Filter valid points
    const heatPoints = locations
      .filter(loc => loc.airQualityData && loc.lat != null && loc.lng != null)
      .map(loc => {
        const intensity = Math.min(loc.airQualityData.pm25 / 5, 1); // adjust divisor based on your data
        console.log("Adding heat point:", loc.name, loc.lat, loc.lng, "Intensity:", intensity);
        return [loc.lat, loc.lng, intensity];
      });

    if (heatPoints.length > 0) {
      console.log("Creating heat layer with points:", heatPoints);
      L.heatLayer(heatPoints, { radius: 25, blur: 15, maxZoom: 17 }).addTo(map);
    } else {
      console.warn("No valid heat points found!");
    }
  }, [locations]);

  return (
    <MapContainer
      center={[51.5074, -0.1278]}
      zoom={11}
      style={{ height: "500px", width: "100%" }}
    >
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; OpenStreetMap contributors'
      />
  
      {/* Circle markers instead of default icons */}
      {locations
        .filter(loc => loc.lat && loc.lng)
        .map((loc, i) => {
          // Choose color based on PM2.5 reading
          const pm25 = loc.airQualityData?.pm25 ?? 0;
          const color = pm25 <= 1 ? "green" :
                        pm25 <= 2 ? "yellow" :
                        pm25 <= 3 ? "orange" : "red";
  
          return (
            <CircleMarker
              key={i}
              center={[loc.lat, loc.lng]}
              radius={10}             // size of the circle
              fillColor={color}       // fill color
              color="#000"            // stroke color
              weight={1}              // stroke width
              fillOpacity={0.6}       // transparency
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