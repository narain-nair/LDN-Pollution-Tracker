import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from "react";
import axios from "axios";

function App() {
  const[locations, setLocations] = useState([]);

  useEffect(() => {
    axios.get("http://localhost:8080/api/locations")
      .then(res => setLocations(res.data))
      .catch(err => console.error(err));
  }, []);

  return (
    <div>
      <h1>Locations</h1>
      <ul>
        {locations.map(loc => (
          <li key={loc.id}>{loc.name} ({loc.latitude}, {loc.longitude})</li>
        ))}
      </ul>
    </div>
  );
}

export default App;
