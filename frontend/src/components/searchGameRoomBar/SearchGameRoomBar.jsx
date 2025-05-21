import React, { useState } from "react";
import styles from "./SearchGameRoomBar.module.css";
import { MdSearch } from "react-icons/md";

const SearchGameRoomBar = ({ onSearch }) => {
  const [searchTerm, setSearchTerm] = useState("");

  const handleInputChange = (event) => {
    setSearchTerm(event.target.value);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    onSearch(searchTerm);
  };

  return (
    <form className={styles.searchForm} onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Search for a game by name..."
        value={searchTerm}
        onChange={handleInputChange}
        className={styles.searchInput}
      />
      <button type="submit" className={styles.searchButton}>
        <MdSearch size={24} />
      </button>
    </form>
  );
};

export default SearchGameRoomBar;
