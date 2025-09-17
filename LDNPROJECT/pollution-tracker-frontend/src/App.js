import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from "react";
import axios from "axios";

import SearchBar from './components/SearchBar';
import LocationStats from "./components/LocationStats";
import PollutantChart from "./components/PollutionChart";

function App() {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [selectedSiteCode, setSelectedSiteCode] = useState(null);
  const [locations, setLocations] = useState([]);

  // When user clicks a suggestion
  const handleSelectSuggestion = async (siteCode) => {
    setSelectedSiteCode(siteCode);
    setSuggestions([]);
    setQuery(siteCode);

    const res = await fetch(`http://localhost:8080/locations/search?siteCode=${encodeURIComponent(siteCode)}`);
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
      <SearchBar query={query} setQuery={setQuery} suggestions={suggestions} setSuggestions={setSuggestions} onSelect={handleSelectSuggestion} />
      <LocationStats locations={locations} />
      <PollutantChart locations={locations} />
    </div>
  );
}

export default App;
