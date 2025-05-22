import React, { useState } from "react";
import styles from "./SearchGameRoomBar.module.css";
import { FaSearch } from "react-icons/fa";

const SearchGameRoomBar = ({ onSearch }) => {
  const [searchTerm, setSearchTerm] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    onSearch(searchTerm);
  };

  return (
    <form className={styles.searchForm} onSubmit={handleSubmit}>
      <div className={styles.inputWrapper}>
        {<FaSearch className={styles.searchIcon} />}
        <input
          type="text"
          className={styles.searchInput}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search games by name..."
        />
      </div>
      <button type="submit" className={styles.searchButton}>
        Search
      </button>
    </form>
  );
};

export default SearchGameRoomBar;
