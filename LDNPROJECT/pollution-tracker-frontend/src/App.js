import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from "react";
import axios from "axios";

function App() {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [selectedSiteCode, setSelectedSiteCode] = useState(null);
  const [locations, setLocations] = useState([]);

  // Fetch suggestions as user types
  const handleInputChange = async (e) => {
    const val = e.target.value;
    setQuery(val);

    if (val.length >= 2) { // fetch suggestions after 2 chars
      const res = await fetch(`/locations/suggest?query=${encodeURIComponent(val)}`);
      const data = await res.json();
      setSuggestions(data);
    } else {
      setSuggestions([]);
    }
  };

  // When user clicks a suggestion
  const handleSelectSuggestion = async (siteCode) => {
    setSelectedSiteCode(siteCode);
    setSuggestions([]);
    setQuery(siteCode);

    const res = await fetch(`/locations/search?siteCode=${encodeURIComponent(siteCode)}`);
    if (res.ok) {
      const data = await res.json();
      setLocations(data);
    } else {
      setLocations([]);
    }
  };

  return (
    <div style={{ padding: "20px", fontFamily: "sans-serif" }}>
      <h1>Search Locations</h1>

      <input
        type="text"
        value={query}
        onChange={handleInputChange}
        placeholder="Type location name..."
        style={{ padding: "5px", width: "300px" }}
      />

      {/* Suggestions dropdown */}
      {suggestions.length > 0 && (
        <ul style={{ border: "1px solid #ccc", width: "300px", marginTop: 0, padding: 0, listStyle: "none" }}>
          {suggestions.map((s, i) => (
            <li
              key={i}
              onClick={() => handleSelectSuggestion(s)}
              style={{ padding: "5px", cursor: "pointer", backgroundColor: "#f9f9f9" }}
            >
              {s}
            </li>
          ))}
        </ul>
      )}

      {/* Display location info */}
      {locations.length > 0 && (
        <div style={{ marginTop: "20px" }}>
          <h2>Location Data</h2>
          {locations.map((loc) => (
            <div key={loc.name} style={{ marginBottom: "10px" }}>
              <strong>{loc.name}</strong> ({loc.latitude}, {loc.longitude})<br />
              PM2.5: {loc.airQualityData?.pm25 ?? "N/A"}, PM10: {loc.airQualityData?.pm10 ?? "N/A"}, 
              NO2: {loc.airQualityData?.no2 ?? "N/A"}, SO2: {loc.airQualityData?.so2 ?? "N/A"}, 
              O3: {loc.airQualityData?.o3 ?? "N/A"}, CO: {loc.airQualityData?.co ?? "N/A"}<br />
              Timestamp: {loc.airQualityData?.timestamp ?? "N/A"}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default App;
