import React, { useState } from "react";
import { pollutantInfo } from "./utils/PollutantInfo";

export default function PollutantTabs() {
  const [activeTab, setActiveTab] = useState(Object.keys(pollutantInfo)[0]); // default first tab

  return (
    <div className="mt-6">
      {/* Tab headers */}
      <div style={{ display: "flex", borderBottom: "2px solid #ddd" }}>
        {Object.keys(pollutantInfo).map((key) => {
          const pollutant = pollutantInfo[key];
          return (
            <button
              key={key}
              onClick={() => setActiveTab(key)}
              style={{
                flex: 1,
                padding: "10px",
                border: "none",
                borderBottom: activeTab === key ? "3px solid #007bff" : "3px solid transparent",
                background: "none",
                fontWeight: activeTab === key ? "bold" : "normal",
                cursor: "pointer",
              }}
            >
              {pollutant.name}
            </button>
          );
        })}
      </div>

      {/* Active tab content */}
      <div style={{ padding: "15px", background: "#fff", border: "1px solid #ddd" }}>
        <p>
            <strong>Description:</strong> {pollutantInfo[activeTab].description}
        </p>
        <p>
            <strong>Harmful Effects:</strong> {pollutantInfo[activeTab].harmful}
        </p>
        <p>
            <strong>Prevention measures:</strong> {pollutantInfo[activeTab].tip}
        </p>
        {pollutantInfo[activeTab].source && (
            <p>
            <strong>Source:</strong>{" "}
            <a
                href={pollutantInfo[activeTab].source}
                target="_blank"
                rel="noopener noreferrer"
                style={{ color: "#E65100", textDecoration: "underline" }}
            >
                {pollutantInfo[activeTab].source}   
            </a>
            </p>
        )}
      </div>
    </div>
  );
}