import React from "react";
import MainLayout from "../../layouts/mainLayout/MainLayout";
import ProfileForm from "../../components/profileForm/ProfileForm";
import styles from "./ProfileView.module.css";

const ProfileView = () => {
  return (
    <MainLayout>
      <div className={styles.profileViewContainer}>
        <h1>Your Profile</h1>
        <ProfileForm />
      </div>
    </MainLayout>
  );
};

export default ProfileView;
