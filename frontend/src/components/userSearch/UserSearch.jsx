import React, { useState } from "react";

const UserSearch = () => {
  const [query, setQuery] = useState(""); // Przechowuje zapytanie wyszukiwania
  const [results, setResults] = useState([]); // Przechowuje wyniki wyszukiwania

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      console.log("Sending search request with query:", query);
      const response = await fetch(`http://localhost:8080/api/users/search?query=${query}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`, // Dodaj token JWT
        },
      });
      console.log("Response status:", response.status);
      if (response.ok) {
        const data = await response.json();
        console.log("Fetched users:", data);
        setResults(data);
      } else {
        const errorText = await response.text();
        console.error("Error response:", errorText);
        alert("Error fetching users");
      }
    } catch (err) {
      console.error("Error during search:", err);
      alert("An error occurred!");
    }
  };

  return (
    <div>
      <h2>Search Users</h2>
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Enter username"
      />
      <button onClick={handleSubmit}>Search</button>
      <ul>
        {results.map((user) => (
          <li key={user.id}>
            {user.username} ({user.email})
          </li>
        ))}
      </ul>
    </div>
  );
};

export default UserSearch;