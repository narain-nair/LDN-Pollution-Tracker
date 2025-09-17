import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from "react";
import axios from "axios";

import SearchBar from './components/SearchBar';
import LocationStats from "./components/LocationStats";
import PollutantChart from "./components/PollutionChart";
import Navbar from "./components/layout/NavBar";
import PageContainer from "./components/layout/PageContainer";
import PollutantTabs from "./components/PollutantTabs";
import Heatmap from "./components/Heatmap";

function App() {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [selectedSiteCode, setSelectedSiteCode] = useState(null);
  const [locations, setLocations] = useState([]);
  console.log("App.js locations state:", locations);

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
    <div className="App">
      {/* Fixed top navbar */}
      <Navbar />
  
      {/* Page content below navbar */}
      <PageContainer>
        <h1 style={{ marginBottom: "10px" }}>Search Locations</h1>
  
        <div style={{ display: "flex", justifyContent: "center", marginBottom: "20px" }}>
          <SearchBar
            query={query}
            setQuery={setQuery}
            suggestions={suggestions}
            setSuggestions={setSuggestions}
            onSelect={handleSelectSuggestion}
          />
        </div>
  
        {/* Show charts and info only if a location is selected */}
        {locations.length > 0 && (
          <>
            <LocationStats locations={locations} />
            <PollutantChart locations={locations} />
            <Heatmap locations={locations} />
            <PollutantTabs />
          </>
        )}
      </PageContainer>
    </div>
  );
}

export default App;
