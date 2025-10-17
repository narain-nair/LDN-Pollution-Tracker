import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export default function PollutantChart({ locations }) {
  if (!locations || locations.length === 0) return null;

  const data = Object.keys(locations[0].airQualityData || {})
    .filter(k => ["pm25","pm10","no2","so2","o3","co"].includes(k))
    .map(k => ({ name: k.toUpperCase(), Index: locations[0].airQualityData[k] ?? 0 }));
    
    return (
        <div style={{ textAlign: "center", marginBottom: "40px" }}>
            {/* Chart title */}
            <h3 style={{ marginBottom: "60px"}}>Pollutant Levels</h3>
        
            {/* Chart */}
            <ResponsiveContainer width="100%" height={300}>
            <BarChart data={data}>
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="Index" fill="#8884d8" />
            </BarChart>
            </ResponsiveContainer>
        </div>
    );
}