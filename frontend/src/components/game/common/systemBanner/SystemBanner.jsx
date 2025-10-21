import React from "react";
import styles from "./SystemBanner.module.css";

const SystemBanner = ({ message }) => {
  // W kolejnej iteracji podłącz WS i dynamiczne ogłoszenia
  if (!message) return null;
  return <div className={styles.banner}>{message}</div>;
};

export default SystemBanner;
