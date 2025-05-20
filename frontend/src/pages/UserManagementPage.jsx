import React, { useState, useEffect, useCallback } from "react";

const API_BASE_URL = "http://localhost:8080/api"; // Adjust if your backend URL is different

// Basic auth credentials (replace with a real auth mechanism later)
// For testing, you might have an admin user in your backend with these credentials.
const AUTH_USER = "admin"; // Replace with your admin username
const AUTH_PASS = "password"; // Replace with your admin password

const initialNewUserState = {
  username: "",
  password: "",
  role: "MODULE_HOSPITAL", // Default role, or make it selectable
  hospitalServiceCredentials: "",
  pharmacyServiceCredentials: "",
  insuranceServiceCredentials: "",
};

const initialEditUserState = {
  username: "",
  newPassword: "",
  hospitalServiceCredentials: "",
  pharmacyServiceCredentials: "",
  insuranceServiceCredentials: "",
};

function UserManagementPage() {
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [apiActionError, setApiActionError] = useState(null);
  const [apiActionSuccess, setApiActionSuccess] = useState("");

  const [newUser, setNewUser] = useState(initialNewUserState);
  const [isEditing, setIsEditing] = useState(false);
  const [editingUser, setEditingUser] = useState(initialEditUserState);

  const [searchUsername, setSearchUsername] = useState("");
  const [searchedUser, setSearchedUser] = useState(null); // To store the result of a specific user search
  const [searchError, setSearchError] = useState(null);

  const clearMessages = () => {
    setApiActionError(null);
    setApiActionSuccess("");
    setSearchError(null);
  };

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    clearMessages();
    setSearchedUser(null); // Clear single search result when fetching all users
    try {
      const response = await fetch(`${API_BASE_URL}/users`, {
        headers: {
          Authorization: "Basic " + btoa(`${AUTH_USER}:${AUTH_PASS}`),
        },
      });
      if (!response.ok) {
        throw new Error(
          `HTTP error! status: ${response.status} - ${await response.text()}`
        );
      }
      const data = await response.json();
      setUsers(data);
    } catch (e) {
      console.error("Failed to fetch users:", e);
      setError(e.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleNewUserChange = (e) => {
    const { name, value } = e.target;
    setNewUser((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    clearMessages();
    setIsLoading(true);

    // Basic frontend validation
    if (!newUser.username || !newUser.password || !newUser.role) {
      setApiActionError("Username, password, and role are required.");
      setIsLoading(false);
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/users`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa(`${AUTH_USER}:${AUTH_PASS}`),
        },
        body: JSON.stringify(newUser),
      });
      const responseBody = await response.text(); // Read body once
      if (!response.ok) {
        throw new Error(
          `HTTP error! status: ${response.status} - ${responseBody}`
        );
      }
      setApiActionSuccess(`User ${newUser.username} created successfully!`);
      setNewUser(initialNewUserState); // Reset form
      fetchUsers(); // Refresh user list
    } catch (err) {
      console.error("Failed to create user:", err);
      setApiActionError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleEditUserChange = (e) => {
    const { name, value } = e.target;
    setEditingUser((prev) => ({ ...prev, [name]: value }));
  };

  const openEditModal = (user) => {
    setIsEditing(true);
    clearMessages();
    setEditingUser({
      username: user.username,
      newPassword: "", // Clear password field
      hospitalServiceCredentials: user.hospitalServiceCredentials || "",
      pharmacyServiceCredentials: user.pharmacyServiceCredentials || "",
      insuranceServiceCredentials: user.insuranceServiceCredentials || "",
    });
  };

  const handleUpdateUserCredentials = async (e) => {
    e.preventDefault();
    clearMessages();
    setIsLoading(true);

    const payload = {
      newPassword: editingUser.newPassword || undefined, // Send undefined if empty, so backend doesn't try to update with empty string
      hospitalServiceCredentials: editingUser.hospitalServiceCredentials,
      pharmacyServiceCredentials: editingUser.pharmacyServiceCredentials,
      insuranceServiceCredentials: editingUser.insuranceServiceCredentials,
    };
    // Remove newPassword from payload if it's empty string, to make it truly optional on backend if not provided
    if (!editingUser.newPassword) {
      delete payload.newPassword;
    }

    try {
      const response = await fetch(
        `${API_BASE_URL}/users/${editingUser.username}/credentials`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa(`${AUTH_USER}:${AUTH_PASS}`),
          },
          body: JSON.stringify(payload),
        }
      );
      const responseBody = await response.text();
      if (!response.ok) {
        throw new Error(
          `HTTP error! status: ${response.status} - ${responseBody}`
        );
      }
      setApiActionSuccess(
        `Credentials for user ${editingUser.username} updated successfully!`
      );
      setIsEditing(false);
      fetchUsers(); // Refresh user list
    } catch (err) {
      console.error("Failed to update user credentials:", err);
      setApiActionError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteUser = async (userId, username) => {
    if (
      window.confirm(
        `Are you sure you want to delete user ${username} (ID: ${userId})?`
      )
    ) {
      clearMessages();
      setIsLoading(true);
      try {
        const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
          method: "DELETE",
          headers: {
            Authorization: "Basic " + btoa(`${AUTH_USER}:${AUTH_PASS}`),
          },
        });
        if (!response.ok) {
          // For DELETE, 204 No Content is also a success.
          // Backend returns 204 on success, or 404 if not found (which is also an ok outcome here)
          if (response.status !== 204 && response.status !== 404) {
            const responseBody = await response.text();
            throw new Error(
              `HTTP error! status: ${response.status} - ${responseBody}`
            );
          }
        }
        setApiActionSuccess(
          `User ${username} deleted successfully (or was not found).`
        );
        fetchUsers(); // Refresh user list
      } catch (err) {
        console.error("Failed to delete user:", err);
        setApiActionError(err.message);
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleSearchUser = async (e) => {
    e.preventDefault();
    if (!searchUsername.trim()) {
      setSearchError("Please enter a username to search.");
      return;
    }
    setIsLoading(true);
    clearMessages();
    setSearchedUser(null);
    setUsers([]); // Clear the main list when searching for a single user

    try {
      const response = await fetch(
        `${API_BASE_URL}/users/${searchUsername.trim()}`,
        {
          headers: {
            Authorization: "Basic " + btoa(`${AUTH_USER}:${AUTH_PASS}`),
          },
        }
      );
      if (response.status === 404) {
        setSearchError(`User "${searchUsername}" not found.`);
        setSearchedUser(null);
      } else if (!response.ok) {
        const responseBody = await response.text();
        throw new Error(
          `HTTP error! status: ${response.status} - ${responseBody}`
        );
      } else {
        const data = await response.json();
        setSearchedUser(data);
        setSearchError(null);
      }
    } catch (err) {
      console.error("Failed to search user:", err);
      setSearchError(err.message);
      setSearchedUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading && users.length === 0 && !searchedUser)
    return <p>Loading ...</p>;
  if (error) return <p>Error fetching users: {error}</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h1>User Management (Admin)</h1>

      <h2>Create New User</h2>
      <form
        onSubmit={handleCreateUser}
        style={{
          marginBottom: "20px",
          border: "1px solid #ccc",
          padding: "15px",
        }}
      >
        <div>
          <label>Username: </label>
          <input
            type="text"
            name="username"
            value={newUser.username}
            onChange={handleNewUserChange}
            required
          />
        </div>
        <div>
          <label>Password: </label>
          <input
            type="password"
            name="password"
            value={newUser.password}
            onChange={handleNewUserChange}
            required
          />
        </div>
        <div>
          <label>Role: </label>
          <select
            name="role"
            value={newUser.role}
            onChange={handleNewUserChange}
          >
            <option value="MODULE_HOSPITAL">MODULE_HOSPITAL</option>
            <option value="MODULE_PHARMACY">MODULE_PHARMACY</option>
            <option value="MODULE_INSURANCE">MODULE_INSURANCE</option>
            <option value="ADMINISTRATOR">ADMINISTRATOR</option>
            {/* Add other roles as needed, e.g., a general business user role */}
          </select>
        </div>
        <div>
          <label>Hospital Credentials: </label>
          <input
            type="text"
            name="hospitalServiceCredentials"
            value={newUser.hospitalServiceCredentials}
            onChange={handleNewUserChange}
          />
        </div>
        <div>
          <label>Pharmacy Credentials: </label>
          <input
            type="text"
            name="pharmacyServiceCredentials"
            value={newUser.pharmacyServiceCredentials}
            onChange={handleNewUserChange}
          />
        </div>
        <div>
          <label>Insurance Credentials: </label>
          <input
            type="text"
            name="insuranceServiceCredentials"
            value={newUser.insuranceServiceCredentials}
            onChange={handleNewUserChange}
          />
        </div>
        <button
          type="submit"
          disabled={isLoading}
          style={{ marginTop: "10px" }}
        >
          Create User
        </button>
        {isLoading && <p>Processing...</p>}
        {apiActionError && (
          <p style={{ color: "red" }}>Error: {apiActionError}</p>
        )}
        {apiActionSuccess && (
          <p style={{ color: "green" }}>{apiActionSuccess}</p>
        )}
      </form>

      {isEditing && (
        <div
          className="edit-modal" // Added class for styling
        >
          <h2>Edit Credentials for {editingUser.username}</h2>
          <form onSubmit={handleUpdateUserCredentials}>
            <div>
              <label>New Password (optional): </label>
              <input
                type="password"
                name="newPassword"
                value={editingUser.newPassword}
                onChange={handleEditUserChange}
              />
            </div>
            <div>
              <label>Hospital Credentials: </label>
              <input
                type="text"
                name="hospitalServiceCredentials"
                value={editingUser.hospitalServiceCredentials}
                onChange={handleEditUserChange}
              />
            </div>
            <div>
              <label>Pharmacy Credentials: </label>
              <input
                type="text"
                name="pharmacyServiceCredentials"
                value={editingUser.pharmacyServiceCredentials}
                onChange={handleEditUserChange}
              />
            </div>
            <div>
              <label>Insurance Credentials: </label>
              <input
                type="text"
                name="insuranceServiceCredentials"
                value={editingUser.insuranceServiceCredentials}
                onChange={handleEditUserChange}
              />
            </div>
            <button
              type="submit"
              disabled={isLoading}
              style={{ marginTop: "10px" }}
            >
              Update Credentials
            </button>
            <button
              type="button"
              onClick={() => setIsEditing(false)}
              style={{ marginTop: "10px", marginLeft: "10px" }}
              disabled={isLoading}
            >
              Cancel
            </button>
          </form>
        </div>
      )}

      {/* Display API action messages outside modal if not editing */}
      {!isEditing && apiActionError && (
        <p className="error-message">Error: {apiActionError}</p>
      )}
      {!isEditing && apiActionSuccess && (
        <p className="success-message">{apiActionSuccess}</p>
      )}

      <h2>Search User</h2>
      <form
        onSubmit={handleSearchUser}
        style={{ marginBottom: "20px", display: "flex", gap: "10px" }}
      >
        <input
          type="text"
          value={searchUsername}
          onChange={(e) => setSearchUsername(e.target.value)}
          placeholder="Enter username to search"
          style={{ flexGrow: 1, padding: "8px" }}
        />
        <button
          type="submit"
          disabled={isLoading}
          style={{ padding: "8px 15px" }}
        >
          Search
        </button>
        <button
          type="button"
          onClick={fetchUsers} // This will also clear searchedUser and searchError
          disabled={isLoading}
          style={{ padding: "8px 15px" }}
        >
          Show All Users
        </button>
      </form>
      {searchError && <p className="error-message">{searchError}</p>}

      {searchedUser && (
        <div>
          <h3>Searched User Details</h3>
          <table
            style={{
              borderCollapse: "collapse",
              width: "auto", // Make table only as wide as content
              marginBottom: "20px",
            }}
          >
            <thead>
              <tr>
                <th style={tableHeaderStyle}>ID</th>
                <th style={tableHeaderStyle}>Username</th>
                <th style={tableHeaderStyle}>Role</th>
                <th style={tableHeaderStyle}>Is Admin Creator</th>
                <th style={tableHeaderStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style={tableCellStyle}>{searchedUser.id}</td>
                <td style={tableCellStyle}>{searchedUser.username}</td>
                <td style={tableCellStyle}>{searchedUser.role}</td>
                <td style={tableCellStyle}>
                  {searchedUser.adminCreator ? "Yes" : "No"}
                </td>
                <td style={tableCellStyle}>
                  <button
                    onClick={() => openEditModal(searchedUser)}
                    disabled={isLoading}
                    style={{ marginRight: "5px" }}
                  >
                    Edit Credentials
                  </button>
                  <button
                    onClick={() =>
                      handleDeleteUser(searchedUser.id, searchedUser.username)
                    }
                    disabled={isLoading}
                  >
                    Delete User
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      )}

      {!searchedUser && (
        <>
          <h2>Existing Users</h2>
          {/* Message for API actions related to the main list (e.g. delete from list) can go here if not editing */}
          {!isEditing && apiActionError && (
            <p className="error-message">Error: {apiActionError}</p>
          )}
          {!isEditing && apiActionSuccess && (
            <p className="success-message">{apiActionSuccess}</p>
          )}

          {users.length === 0 && !isLoading ? (
            <p>No users found. Use search above or create new users.</p>
          ) : (
            <table style={{ borderCollapse: "collapse", width: "100%" }}>
              <thead>
                <tr>
                  <th style={tableHeaderStyle}>ID</th>
                  <th style={tableHeaderStyle}>Username</th>
                  <th style={tableHeaderStyle}>Role</th>
                  <th style={tableHeaderStyle}>Is Admin Creator</th>
                  <th style={tableHeaderStyle}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td style={tableCellStyle}>{user.id}</td>
                    <td style={tableCellStyle}>{user.username}</td>
                    <td style={tableCellStyle}>{user.role}</td>
                    <td style={tableCellStyle}>
                      {user.adminCreator ? "Yes" : "No"}
                    </td>
                    <td style={tableCellStyle}>
                      <button
                        onClick={() => openEditModal(user)}
                        disabled={isLoading}
                        style={{ marginRight: "5px" }}
                      >
                        Edit Credentials
                      </button>
                      <button
                        onClick={() => handleDeleteUser(user.id, user.username)}
                        disabled={isLoading}
                      >
                        Delete User
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  );
}

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

export default UserManagementPage;
