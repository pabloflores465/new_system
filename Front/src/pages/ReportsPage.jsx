import React, { useState, useCallback } from "react";

const API_BASE_URL = "http://localhost:8080/api";
const AUTH_USER = "admin"; // Replace with your admin username
const AUTH_PASS = "password"; // Replace with your admin password

function ReportsPage() {
  const [reportType, setReportType] = useState("nit"); // 'nit', 'module', 'general', 'provider', 'itemCategory'
  const [nit, setNit] = useState("");
  const [moduleRole, setModuleRole] = useState("MODULE_HOSPITAL");
  const [providerName, setProviderName] = useState("");
  const [itemCategory, setItemCategory] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [reportData, setReportData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleFetchReport = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setReportData(null);

    let url = "";
    const params = new URLSearchParams();

    if (reportType === "nit") {
      if (!nit) {
        setError("NIT is required for this report.");
        setIsLoading(false);
        return;
      }
      url = `${API_BASE_URL}/invoicing/reports/by-nit`;
      params.append("nit", nit);
    } else if (reportType === "module") {
      url = `${API_BASE_URL}/invoicing/reports/by-module`;
      params.append("moduleRole", moduleRole);
    } else if (reportType === "general") {
      url = `${API_BASE_URL}/invoicing/reports/general`;
    } else if (reportType === "provider") {
      if (!providerName) {
        setError("Provider Name is required.");
        setIsLoading(false);
        return;
      }
      url = `${API_BASE_URL}/invoicing/reports/by-provider`;
      params.append("providerName", providerName);
    } else if (reportType === "itemCategory") {
      if (!itemCategory) {
        setError("Item Category is required.");
        setIsLoading(false);
        return;
      }
      url = `${API_BASE_URL}/invoicing/reports/by-item-category`;
      params.append("category", itemCategory);
    }

    if (startDate)
      params.append("startDate", new Date(startDate).toISOString());
    if (endDate) {
      // Adjust end date to include the whole day if only date is picked
      const endOfDay = new Date(endDate);
      endOfDay.setHours(23, 59, 59, 999);
      params.append("endDate", endOfDay.toISOString());
    }

    const fullUrl = url
      ? `${url}?${params.toString()}`
      : `${url}${params.toString() ? "?" + params.toString() : ""}`;
    if (!url) {
      setError("Invalid report type selected.");
      setIsLoading(false);
      return;
    }

    try {
      const response = await fetch(fullUrl, {
        headers: {
          Authorization: "Basic " + btoa(`${AUTH_USER}:${AUTH_PASS}`),
        },
      });
      if (response.status === 204) {
        setReportData([]);
      } else if (!response.ok) {
        throw new Error(
          `HTTP error! status: ${response.status} - ${await response.text()}`
        );
      } else {
        const data = await response.json();
        setReportData(data);
      }
    } catch (err) {
      console.error("Failed to fetch report:", err);
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h1>Admin Reports</h1>
      <form
        onSubmit={handleFetchReport}
        style={{
          marginBottom: "20px",
          border: "1px solid #ccc",
          padding: "15px",
        }}
      >
        <div>
          <label>Report Type: </label>
          <select
            value={reportType}
            onChange={(e) => setReportType(e.target.value)}
          >
            <option value="nit">Tax Report by NIT</option>
            <option value="module">Tax Report by Module</option>
            <option value="general">General Sales Report</option>
            <option value="provider">Tax Report by Provider</option>
            <option value="itemCategory">Tax Report by Item Category</option>
          </select>
        </div>

        {reportType === "nit" && (
          <div>
            <label>Client NIT: </label>
            <input
              type="text"
              value={nit}
              onChange={(e) => setNit(e.target.value)}
              required
            />
          </div>
        )}

        {reportType === "module" && (
          <div>
            <label>Module Role: </label>
            <select
              value={moduleRole}
              onChange={(e) => setModuleRole(e.target.value)}
            >
              <option value="MODULE_HOSPITAL">MODULE_HOSPITAL</option>
              <option value="MODULE_PHARMACY">MODULE_PHARMACY</option>
              <option value="MODULE_INSURANCE">MODULE_INSURANCE</option>
              {/* Add other roles that place orders if necessary */}
            </select>
          </div>
        )}

        {reportType === "provider" && (
          <div>
            <label>Provider Name: </label>
            <input
              type="text"
              value={providerName}
              onChange={(e) => setProviderName(e.target.value)}
              required
            />
          </div>
        )}

        {reportType === "itemCategory" && (
          <div>
            <label>Item Category: </label>
            <input
              type="text"
              value={itemCategory}
              onChange={(e) => setItemCategory(e.target.value)}
              required
            />
          </div>
        )}

        <div>
          <label>Start Date (Optional): </label>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
          />
        </div>
        <div>
          <label>End Date (Optional): </label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
          />
        </div>
        <button
          type="submit"
          disabled={isLoading}
          style={{ marginTop: "10px" }}
        >
          Fetch Report
        </button>
      </form>

      {isLoading && <p>Loading report...</p>}
      {error && <p style={{ color: "red" }}>Error: {error}</p>}

      {reportData && (
        <div>
          <h2>Report Results for: {reportType.toUpperCase()}</h2>
          {reportData.length === 0 ? (
            <p>No data found for the selected criteria.</p>
          ) : (
            <table style={{ borderCollapse: "collapse", width: "100%" }}>
              <thead>
                <tr>
                  {/* Adjust headers based on OrderResponseDTO fields you want to show */}
                  <th style={tableHeaderStyle}>Order ID</th>
                  <th style={tableHeaderStyle}>Client Name</th>
                  <th style={tableHeaderStyle}>Client NIT</th>
                  <th style={tableHeaderStyle}>Provider Name</th>
                  <th style={tableHeaderStyle}>Order Date</th>
                  <th style={tableHeaderStyle}>Total Taxes</th>
                  <th style={tableHeaderStyle}>Total Amount</th>
                  <th style={tableHeaderStyle}>PDF</th>
                </tr>
              </thead>
              <tbody>
                {reportData.map((order) => (
                  <tr key={order.orderId}>
                    <td style={tableCellStyle}>{order.orderId}</td>
                    <td style={tableCellStyle}>{order.clientName}</td>
                    <td style={tableCellStyle}>{order.clientNit}</td>
                    <td style={tableCellStyle}>
                      {order.providerName || "N/A"}
                    </td>
                    <td style={tableCellStyle}>
                      {new Date(order.orderDate).toLocaleString()}
                    </td>
                    <td style={tableCellStyle}>
                      {order.totalTaxes?.toFixed(2)}
                    </td>
                    <td style={tableCellStyle}>
                      {order.totalAmount?.toFixed(2)}
                    </td>
                    <td style={tableCellStyle}>
                      {order.invoicePdfUrl ? (
                        <a
                          href={`${API_BASE_URL}/invoicing${order.invoicePdfUrl}`}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          Download
                        </a>
                      ) : (
                        "N/A"
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

// Re-use styles from UserManagementPage or define globally
const tableHeaderStyle = {
  border: "1px solid #ddd",
  padding: "8px",
  textAlign: "left",
  backgroundColor: "#f2f2f2",
};

const tableCellStyle = {
  border: "1px solid #ddd",
  padding: "8px",
  textAlign: "left",
};

export default ReportsPage;
