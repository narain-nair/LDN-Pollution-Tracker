import React from "react";

export default function PageContainer({ children }) {
    return (
        <div className="pt-20 px-4">
          {children}
        </div>
    );
}