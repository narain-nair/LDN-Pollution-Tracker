export default functionexport default function SearchBar({ query, setQuery, suggestions, setSuggestions, onSelect }) {
    const handleChange = async (e) => {
        const val = e.target.value;
        setQuery(val);
    
        if (val.length >= 2) {
          const res = await fetch(`http://localhost:8080/locations/suggest?query=${encodeURIComponent(val)}`);
          const data = await res.json();
          setSuggestions(data);
        } else {
          setSuggestions([]);
        }
    };


  return (
    <div style={{ position: "relative" }}>
      <input
        type="text"
        value={query}
        onChange={handleChange}
        placeholder="Type location name..."
        style={{ padding: "5px", width: "300px" }}
      />
      {suggestions.length > 0 && (
        <ul style={{ border: "1px solid #ccc", listStyle: "none", marginTop: 0, padding: 0, width: "300px", position: "absolute", background: "#fff" }}>
          {suggestions.map((s, i) => (
            <li key={i} onClick={() => onSelect(s.siteCode)} style={{ padding: "5px", cursor: "pointer" }}>
              {s.name} ({s.siteCode})
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}