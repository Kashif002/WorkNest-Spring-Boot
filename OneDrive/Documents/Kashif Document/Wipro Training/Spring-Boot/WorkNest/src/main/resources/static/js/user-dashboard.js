// user-dashboard.js - User dashboard functionality
document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication (no specific role required for user dashboard)
    if (!checkAuth()) return;
    
    try {
        // Load user info
        const userInfo = getUserInfo();
        document.getElementById('userWelcome').textContent = `Welcome, ${userInfo.name}!`;
        
        // Load user tasks
        const response = await fetchWithAuth('/api/user/tasks');
        const tasks = await response.json();
        
        const tasksContainer = document.getElementById('tasksContainer');
        if (tasks.length === 0) {
            tasksContainer.innerHTML = '<p>You have no tasks assigned to you. Great job!</p>';
        } else {
            tasksContainer.innerHTML = tasks.map(task => `
                <div class="task-card">
                    <div class="task-header">
                        <div>
                            <h3 class="task-title">
                                <a href="/user/tasks/${task.id}">${task.title}</a>
                            </h3>
                            <small>Due on: ${new Date(task.dueDate).toLocaleDateString()}</small>
                        </div>
                        <div class="task-status">
                            <span class="status-badge status-${task.status}">${task.status}</span>
                        </div>
                    </div>
                    <p>${task.description}</p>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load dashboard:', error);
        document.getElementById('tasksContainer').innerHTML = '<p>Error loading tasks. Please try again.</p>';
    }
});