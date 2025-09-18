export default function LocationStats({ locations }) {
    if (!locations || locations.length === 0) return null;
  
    return (
      <div style={{ marginTop: "20px" }}>
        <h2 style={{ color: "#E65100", marginBottom: "15px" }}>Location Data</h2>
        {locations.map((loc) => (
          <div
            key={loc.name}
            style={{
              backgroundColor: "#FFF3E0", // soft orange
              padding: "15px",
              borderRadius: "10px",
              marginBottom: "10px",
              boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
            }}
          >
            <strong>{loc.name}</strong> ({loc.latitude}, {loc.longitude})<br />
            PM2.5: {loc.airQualityData?.pm25 ?? "N/A"}, PM10: {loc.airQualityData?.pm10 ?? "N/A"}, 
            NO2: {loc.airQualityData?.no2 ?? "N/A"}, SO2: {loc.airQualityData?.so2 ?? "N/A"}, 
            O3: {loc.airQualityData?.o3 ?? "N/A"}, CO: {loc.airQualityData?.co ?? "N/A"}<br />
            Timestamp: {loc.airQualityData?.timestamp ?? "N/A"}
          </div>
        ))}
      </div>
    );
  }