import React from 'react';
import { Navigate } from 'react-router-dom';
import { decodeJwt } from '../utils/decodeJwt';

const AdminRoute = ({ children }) => {
    const token = localStorage.getItem('token');

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    const decodedToken = decodeJwt(token);
    const isAdmin = decodedToken && decodedToken.roles && decodedToken.roles.includes('ROLE_ADMIN');

    if (!isAdmin) {
        alert("Access Denied: You do not have admin privileges.");
        return <Navigate to="/dashboard" replace />;
    }

    return children;
};

export default AdminRoute;