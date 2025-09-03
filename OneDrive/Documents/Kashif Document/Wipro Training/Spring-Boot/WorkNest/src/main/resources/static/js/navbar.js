// navbar.js - Dynamic navbar loading and functionality
async function loadNavbar() {
    try {
        const response = await fetch('/fragments/navbar.html');
        const navbarHtml = await response.text();
        document.getElementById('navbar-container').innerHTML = navbarHtml;
        setupNavbarEventListeners();
    } catch (error) {
        console.error('Failed to load navbar:', error);
    }
}

function setupNavbarEventListeners() {
    // Navigation function
    window.navigateTo = function(path) {
        window.location.href = path;
    };

    // Logout functionality
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userRole');
            localStorage.removeItem('userName');
            window.location.href = '/login.html';
        });
    }

    // Update navbar based on authentication
    updateNavbarAuthState();
}

function updateNavbarAuthState() {
    const token = localStorage.getItem('jwtToken');
    const userRole = localStorage.getItem('userRole');
    const navLinks = document.querySelector('.nav-links');
    
    if (!navLinks) return;

    if (token) {
        // User is authenticated
        navLinks.innerHTML = `
            <li><a href="#" onclick="navigateTo('/user/dashboard.html')">Dashboard</a></li>
            ${userRole === 'ROLE_ADMIN' ? `
                <li><a href="#" onclick="navigateTo('/admin/dashboard.html')">Admin Dashboard</a></li>
                <li><a href="#" onclick="navigateTo('/admin/tasks.html')">Manage Tasks</a></li>
                <li><a href="#" onclick="navigateTo('/admin/groups.html')">Manage Groups</a></li>
            ` : ''}
            <li>
                <button id="logoutBtn" class="logout-btn">Logout</button>
            </li>
        `;
        
        // Re-attach logout event listener
        document.getElementById('logoutBtn').addEventListener('click', () => {
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userRole');
            localStorage.removeItem('userName');
            window.location.href = '/login.html';
        });
    } else {
        // User is not authenticated
        navLinks.innerHTML = `
            <li><a href="/login.html">Login</a></li>
            <li><a href="/register.html">Register</a></li>
        `;
    }
}

async function loadFooter() {
    try {
        const response = await fetch('/fragments/footer.html');
        const footerHtml = await response.text();
        document.getElementById('footer-container').innerHTML = footerHtml;
    } catch (error) {
        console.error('Failed to load footer:', error);
    }
}