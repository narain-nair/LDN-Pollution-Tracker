export default function LocationStats({ locations }) {
    if (!locations || locations.length === 0) return null;
  
    return (
      <div style={{ marginTop: "20px" }}>
        <h2>Location Data</h2>
        {locations.map((loc) => (
          <div key={loc.name} style={{ marginBottom: "10px" }}>
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