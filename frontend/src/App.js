import React from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Link,
  Outlet,
} from "react-router-dom";
import "./App.css";

// Import the actual page components
import UserManagementPage from "./pages/UserManagementPage.jsx";
import ReportsPage from "./pages/ReportsPage.jsx";

// Placeholder components for now (Home and ModuleIntegration can remain if not yet developed)
const Home = () => (
  <div style={{ padding: "20px" }}>
    <h2>Home - SAT Simulation</h2>
    <p>Welcome! Use the navigation to access User Management or Reports.</p>
  </div>
);

// const UserManagement = () => ( ... placeholder removed ... );
// const Reports = () => ( ... placeholder removed ... );

const ModuleIntegration = () => (
  <div style={{ padding: "20px" }}>
    <h3>Module Integration (For Hospital, Pharmacy, Insurance modules)</h3>
    <p>
      This section would typically not have a direct UI in the admin portal, but
      represents the REST services and JSON formats for communication.
    </p>
    <p>
      The client modules (Hospital, Pharmacy, Insurance) would authenticate and
      use these services.
    </p>
  </div>
);

const AppLayout = () => (
  <div className="App">
    {" "}
    {/* Ensure .App class is used if App.css targets it for root styling */}
    <nav>
      <ul>
        <li>
          <Link to="/">Home</Link>
        </li>
        <li>
          <Link to="/admin/users">User Management</Link>
        </li>
        <li>
          <Link to="/admin/reports">Reports</Link>
        </li>
        <li>
          <Link to="/module-info">Module Integration Info</Link>
        </li>
      </ul>
    </nav>
    <hr />
    <div className="content-area">
      {" "}
      {/* Added a wrapper for content padding */}
      <Outlet /> {/* Child routes will render here */}
    </div>
  </div>
);

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<Home />} />
          {/* Use the actual imported page components */}
          <Route path="admin/users" element={<UserManagementPage />} />
          <Route path="admin/reports" element={<ReportsPage />} />
          <Route path="module-info" element={<ModuleIntegration />} />
          {/* Add more routes as needed */}
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
