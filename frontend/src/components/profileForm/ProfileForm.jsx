import React, { useState } from 'react';
import styles from './ProfileForm.module.css';

const ProfileForm = () => {
    const [usernameData, setUsernameData] = useState({ newUsername: '' });
    const [emailData, setEmailData] = useState({ newEmail: '' });
    const [passwordData, setPasswordData] = useState({ oldPassword: '', newPassword: '' });
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    const handleInputChange = (setter) => (e) => {
        setter(prev => ({ ...prev, [e.target.name]: e.target.value }));
        setMessage('');
        setError('');
    };

    const handleSubmit = async (endpoint, data, successMessage) => {
        setMessage('');
        setError('');
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`http://localhost:8080/api/users/profile/${endpoint}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(data),
            });

            if (response.ok) {
                setMessage(successMessage);
                if (endpoint === 'username') setUsernameData({ newUsername: '' });
                if (endpoint === 'email') setEmailData({ newEmail: '' });
                if (endpoint === 'password') setPasswordData({ oldPassword: '', newPassword: '' });
            } else {
                const errorText = await response.text();
                setError(errorText || `Failed to update ${endpoint}`);
            }
        } catch (err) {
            console.error(`Error updating ${endpoint}:`, err);
            setError(`An error occurred while updating ${endpoint}.`);
        }
    };

    return (
        <div className={styles.formContainer}>
            {message && <p className={styles.successMessage}>{message}</p>}
            {error && <p className={styles.errorMessage}>{error}</p>}

            <form onSubmit={(e) => { e.preventDefault(); handleSubmit('username', usernameData, 'Username updated successfully!'); }} className={styles.profileFormSection}>
                <h3>Change Username</h3>
                <div className={styles.formGroup}>
                    <label htmlFor="newUsername">New Username:</label>
                    <input
                        type="text"
                        id="newUsername"
                        name="newUsername"
                        value={usernameData.newUsername}
                        onChange={handleInputChange(setUsernameData)}
                        required
                    />
                </div>
                <button type="submit">Update Username</button>
            </form>

            <form onSubmit={(e) => { e.preventDefault(); handleSubmit('email', emailData, 'Email updated successfully!'); }} className={styles.profileFormSection}>
                <h3>Change Email</h3>
                <div className={styles.formGroup}>
                    <label htmlFor="newEmail">New Email:</label>
                    <input
                        type="email"
                        id="newEmail"
                        name="newEmail"
                        value={emailData.newEmail}
                        onChange={handleInputChange(setEmailData)}
                        required
                    />
                </div>
                <button type="submit">Update Email</button>
            </form>

            <form onSubmit={(e) => { e.preventDefault(); handleSubmit('password', passwordData, 'Password updated successfully!'); }} className={styles.profileFormSection}>
                <h3>Change Password</h3>
                <div className={styles.formGroup}>
                    <label htmlFor="oldPassword">Old Password:</label>
                    <input
                        type="password"
                        id="oldPassword"
                        name="oldPassword"
                        value={passwordData.oldPassword}
                        onChange={handleInputChange(setPasswordData)}
                        required
                    />
                </div>
                <div className={styles.formGroup}>
                    <label htmlFor="newPassword">New Password:</label>
                    <input
                        type="password"
                        id="newPassword"
                        name="newPassword"
                        value={passwordData.newPassword}
                        onChange={handleInputChange(setPasswordData)}
                        required
                    />
                </div>
                <button type="submit">Update Password</button>
            </form>
        </div>
    );
};

export default ProfileForm;