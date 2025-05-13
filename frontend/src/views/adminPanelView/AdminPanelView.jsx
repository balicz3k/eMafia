import React from 'react';
import MainLayout from '../../layouts/mainLayout/MainLayout';
import UserManagementTable from '../../components/userManagementTable/UserManagementTable';
import styles from './AdminPanelView.module.css';

const AdminPanelView = () => {
    return (
        <MainLayout>
            <div className={styles.adminPanelContainer}>
                <h1>Admin Panel - User Management</h1>
                <UserManagementTable />
            </div>
        </MainLayout>
    );
};

export default AdminPanelView;