// auth.js - Enhanced authentication utilities
function checkAuth(requiredRole = null) {
    const token = sessionStorage.getItem('jwtToken');
    const userRole = sessionStorage.getItem('userRole');
    
    if (!token) {
        window.location.href = '/login';
        return false;
    }
    
    // Check if token is expired
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expirationTime = payload.exp * 1000;
        const currentTime = Date.now();
        
        if (currentTime >= expirationTime) {
            sessionStorage.clear();
            window.location.href = '/login';
            return false;
        }
    } catch (error) {
        sessionStorage.clear();
        window.location.href = '/login';
        return false;
    }
    
    // Check role if required (compare without ROLE_ prefix)
    if (requiredRole) {
        // Remove ROLE_ prefix from stored role for comparison
        const storedRoleWithoutPrefix = userRole ? userRole.replace('ROLE_', '') : '';
        if (storedRoleWithoutPrefix !== requiredRole) {
            window.location.href = '/unauthorized.html';
            return false;
        }
    }
    
    return true;
}

async function fetchWithAuth(url, options = {}) {
    const token = sessionStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = '/login';
        throw new Error('No authentication token');
    }

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers
    };

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        sessionStorage.clear();
        window.location.href = '/login';
        throw new Error('Authentication failed');
    }
    
    return response;
}

function logout() {
    sessionStorage.removeItem('jwtToken');
    sessionStorage.removeItem('userRole');
    sessionStorage.removeItem('userName');
    window.location.href = '/login';
}

function getUserInfo() {
    return {
        name: sessionStorage.getItem('userName'),
        role: sessionStorage.getItem('userRole'),
        token: sessionStorage.getItem('jwtToken')
    };
}