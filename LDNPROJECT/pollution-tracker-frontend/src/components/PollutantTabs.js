import { pollutantInfo } from "./utils/PollutantInfo";
import React, { useState } from "react";

export default function PollutantTabs({}) {
    const [openTab, setOpenTab] = useState(null);

    return (
        <div className="mt-6">
          {Object.keys(pollutantInfo).map((key) => {
            const pollutant = pollutantInfo[key];
            const isOpen = openTab === key;
    
            return (
              <div key={key} className="border rounded mb-2">
                <button
                  className="w-full text-left p-3 font-semibold bg-gray-100 hover:bg-gray-200"
                  onClick={() => setOpenTab(isOpen ? null : key)}
                >
                  {pollutant.name}
                </button>
                {isOpen && (
                  <div className="p-3 bg-white">
                    <p><strong>Description:</strong> {pollutant.description}</p>
                    <p><strong>Harmful Effects:</strong> {pollutant.harmful}</p>
                    <p><strong>Source:</strong> {pollutant.source}</p>
                  </div>
                )}
              </div>
            );
          })}
        </div>
    );
}