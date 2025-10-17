import { Pie } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";

ChartJS.register(ArcElement, Tooltip, Legend);

export default function PollutantPieChart({ location }) {
  if (!location || !location.airQualityData) return null;

  const { pm25, pm10, no2, so2, o3, co } = location.airQualityData;

  // Filter out pollutants with null or zero values
  const pollutants = [
    { label: "PM2.5", value: pm25 },
    { label: "PM10", value: pm10 },
    { label: "NO2", value: no2 },
    { label: "SO2", value: so2 },
    { label: "O3", value: o3 },
    { label: "CO", value: co },
  ].filter(p => p.value != null && p.value > 0);

  const data = {
    labels: pollutants.map(p => p.label),
    datasets: [
      {
        label: "Pollutant Index",
        data: pollutants.map(p => p.value),
        backgroundColor: [
          "#FF6384",
          "#36A2EB",
          "#FFCE56",
          "#FFA726",
          "#66BB6A",
          "#AB47BC"
        ],
        borderWidth: 1,
      },
    ],
  };

  return (
    <div style={{ maxWidth: "400px", margin: "0 auto" }}>
      <h3 style={{ textAlign: "center" }}>Pollutant Distribution</h3>
      <Pie data={data} />
    </div>
  );
}