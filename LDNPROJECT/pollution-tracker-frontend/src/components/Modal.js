import React from "react";

export default function Modal({ message, onClose }) {
  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100vw",
        height: "100vh",
        backgroundColor: "rgba(0,0,0,0.5)", // dark blur overlay
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 2000,
      }}
    >
      <div
        style={{
          backgroundColor: "#fff",
          padding: "20px",
          borderRadius: "10px",
          maxWidth: "400px",
          textAlign: "center",
          boxShadow: "0 4px 12px rgba(0,0,0,0.3)",
        }}
      >
        <h3 style={{ marginBottom: "15px", color: "#E65100" }}>Notice</h3>
        <p>{message}</p>
        <button
          onClick={onClose}
          style={{
            marginTop: "20px",
            padding: "8px 16px",
            backgroundColor: "#FFB74D",
            border: "none",
            borderRadius: "6px",
            cursor: "pointer",
            color: "#fff",
            fontWeight: "bold",
          }}
        >
          Close
        </button>
      </div>
    </div>
  );
}