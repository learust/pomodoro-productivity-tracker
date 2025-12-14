// Pomodoro Productivity Tracker JavaScript
class PomodoroApp {
    constructor() {
        this.timerInterval = null;
        this.currentFilter = 'all';
        
        // Local timer state as backup when API fails
        this.localTimer = {
            sessionType: 'WORK',
            state: 'STOPPED',
            totalDurationSeconds: 25 * 60,
            remainingSeconds: 25 * 60,
            completedWorkSessions: 0,
            isRunning: false,
            startedAt: null
        };
        
        this.useLocalTimer = false; // Flag to use local timer when API fails
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadInitialData();
        this.startPeriodicUpdates();
    }

    bindEvents() {
        // Timer controls
        document.getElementById('startBtn').addEventListener('click', () => this.startTimer());
        document.getElementById('pauseBtn').addEventListener('click', () => this.pauseTimer());
        document.getElementById('stopBtn').addEventListener('click', () => this.stopTimer());
        document.getElementById('completeBtn').addEventListener('click', () => this.completeSession());
        document.getElementById('configBtn').addEventListener('click', () => this.openConfigModal());

        // Configuration modal
        document.getElementById('closeModal').addEventListener('click', () => this.closeConfigModal());
        document.getElementById('saveConfig').addEventListener('click', () => this.saveConfiguration());
        document.getElementById('cancelConfig').addEventListener('click', () => this.closeConfigModal());
        
        // Close modal when clicking outside
        document.getElementById('configModal').addEventListener('click', (e) => {
            if (e.target.id === 'configModal') this.closeConfigModal();
        });

        // Task management
        document.getElementById('addTaskBtn').addEventListener('click', () => this.addTask());
        document.getElementById('taskInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.addTask();
        });

        // Task filters
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.setTaskFilter(e.target.dataset.filter));
        });
    }

    async loadInitialData() {
        await this.updateTimerStatus();
        await this.loadTasks();
        await this.loadProgressChart();
        await this.loadSessionHistory();
    }

    startPeriodicUpdates() {
        // Clear any existing timer
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
        }
        
        // Update timer every second
        this.timerInterval = setInterval(async () => {
            await this.updateTimerStatus();
        }, 1000);

        // Update other data every 30 seconds
        setInterval(() => {
            this.loadTasks();
            this.loadSessionHistory();
        }, 30000);
    }

    // Timer Functions
    async startTimer() {
        if (this.useLocalTimer) {
            this.startLocalTimer();
            return;
        }
        
        try {
            const response = await fetch('/api/timer/start', { method: 'POST' });
            if (response.ok) {
                await this.updateTimerStatus();
                this.showMessage('Timer started!', 'success');
            }
        } catch (error) {
            console.error('API failed, switching to local timer');
            this.switchToLocalTimer();
            this.startLocalTimer();
        }
    }
    
    startLocalTimer() {
        this.localTimer.isRunning = true;
        this.localTimer.state = 'RUNNING';
        this.localTimer.startedAt = Date.now();
        this.showMessage('Timer started (local mode)!', 'success');
    }

    async pauseTimer() {
        try {
            const response = await fetch('/api/timer/pause', { method: 'POST' });
            if (response.ok) {
                await this.updateTimerStatus();
                this.showMessage('Timer paused', 'success');
            }
        } catch (error) {
            this.showMessage('Failed to pause timer', 'error');
        }
    }

    async stopTimer() {
        try {
            const response = await fetch('/api/timer/stop', { method: 'POST' });
            if (response.ok) {
                await this.updateTimerStatus();
                this.showMessage('Timer stopped', 'success');
            }
        } catch (error) {
            this.showMessage('Failed to stop timer', 'error');
        }
    }

    async completeSession() {
        try {
            const response = await fetch('/api/timer/complete', { method: 'POST' });
            if (response.ok) {
                await this.updateTimerStatus();
                await this.loadSessionHistory();
                await this.loadProgressChart();
                this.showMessage('Session completed!', 'success');
            }
        } catch (error) {
            this.showMessage('Failed to complete session', 'error');
        }
    }

    async updateTimerStatus() {
        console.log('updateTimerStatus called at', new Date().toISOString());
        
        // Use local timer if we're in local mode or test mode
        if (this.useLocalTimer || window.location.search.includes('test=true')) {
            console.log('Using local timer');
            this.updateLocalTimer();
            this.updateTimerDisplay(this.localTimer);
            return;
        }
        
        try {
            const response = await fetch('/api/timer/status', {
                method: 'GET',
                timeout: 2000 // 2 second timeout
            });
            console.log('Response status:', response.status);
            
            if (response.ok) {
                const timer = await response.json();
                console.log('Timer data received:', timer);
                this.updateTimerDisplay(timer);
            } else {
                console.error('Failed to fetch timer status:', response.status);
                this.switchToLocalTimer();
            }
        } catch (error) {
            console.error('Failed to update timer status:', error);
            this.switchToLocalTimer();
        }
    }
    
    switchToLocalTimer() {
        console.log('Switching to local timer mode');
        this.useLocalTimer = true;
        this.updateLocalTimer();
        this.updateTimerDisplay(this.localTimer);
    }
    
    updateLocalTimer() {
        // If local timer is running, update remaining seconds
        if (this.localTimer.isRunning && this.localTimer.startedAt) {
            const elapsed = Math.floor((Date.now() - this.localTimer.startedAt) / 1000);
            const newRemaining = Math.max(0, this.localTimer.totalDurationSeconds - elapsed);
            this.localTimer.remainingSeconds = newRemaining;
            
            if (newRemaining === 0) {
                this.localTimer.isRunning = false;
                this.localTimer.state = 'STOPPED';
                console.log('Local timer finished');
            }
        }
    }

    updateTimerDisplay(timer) {
        console.log('updateTimerDisplay called with:', timer);
        
        // Force update the time display first
        const timeDisplayElement = document.getElementById('timeDisplay');
        if (timeDisplayElement && timer && timer.remainingSeconds !== undefined) {
            const formattedTime = this.formatTime(timer.remainingSeconds);
            console.log('Updating time display:', formattedTime, 'from seconds:', timer.remainingSeconds);
            timeDisplayElement.textContent = formattedTime;
            timeDisplayElement.style.color = '#007bff'; // Force a visual change to confirm update
        }
        
        // Update other elements
        const sessionTypeElement = document.getElementById('sessionType');
        const sessionStateElement = document.getElementById('sessionState');
        const completedSessionsElement = document.getElementById('completedSessions');
        
        if (sessionTypeElement && timer) sessionTypeElement.textContent = timer.sessionType || 'WORK';
        if (sessionStateElement && timer) sessionStateElement.textContent = timer.state || 'STOPPED';
        if (completedSessionsElement && timer) completedSessionsElement.textContent = timer.completedWorkSessions || 0;
        
        // Update button states
        const isRunning = timer.state === 'RUNNING';
        const isPaused = timer.state === 'PAUSED';
        const isStopped = timer.state === 'STOPPED';
        
        const startBtn = document.getElementById('startBtn');
        const pauseBtn = document.getElementById('pauseBtn');
        const stopBtn = document.getElementById('stopBtn');
        const completeBtn = document.getElementById('completeBtn');
        
        if (startBtn) startBtn.disabled = isRunning;
        if (pauseBtn) pauseBtn.disabled = !isRunning;
        if (stopBtn) stopBtn.disabled = isStopped;
        if (completeBtn) completeBtn.disabled = isStopped;
    }

    formatTime(seconds) {
        console.log('formatTime called with:', seconds, 'type:', typeof seconds);
        
        // Ensure seconds is a valid number
        if (typeof seconds !== 'number' || isNaN(seconds) || seconds < 0) {
            seconds = 0;
        }
        
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        const result = `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
        console.log('formatTime result:', result);
        return result;
    }

    // Task Functions
    async addTask() {
        const taskInput = document.getElementById('taskInput');
        const taskPriority = document.getElementById('taskPriority');
        const text = taskInput.value.trim();
        
        if (!text) return;
        
        try {
            const response = await fetch('/api/tasks', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    text: text,
                    priority: parseInt(taskPriority.value)
                })
            });
            
            if (response.ok) {
                taskInput.value = '';
                await this.loadTasks();
                this.showMessage('Task added!', 'success');
            }
        } catch (error) {
            this.showMessage('Failed to add task', 'error');
        }
    }

    async loadTasks() {
        try {
            const today = new Date().toISOString().split('T')[0];
            const response = await fetch(`/api/tasks/${today}`);
            if (response.ok) {
                const tasks = await response.json();
                this.displayTasks(tasks);
                this.updateTaskStats(tasks);
            }
        } catch (error) {
            console.error('Failed to load tasks:', error);
        }
    }

    displayTasks(tasks) {
        const taskList = document.getElementById('taskList');
        
        // Filter tasks based on current filter
        const filteredTasks = tasks.filter(task => {
            if (this.currentFilter === 'completed') return task.completed;
            if (this.currentFilter === 'incomplete') return !task.completed;
            return true; // 'all'
        });
        
        if (filteredTasks.length === 0) {
            taskList.innerHTML = '<div class="loading">No tasks found</div>';
            return;
        }
        
        taskList.innerHTML = filteredTasks.map(task => `
            <div class="task-item ${task.completed ? 'completed' : ''}">
                <input type="checkbox" class="task-checkbox" ${task.completed ? 'checked' : ''} 
                       onchange="app.toggleTask(${task.id}, this.checked)">
                <span class="task-text">${this.escapeHtml(task.text)}</span>
                <span class="task-priority priority-${task.priority}">
                    Priority ${task.priority}
                </span>
                <div class="task-actions">
                    <button class="task-delete" onclick="app.deleteTask(${task.id})">Delete</button>
                </div>
            </div>
        `).join('');
    }

    async toggleTask(taskId, completed) {
        try {
            const endpoint = completed ? `/api/tasks/${taskId}/complete` : `/api/tasks/${taskId}`;
            const method = completed ? 'PATCH' : 'PUT';
            
            let response;
            if (completed) {
                response = await fetch(endpoint, { method: 'PATCH' });
            } else {
                // For uncompleting, we need to update the task
                const taskResponse = await fetch(`/api/tasks/task/${taskId}`);
                if (taskResponse.ok) {
                    const task = await taskResponse.json();
                    response = await fetch(`/api/tasks/${taskId}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            text: task.text,
                            priority: task.priority,
                            completed: false
                        })
                    });
                }
            }
            
            if (response && response.ok) {
                await this.loadTasks();
                await this.loadProgressChart();
            }
        } catch (error) {
            this.showMessage('Failed to update task', 'error');
            await this.loadTasks(); // Reload to reset checkbox state
        }
    }

    async deleteTask(taskId) {
        if (!confirm('Are you sure you want to delete this task?')) return;
        
        try {
            const response = await fetch(`/api/tasks/${taskId}`, { method: 'DELETE' });
            if (response.ok) {
                await this.loadTasks();
                this.showMessage('Task deleted', 'success');
            }
        } catch (error) {
            this.showMessage('Failed to delete task', 'error');
        }
    }

    updateTaskStats(tasks) {
        const completed = tasks.filter(t => t.completed).length;
        const total = tasks.length;
        document.getElementById('taskStatsText').textContent = 
            `${completed}/${total} tasks completed today`;
    }

    setTaskFilter(filter) {
        this.currentFilter = filter;
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.filter === filter);
        });
        this.loadTasks();
    }

    // Progress Chart Functions
    async loadProgressChart() {
        try {
            const now = new Date();
            const year = now.getFullYear();
            const month = now.getMonth() + 1;
            
            const response = await fetch(`/api/progress/month/${year}/${month}`);
            if (response.ok) {
                const chartData = await response.json();
                this.displayProgressChart(chartData);
            }
        } catch (error) {
            console.error('Failed to load progress chart:', error);
        }
    }

    displayProgressChart(chartData) {
        const chartGrid = document.getElementById('chartGrid');
        document.getElementById('chartMonth').textContent = 
            `${chartData.monthName} ${chartData.year}`;
        document.getElementById('monthlyHours').textContent = 
            chartData.totalHours.toFixed(1);
        document.getElementById('monthlySessions').textContent = 
            chartData.totalSessions;
        
        chartGrid.innerHTML = chartData.days.map(day => `
            <div class="chart-day" 
                 data-level="${day.productivityLevel}"
                 style="background-color: ${day.cssColor};"
                 title="${day.date}: ${day.description}">
            </div>
        `).join('');
    }

    // Session History Functions
    async loadSessionHistory() {
        try {
            const today = new Date().toISOString().split('T')[0];
            const response = await fetch(`/api/sessions/work/${today}`);
            if (response.ok) {
                const sessions = await response.json();
                this.displaySessionHistory(sessions);
            }
        } catch (error) {
            console.error('Failed to load session history:', error);
        }
    }

    displaySessionHistory(sessions) {
        const historyContainer = document.getElementById('sessionHistory');
        
        if (sessions.length === 0) {
            historyContainer.innerHTML = '<div class="loading">No sessions completed today</div>';
            return;
        }
        
        historyContainer.innerHTML = sessions.map(session => `
            <div class="session-item">
                <div>
                    <span class="session-type-badge session-work">WORK</span>
                    <span>${this.formatDuration(session.durationSeconds)} 
                          (${this.formatTime(session.startTime)} - ${this.formatTime(session.endTime)})</span>
                </div>
                <div>${session.durationHours.toFixed(2)} hours</div>
            </div>
        `).join('');
    }

    formatDuration(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}m ${remainingSeconds}s`;
    }

    formatTime(isoString) {
        return new Date(isoString).toLocaleTimeString('en-US', { 
            hour: '2-digit', 
            minute: '2-digit',
            hour12: false 
        });
    }

    // Utility Functions
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    showMessage(message, type) {
        const messageDiv = document.createElement('div');
        messageDiv.className = type;
        messageDiv.textContent = message;
        
        const container = document.querySelector('.container');
        container.insertBefore(messageDiv, container.firstChild);
        
        setTimeout(() => {
            messageDiv.remove();
        }, 3000);
    }

    // Configuration Functions
    openConfigModal() {
        this.loadCurrentSettings();
        document.getElementById('configModal').style.display = 'block';
    }

    closeConfigModal() {
        document.getElementById('configModal').style.display = 'none';
    }

    async loadCurrentSettings() {
        try {
            const response = await fetch('/api/timer/settings');
            if (response.ok) {
                const settings = await response.json();
                document.getElementById('workDuration').value = Math.floor(settings.workDurationSeconds / 60);
                document.getElementById('shortBreakDuration').value = Math.floor(settings.shortBreakDurationSeconds / 60);
                document.getElementById('longBreakDuration').value = Math.floor(settings.longBreakDurationSeconds / 60);
            }
        } catch (error) {
            console.error('Failed to load settings:', error);
            // Set defaults if loading fails
            document.getElementById('workDuration').value = 25;
            document.getElementById('shortBreakDuration').value = 5;
            document.getElementById('longBreakDuration').value = 15;
        }
    }

    async saveConfiguration() {
        const workMinutes = parseInt(document.getElementById('workDuration').value);
        const shortBreakMinutes = parseInt(document.getElementById('shortBreakDuration').value);
        const longBreakMinutes = parseInt(document.getElementById('longBreakDuration').value);

        // Validation
        if (workMinutes < 1 || workMinutes > 120 || 
            shortBreakMinutes < 1 || shortBreakMinutes > 60 ||
            longBreakMinutes < 1 || longBreakMinutes > 60) {
            this.showMessage('Please enter valid durations (Work: 1-120 min, Breaks: 1-60 min)', 'error');
            return;
        }

        const settings = {
            workDurationSeconds: workMinutes * 60,
            shortBreakDurationSeconds: shortBreakMinutes * 60,
            longBreakDurationSeconds: longBreakMinutes * 60
        };

        try {
            const response = await fetch('/api/timer/settings', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(settings)
            });

            if (response.ok) {
                this.closeConfigModal();
                // Force an immediate timer status update to reflect new settings
                await this.updateTimerStatus();
                this.showMessage('Timer settings updated successfully!', 'success');
            } else {
                this.showMessage('Failed to save settings', 'error');
            }
        } catch (error) {
            console.error('Error saving settings:', error);
            this.showMessage('Failed to save settings', 'error');
        }
    }
}

// Initialize the app when the page loads
document.addEventListener('DOMContentLoaded', () => {
    window.app = new PomodoroApp();
});

// Handle page visibility changes to update data when user returns
document.addEventListener('visibilitychange', () => {
    if (!document.hidden && window.app) {
        window.app.loadInitialData();
    }
});