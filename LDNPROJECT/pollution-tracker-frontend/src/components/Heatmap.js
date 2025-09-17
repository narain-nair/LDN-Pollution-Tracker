import React, { useEffect, useRef } from "react";
import { MapContainer, TileLayer } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.heat";

export default function Heatmap({ locations }) {
  const mapRef = useRef();

  useEffect(() => {
    if (!mapRef.current) return;

    const map = mapRef.current;
    // Remove existing heat layers
    map.eachLayer((layer) => {
      if (layer instanceof L.LayerGroup && layer.getLayers().some(l => l._latlng)) {
        map.removeLayer(layer);
      }
    });

    const heatPoints = locations
      .filter(loc => loc.airQualityData && loc.latitude && loc.longitude && loc.airQualityData.PM25 != null)
      .map(loc => [
        loc.latitude,
        loc.longitude,
        Math.min(loc.airQualityData.PM25 / 100, 1) // normalize intensity
      ]
    );
    console.log("Heatmap.js heatPoints:", heatPoints);

    if (heatPoints.length > 0) {
        console.log("Adding heat layer with points:", heatPoints);
        L.heatLayer(heatPoints, { radius: 25, blur: 15, maxZoom: 17 }).addTo(map);
    } else {
        console.warn("No valid heat points to display!");
    }
  }, [locations]);

  return (
    <MapContainer
      center={[51.5074, -0.1278]}
      zoom={11}
      style={{ height: "400px", width: "100%" }}
      whenCreated={(mapInstance) => (mapRef.current = mapInstance)}
    >
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="http://osm.org/copyright">OSM</a>'
      />
    </MapContainer>
  );
}