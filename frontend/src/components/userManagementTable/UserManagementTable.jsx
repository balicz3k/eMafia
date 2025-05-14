import React, { useState, useEffect, useCallback } from 'react';
import styles from './UserManagementTable.module.css';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const UserManagementTable = () => {
    const [users, setUsers] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');
    const availableRoles = ['ROLE_USER', 'ROLE_ADMIN'];

    const fetchUsers = useCallback(async () => {
        setIsLoading(true);
        setError('');
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/api/admin/users`, {
                headers: { 'Authorization': `Bearer ${token}` },
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to fetch users');
            }
            const data = await response.json();
            setUsers(data);
        } catch (err) {
            setError(err.message);
            console.error("Error fetching users:", err);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    const handleDeleteUser = async (userId) => {
        if (!window.confirm('Are you sure you want to delete this user?')) return;
        setError('');
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/api/admin/users/${userId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` },
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to delete user');
            }
            setUsers(prevUsers => prevUsers.filter(user => user.id !== userId));
            alert('User deleted successfully.');
        } catch (err) {
            setError(err.message);
            console.error("Error deleting user:", err);
        }
    };

    const handleRoleChange = async (userId, newRoles) => {
        setError('');
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/api/admin/users/${userId}/roles`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({ roles: newRoles }),
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to update roles');
            }
            fetchUsers();
            alert('User roles updated successfully.');
        } catch (err) {
            setError(err.message);
            console.error("Error updating roles:", err);
        }
    };


    if (isLoading) return <p>Loading users...</p>;

    return (
        <div className={styles.tableContainer}>
            {error && <p className={styles.errorMessage}>{error}</p>}
            <button onClick={fetchUsers} className={styles.refreshButton} disabled={isLoading}>Refresh Users</button>
            <table className={styles.userTable}>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Roles</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.username}</td>
                            <td>{user.email}</td>
                            <td>
                                <select
                                    multiple
                                    value={user.roles}
                                    onChange={(e) => {
                                        const selectedRoles = Array.from(e.target.selectedOptions, option => option.value);
                                        handleRoleChange(user.id, selectedRoles);
                                    }}
                                    className={styles.roleSelect}
                                >
                                    {availableRoles.map(role => (
                                        <option key={role} value={role}>
                                            {role}
                                        </option>
                                    ))}
                                </select>
                            </td>
                            <td>
                                <button
                                    onClick={() => handleDeleteUser(user.id)}
                                    className={styles.deleteButton}
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default UserManagementTable;