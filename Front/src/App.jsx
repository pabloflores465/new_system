import React from "react";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import UserManagementPage from "./pages/UserManagementPage";
import ReportsPage from "./pages/ReportsPage";
import "./App.css"; // Assuming you have some global styles

// A simple Home component for the root path
function HomePage() {
  return (
    <div style={{ padding: "20px" }}>
      <h1>SAT Simulation System</h1>
      <p>Welcome to the SAT Simulation System frontend.</p>
      <p>Navigate using the links above.</p>
    </div>
  );
}

function App() {
  return (
    <Router>
      <div>
        <nav style={{ padding: "10px", backgroundColor: "#eee" }}>
          <Link to="/" style={{ marginRight: "10px" }}>
            Home
          </Link>
          <Link to="/admin/users" style={{ marginRight: "10px" }}>
            User Management
          </Link>
          <Link to="/admin/reports">Reports</Link>
          {/* TODO: Add links to Reports page etc. */}
        </nav>

        <hr />

        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/admin/users" element={<UserManagementPage />} />
          <Route path="/admin/reports" element={<ReportsPage />} />
          {/* TODO: Add routes for Reports page etc. */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;
