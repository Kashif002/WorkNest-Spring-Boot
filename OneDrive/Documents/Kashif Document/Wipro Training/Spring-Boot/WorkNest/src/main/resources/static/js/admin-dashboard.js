// admin-dashboard.js - Admin dashboard functionality
document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication and admin role (use 'ADMIN' without ROLE_ prefix)
    if (!checkAuth('ADMIN')) return;
    
    try {
        // Load user info
        const userInfo = getUserInfo();
        document.getElementById('adminWelcome').textContent = `Welcome, ${userInfo.name}!`;
        
        // Load dashboard data
        const response = await fetchWithAuth('/api/admin/dashboard-data');
        const data = await response.json();
        
        // Update statistics
        document.getElementById('total-tasks').textContent = data.tasks.length;
        document.getElementById('total-users').textContent = data.users.length;
        document.getElementById('pending-tasks').textContent = data.tasks.filter(t => t.status === 'PENDING').length;
        document.getElementById('completed-tasks').textContent = data.tasks.filter(t => t.status === 'COMPLETED').length;
        
        // Update recent tasks table
        const tasksTable = document.getElementById('tasks-table-body');
        tasksTable.innerHTML = data.tasks.slice(0, 5).map(task => `
            <tr>
                <td>${task.title}</td>
                <td><span class="status-badge status-${task.status}">${task.status}</span></td>
                <td>${new Date(task.dueDate).toLocaleDateString()}</td>
                <td>${task.assignedUsers.map(u => u.name).join(', ')}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Failed to load admin dashboard:', error);
        alert('Error loading dashboard data');
    }
});