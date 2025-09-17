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
  const [allLocations, setAllLocations] = useState([]);
  const [selectedLocation, setSelectedLocation] = useState(null);

  // Search handler
  const hasAirQualityData = (aqData) => {
    if (!aqData) return false;
    return Object.values(aqData).some((v) => v != null);
  };
  
  const handleSelectSuggestion = async (siteCode) => {
    setSelectedSiteCode(siteCode);
    setSuggestions([]);
    setQuery(siteCode);
  
    try {
      const res = await fetch(
        `http://localhost:8080/locations/search?siteCode=${encodeURIComponent(siteCode)}`
      );
      if (!res.ok) throw new Error("Failed to fetch location");
      const data = await res.json();
  
      const site = data[0];
      if (site && hasAirQualityData(site.airQualityData)) {
        setSelectedLocation(site);
      } else {
        console.warn("Site has no usable air quality data:", site);
        setSelectedLocation(null);
      }
    } catch (err) {
      console.error(err);
      setSelectedLocation(null);
    }
  };

  // Fetch all locations for prepopulated map
  useEffect(() => {
    const fetchAllLocations = async () => {
      try {
        const response = await axios.get('http://localhost:8080/locations/all');
        setAllLocations(response.data);
        console.log("All locations fetched:", response.data);
      } catch (error) {
        console.error(error);
      }
    };
    fetchAllLocations();
  }, []);

  return (
    <div className="App">
      <Navbar />
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

        {/* Heatmap always shows all locations */}

        {/* Charts / stats for selected site only */}
        {allLocations.length > 0 && <Heatmap locations={allLocations} />}
        {selectedLocation && (
          <>
            <LocationStats locations={[selectedLocation]} />
            <PollutantChart locations={[selectedLocation]} />
            <PollutantTabs location={selectedLocation} />
          </>
        )}
      </PageContainer>
    </div>
  );
}

export default App;
