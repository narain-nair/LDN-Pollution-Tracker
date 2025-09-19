import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from "react";
import { ToastContainer, toast } from "react-toastify";
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
  const [modalMessage, setModalMessage] = useState(null);
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
        setModalMessage("This site has no usable air quality data.");
        setSelectedLocation(null);
      }
    } catch (err) {
      setModalMessage("Something went wrong while fetching data.");
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
        backgroundColor: "#FFF9F0", // very soft orange, almost off-white
        minHeight: "100vh",
        padding: "20px",
        boxSizing: "border-box",
      }}
    >
      {/* Navbar */}
      <Navbar
        style={{
          backgroundColor: "#FFB74D", // strong accent only for navbar
          color: "#fff",
        }}
      />
  
      {/* Toasts */}
      <ToastContainer position="top-center" autoClose={3000} />
  
      {/* Modal */}
      {modalMessage && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            backgroundColor: "rgba(0,0,0,0.5)", // blur background
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 2000,
          }}
        >
          <div
            style={{
              backgroundColor: "#fff",
              padding: "20px",
              borderRadius: "10px",
              maxWidth: "400px",
              textAlign: "center",
              boxShadow: "0 4px 12px rgba(0,0,0,0.2)",
            }}
          >
            <h3 style={{ color: "#E65100" }}>Notice</h3>
            <p>{modalMessage}</p>
            <button
              onClick={() => setModalMessage(null)}
              style={{
                marginTop: "15px",
                padding: "8px 16px",
                backgroundColor: "#FFB74D",
                border: "none",
                borderRadius: "5px",
                cursor: "pointer",
                color: "#fff",
                fontWeight: "bold",
              }}
            >
              Close
            </button>
          </div>
        </div>
      )}
  
      <PageContainer>
        {/* Page title */}
        <h1
          style={{
            marginBottom: "20px",
            color: "#E65100", // strong orange accent for heading
          }}
        >
          Search Locations
        </h1>
  
        {/* SearchBar */}
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            marginBottom: "20px",
            position: "relative",
            zIndex: 1000, // dropdown stays above map
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
  
        {/* Map / Heatmap container */}
        <div
          style={{
            margin: "0 auto",
            maxWidth: "1200px",
            borderRadius: "12px",
            overflow: "hidden",
            boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
            marginBottom: "30px",
            border: "2px solid #FFB74D", // subtle accent
          }}
        >
          {allLocations.length > 0 && <Heatmap locations={allLocations} />}
        </div>
  
        {/* Selected site info */}
        {selectedLocation && (
          <div
            style={{
              backgroundColor: "#FFFFFF", // white for readability
              borderRadius: "12px",
              padding: "20px",
              margin: "0 auto",
              maxWidth: "1200px",
              boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
              borderLeft: "5px solid #FFB74D", // accent indicator
            }}
          >
            <LocationStats locations={[selectedLocation]} />
  
            {/* Pollutant chart */}
            <div
              style={{
                backgroundColor: "#FFF3E0", // subtle orange tint for chart card
                padding: "15px",
                borderRadius: "10px",
                marginTop: "20px",
                boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
              }}
            >
              <PollutantChart locations={[selectedLocation]} />
            </div>
  
            <PollutantTabs location={selectedLocation} />
          </div>
        )}
      </PageContainer>
    </div>
  );
}

export default App;
