import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from "react";
import axios from "axios";

function App() {
  const [query, setQuery] = useState("");
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const searchLocations = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await fetch(`http://localhost:8080/locations/search?query=${query}`);
      if (!res.ok) throw new Error("Failed to fetch locations");
      const data = await res.json();
      setLocations(data);
    } catch (err) {
      setError(err.message);
    }
    setLoading(false);
  };

  return (
    <div style={{ padding: "2rem" }}>
      <h1>Air Quality Locations</h1>
      <input
        type="text"
        value={query}
        placeholder="Enter location name"
        onChange={(e) => setQuery(e.target.value)}
      />
      <button onClick={searchLocations} disabled={loading}>Search</button>

      {error && <p style={{ color: "red" }}>{error}</p>}

      <ul>
        {locations.map((loc) => (
          <li key={loc.name}>
            <strong>{loc.name}</strong> ({loc.latitude}, {loc.longitude})<br />
            PM2.5: {loc.airQualityData?.pm25 ?? "N/A"}, PM10: {loc.airQualityData?.pm10 ?? "N/A"}, 
            NO2: {loc.airQualityData?.no2 ?? "N/A"}, O3: {loc.airQualityData?.o3 ?? "N/A"}
          </li>
        ))}
      </ul>
    </div>
  );
}


export default App;
