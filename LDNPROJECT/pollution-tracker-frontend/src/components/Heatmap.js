import React from "react";
import { MapContainer, TileLayer } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.heat";

export default function Heatmap({ locations }) {
    const mapCenter = [51.5074, -0.1278]; // Default: London
  
    const heatPoints = locations
      .filter(loc => loc.airQualityData) // only include locations with data
      .map(loc => [
        loc.latitude,
        loc.longitude,
        loc.airQualityData.PM25 / 100 // normalize for heat intensity (example)
      ]);
  
    // Create a heat layer
    const createHeatLayer = (map) => {
      if (!map || heatPoints.length === 0) return;
  
      L.heatLayer(heatPoints, { radius: 25, blur: 15, maxZoom: 17 }).addTo(map);
    };
  
    return (
      <MapContainer
        center={mapCenter}
        zoom={11}
        style={{ height: "400px", width: "100%" }}
        whenCreated={createHeatLayer}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="http://osm.org/copyright">OSM</a>'
        />
      </MapContainer>
    );
  }