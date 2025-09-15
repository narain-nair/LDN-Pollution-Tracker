import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from "react";
import axios from "axios";

function App() {
  const[locations, setLocations] = useState([]);

  useEffect(() => {
    axios.get("http://localhost:8080/locations/all")
      .then(res => setLocations(res.data))
      .catch(err => console.error(err));
  }, []);

  return (
    <div>
      <h1>Locations</h1>
      <ul>
        {locations.map(loc => (
          <li key={loc.id}>
            {loc.name} ({loc.latitude}, {loc.longitude}, {loc.siteCode})<br />
            PM2.5: {loc.airQualityData?.pm25 ?? 'N/A'}, 
            PM10: {loc.airQualityData?.pm10 ?? 'N/A'}, 
            NO2: {loc.airQualityData?.no2 ?? 'N/A'}, 
            SO2: {loc.airQualityData?.so2 ?? 'N/A'}, 
            O3: {loc.airQualityData?.o3 ?? 'N/A'}, 
            CO: {loc.airQualityData?.co ?? 'N/A'}<br />
            Timestamp: {loc.airQualityData?.timestamp ?? 'N/A'}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;
