import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from "react";
import { ToastContainer, toast } from "react-toastify";
import axios from "axios";

import SearchBar from './components/SearchBar';
import LocationStats from "./components/LocationStats";
import PollutantChart from "./components/PollutionChart";
import PollutantPieChart from "./components/PollutantPieChart";
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
        setModalMessage("This site has no air quality readings.");
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
        backgroundColor: "#FFF9F0",
        minHeight: "100vh",
        padding: "20px",
        boxSizing: "border-box",
      }}
    >
      {/* Navbar */}
      <Navbar
        style={{
          backgroundColor: "#FFB74D",
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
            backgroundColor: "rgba(0,0,0,0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 2000,
            opacity: modalMessage ? 1 : 0,
            pointerEvents: modalMessage ? "auto" : "none",
            transition: "opacity 0.3s ease-in-out",
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
              transform: modalMessage ? "translateY(0)" : "translateY(-20px)",
              transition: "transform 0.3s ease-in-out",
            }}
          >
            <h3 style={{ color: "#E65100" }}>Sorry, an error has occurred!</h3>
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
            color: "#E65100",
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
            zIndex: 1000,
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
            border: "2px solid #FFB74D",
          }}
        >
          {allLocations.length > 0 && <Heatmap locations={allLocations} />}
        </div>
  
        {/* Selected site info */}
        {selectedLocation && (
          <div
            style={{
              backgroundColor: "#FFFFFF",
              borderRadius: "12px",
              padding: "20px",
              margin: "0 auto",
              maxWidth: "1200px",
              boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
              borderLeft: "5px solid #FFB74D",
            }}
          >
            <LocationStats locations={[selectedLocation]} />
  
            {/* Grid layout for Pie and Bar charts */}
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(400px, 1fr))",
                gap: "20px",
                marginTop: "20px",
              }}
            >
              {/* Pie chart */}
              <div
                style={{
                  backgroundColor: "#FFF3E0",
                  padding: "15px",
                  borderRadius: "10px",
                  boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
                }}
              >
                <PollutantPieChart location={selectedLocation} />
              </div>
  
              {/* Bar chart */}
              <div
                style={{
                  backgroundColor: "#FFF3E0",
                  padding: "15px",
                  borderRadius: "10px",
                  boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
                }}
              >
                <PollutantChart locations={[selectedLocation]} />
              </div>
            </div>
  
            {/* Tabs below charts */}
            <div style={{ marginTop: "20px" }}>
              <PollutantTabs location={selectedLocation} />
            </div>
          </div>
        )}
      </PageContainer>
    </div>
  );
}

export default App;
