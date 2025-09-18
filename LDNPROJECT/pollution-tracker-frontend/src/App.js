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

  useEffect(() => {
    const fetchAllLocations = async () => {
      try {
        const response = await axios.get("http://localhost:8080/locations/all");
        setAllLocations(response.data);
        console.log("All locations fetched:", response.data);
      } catch (error) {
        console.error(error);
      }
    };
    fetchAllLocations();
  }, []);

  return (
    <div
      className="App"
      style={{
        backgroundColor: "#FFF3E0", // soft light-orange
        minHeight: "100vh",
        padding: "20px",
        boxSizing: "border-box",
      }}
    >
      <Navbar
        style={{
          backgroundColor: "#FFB74D", // deeper orange for navbar
          color: "#fff",
        }}
      />
      <PageContainer>
        <h1
          style={{
            marginBottom: "20px",
            color: "#E65100", // strong accent color
          }}
        >
          Search Locations
        </h1>

        {/* SearchBar container */}
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            marginBottom: "20px",
            position: "relative",
            zIndex: 1000, // keep dropdown above map
          }}
        >
          <SearchBar
            query={query}
            setQuery={setQuery}
            suggestions={suggestions}
            setSuggestions={setSuggestions}
            onSelect={handleSelectSuggestion}
          />
        </div>

        {/* Map container */}
        <div
          style={{
            margin: "0 auto",
            maxWidth: "1200px",
            borderRadius: "12px",
            overflow: "hidden",
            boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
            marginBottom: "30px",
            border: "2px solid #FFB74D", // subtle accent border
          }}
        >
          {allLocations.length > 0 && <Heatmap locations={allLocations} />}
        </div>

        {/* Selected site info */}
        {selectedLocation && (
          <div
            style={{
              backgroundColor: "#fff",
              borderRadius: "12px",
              padding: "20px",
              margin: "0 auto",
              maxWidth: "1200px",
              boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
              borderLeft: "5px solid #FFB74D", // accent indicator
            }}
          >
            <LocationStats locations={[selectedLocation]} />
            <PollutantChart locations={[selectedLocation]} />
            <PollutantTabs location={selectedLocation} />
          </div>
        )}
      </PageContainer>
    </div>
  );
}

export default App;
