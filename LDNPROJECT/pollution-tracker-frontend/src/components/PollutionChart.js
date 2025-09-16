import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export default function PollutantChart({ locations }) {
  if (!locations || locations.length === 0) return null;

  const data = Object.keys(locations[0].airQualityData || {})
    .filter(k => ["pm25","pm10","no2","so2","o3","co"].includes(k))
    .map(k => ({ name: k.toUpperCase(), value: locations[0].airQualityData[k] ?? 0 }));

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={data}>
        <XAxis dataKey="name" />
        <YAxis />
        <Tooltip />
        <Legend />
        <Bar dataKey="value" fill="#8884d8" />
      </BarChart>
    </ResponsiveContainer>
  );
}