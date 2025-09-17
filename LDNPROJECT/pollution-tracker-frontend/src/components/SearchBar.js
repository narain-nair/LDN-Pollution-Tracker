export default function SearchBar({ query, setQuery, suggestions, setSuggestions, onSelect }) {
    const hasAirQualityData = (aqData) => {
        if (!aqData) return false; // null or undefined
        return Object.values(aqData).some(value => value != null);
    };
    
    const handleChange = async (e) => {
      const val = e.target.value;
      setQuery(val);
  
      if (val.length >= 2) {
        const res = await fetch(`http://localhost:8080/locations/suggest?query=${encodeURIComponent(val)}`);
        const data = await res.json();

        console.log("Raw suggestions from backend:", data);

        const filteredSuggestions = data.filter(
            (loc) =>
              loc.airQualityData && 
              Object.values(loc.airQualityData).some((value) => value != null)
        );

        console.log("Filtered suggestions:", filteredSuggestions);
        setSuggestions(filteredSuggestions.length > 0 ? filteredSuggestions : data);
      } else {
        setSuggestions([]);
      }
    };
  
    return (
      <div style={{ position: "relative", width: "300px" }}>
        <input
          type="text"
          value={query}
          onChange={handleChange}
          placeholder="Type location name..."
          style={{
            width: "100%",
            padding: "8px",
            borderRadius: "4px",
            border: "1px solid #ccc",
          }}
        />
        {suggestions.length > 0 && (
          <ul
            style={{
              position: "absolute",
              top: "100%",
              left: 0,
              right: 0,
              maxHeight: "200px",
              overflowY: "auto",
              background: "#fff",
              border: "1px solid #ccc",
              borderRadius: "4px",
              zIndex: 10,
              margin: 0,
              padding: 0,
              listStyle: "none",
            }}
          >
            {suggestions.map((s, i) => (
              <li
                key={i}
                onClick={() => onSelect(s.siteCode)}
                style={{
                  padding: "8px",
                  cursor: "pointer",
                  borderBottom: "1px solid #eee",
                }}
                onMouseEnter={(e) => (e.currentTarget.style.background = "#f0f0f0")}
                onMouseLeave={(e) => (e.currentTarget.style.background = "#fff")}
              >
                {s.name} ({s.siteCode})
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  }