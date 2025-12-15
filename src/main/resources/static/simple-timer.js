// Simple working timer for Pomodoro app
let timerState = {
    workMinutes: 25,
    breakMinutes: 5,
    remainingSeconds: 25 * 60,
    isRunning: false,
    startTime: null,
    timerInterval: null
};

function updateTimerDisplay() {
    const minutes = Math.floor(timerState.remainingSeconds / 60);
    const seconds = timerState.remainingSeconds % 60;
    const display = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    
    const timeElement = document.getElementById('timeDisplay');
    if (timeElement) {
        timeElement.textContent = display;
        console.log('Timer display updated:', display);
    }
}

function startTimer() {
    if (!timerState.isRunning && timerState.remainingSeconds > 0) {
        timerState.isRunning = true;
        timerState.startTime = Date.now();
        timerState.totalDurationSeconds = timerState.remainingSeconds;
        
        timerState.timerInterval = setInterval(() => {
            if (timerState.remainingSeconds > 0) {
                timerState.remainingSeconds--;
                updateTimerDisplay();
            } else {
                // Timer completed
                recordSession();
                stopTimer();
                alert('Pomodoro session complete!');
            }
        }, 1000);
        
        console.log('Timer started');
        updateTimerDisplay();
    }
}

function pauseTimer() {
    if (timerState.isRunning) {
        timerState.isRunning = false;
        if (timerState.timerInterval) {
            clearInterval(timerState.timerInterval);
            timerState.timerInterval = null;
        }
        console.log('Timer paused');
    }
}

function stopTimer() {
    timerState.isRunning = false;
    if (timerState.timerInterval) {
        clearInterval(timerState.timerInterval);
        timerState.timerInterval = null;
    }
    
    // If timer was running and time was elapsed, record the session
    if (timerState.startTime && timerState.remainingSeconds < timerState.totalDurationSeconds) {
        recordSession();
    }
    
    timerState.remainingSeconds = timerState.workMinutes * 60;
    timerState.totalDurationSeconds = timerState.workMinutes * 60;
    updateTimerDisplay();
    console.log('Timer stopped and reset');
}

async function recordSession() {
    try {
        const sessionData = {
            sessionType: 'WORK',
            durationSeconds: timerState.totalDurationSeconds - timerState.remainingSeconds,
            startTime: new Date(timerState.startTime).toISOString(),
            endTime: new Date().toISOString()
        };
        
        const response = await fetch('/api/sessions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(sessionData)
        });
        
        if (response.ok) {
            console.log('Session recorded');
            // Refresh session history and progress chart
            loadSessionHistory();
            loadProgressChart();
        }
    } catch (error) {
        console.warn('Could not record session:', error);
    }
}

function configureTimer(workMins, breakMins) {
    console.log(`Configuring timer: work=${workMins}, break=${breakMins}`);
    timerState.workMinutes = workMins;
    timerState.breakMinutes = breakMins;
    
    // Update current timer if stopped
    if (!timerState.isRunning) {
        timerState.remainingSeconds = workMins * 60;
        updateTimerDisplay();
    }
}

async function updateBackendSettings(workMins, shortBreakMins, longBreakMins) {
    try {
        const settings = {
            workDurationMinutes: workMins,
            shortBreakDurationMinutes: shortBreakMins,
            longBreakDurationMinutes: longBreakMins,
            longBreakInterval: 4,
            autoStartBreaks: false
        };
        
        const response = await fetch('/api/timer/settings', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(settings)
        });
        
        if (response.ok) {
            console.log('Settings updated on backend');
        } else {
            console.warn('Failed to update backend settings');
        }
    } catch (error) {
        console.warn('Could not connect to backend:', error);
    }
}

function setupTabs() {
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.dataset.tab;
            
            // Remove active class from all tabs and contents
            tabBtns.forEach(b => b.classList.remove('active'));
            tabContents.forEach(c => c.classList.remove('active'));
            
            // Add active class to clicked tab and corresponding content
            btn.classList.add('active');
            document.getElementById(targetTab + '-tab').classList.add('active');
        });
    });
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Simple timer initialized');
    
    // Tab functionality
    setupTabs();
    
    // Override the existing button handlers
    const startBtn = document.getElementById('startBtn');
    const pauseBtn = document.getElementById('pauseBtn');
    const stopBtn = document.getElementById('stopBtn');
    const configBtn = document.getElementById('configBtn');
    
    if (startBtn) {
        startBtn.onclick = startTimer;
    }
    if (pauseBtn) {
        pauseBtn.onclick = pauseTimer;
    }
    if (stopBtn) {
        stopBtn.onclick = stopTimer;
    }
    if (configBtn) {
        configBtn.onclick = function() {
            const modal = document.getElementById('configModal');
            if (modal) {
                modal.style.display = 'flex';
                
                // Pre-fill current values
                const workInput = document.getElementById('workDuration');
                const shortBreakInput = document.getElementById('shortBreakDuration');
                const longBreakInput = document.getElementById('longBreakDuration');
                
                if (workInput) workInput.value = timerState.workMinutes;
                if (shortBreakInput) shortBreakInput.value = timerState.breakMinutes;
                if (longBreakInput) longBreakInput.value = 15; // default long break
            }
        };
    }
    
    // Override config save
    const saveConfigBtn = document.getElementById('saveConfig');
    const closeModalBtn = document.getElementById('closeModal');
    const cancelConfigBtn = document.getElementById('cancelConfig');
    
    if (saveConfigBtn) {
        saveConfigBtn.onclick = function() {
            const workInput = document.getElementById('workDuration');
            const shortBreakInput = document.getElementById('shortBreakDuration'); 
            const longBreakInput = document.getElementById('longBreakDuration');
            
            if (workInput && shortBreakInput && longBreakInput) {
                const workMins = parseInt(workInput.value) || 25;
                const shortBreakMins = parseInt(shortBreakInput.value) || 5;
                const longBreakMins = parseInt(longBreakInput.value) || 15;
                
                // Update local timer
                configureTimer(workMins, shortBreakMins);
                
                // Try to update backend settings
                updateBackendSettings(workMins, shortBreakMins, longBreakMins);
                
                // Close modal
                const modal = document.getElementById('configModal');
                if (modal) {
                    modal.style.display = 'none';
                }
                
                alert(`Timer configured: ${workMins} min work, ${shortBreakMins} min break`);
            }
        };
    }
    
    // Close modal handlers
    if (closeModalBtn) {
        closeModalBtn.onclick = function() {
            const modal = document.getElementById('configModal');
            if (modal) modal.style.display = 'none';
        };
    }
    
    if (cancelConfigBtn) {
        cancelConfigBtn.onclick = function() {
            const modal = document.getElementById('configModal');
            if (modal) modal.style.display = 'none';
        };
    }
    
    // Close modal when clicking outside
    const modal = document.getElementById('configModal');
    if (modal) {
        modal.onclick = function(e) {
            if (e.target === modal) {
                modal.style.display = 'none';
            }
        };
    }
    
    // Task management
    setupTaskManagement();
    
    // Initialize display
    updateTimerDisplay();
    
    // Load initial data
    loadTasks();
    loadProgressChart();
    loadSessionHistory();
});

// Progress Chart Functions
async function loadProgressChart() {
    try {
        const currentYear = new Date().getFullYear();
        const response = await fetch(`/api/progress/chart/${currentYear}`);
        
        if (response.ok) {
            const chartData = await response.json();
            displayProgressChart(chartData);
            updateChartStats(chartData);
        }
    } catch (error) {
        console.error('Error loading progress chart:', error);
    }
}

function displayProgressChart(chartData) {
    const chartGrid = document.getElementById('chartGrid');
    const chartMonth = document.getElementById('chartMonth');
    
    if (!chartGrid || !chartData || !chartData.months) return;
    
    // Update month display
    const currentMonth = new Date().toLocaleDateString('en-US', { 
        month: 'long', 
        year: 'numeric' 
    });
    if (chartMonth) {
        chartMonth.textContent = currentMonth;
    }
    
    // Find current month data
    const currentMonthNum = new Date().getMonth() + 1;
    const monthData = chartData.months.find(m => m.month === currentMonthNum);
    
    if (!monthData) {
        chartGrid.innerHTML = '<div class="no-data">No data for this month</div>';
        return;
    }
    
    // Create grid
    let gridHTML = '';
    monthData.days.forEach(day => {
        const color = getColorForHours(day.totalHours);
        const tooltip = `${day.date}: ${day.totalHours.toFixed(1)} hours`;
        
        gridHTML += `<div class="chart-day" 
                          style="background-color: ${color}" 
                          title="${tooltip}"></div>`;
    });
    
    chartGrid.innerHTML = gridHTML;
}

function getColorForHours(hours) {
    if (hours === 0) return '#ebedf0';
    if (hours < 1) return '#c6e48b'; 
    if (hours < 3) return '#7bc96f';
    return '#239a3b';
}

function updateChartStats(chartData) {
    const monthlyHours = document.getElementById('monthlyHours');
    const monthlySessions = document.getElementById('monthlySessions');
    
    if (chartData && chartData.months && chartData.months.length > 0) {
        const currentMonth = chartData.months[0];
        const totalHours = currentMonth.totalHours || 0;
        const totalSessions = currentMonth.totalSessions || 0;
        
        if (monthlyHours) monthlyHours.textContent = totalHours.toFixed(1);
        if (monthlySessions) monthlySessions.textContent = totalSessions;
    }
}

// Session History Functions
async function loadSessionHistory() {
    try {
        const today = new Date().toISOString().split('T')[0];
        const response = await fetch(`/api/sessions/history?date=${today}&limit=10`);
        
        if (response.ok) {
            const sessions = await response.json();
            displaySessionHistory(sessions);
        }
    } catch (error) {
        console.error('Error loading session history:', error);
    }
}

function displaySessionHistory(sessions) {
    const historyContainer = document.getElementById('sessionHistory');
    
    if (!historyContainer) return;
    
    if (sessions.length === 0) {
        historyContainer.innerHTML = '<div class="no-sessions">No sessions today</div>';
        return;
    }
    
    const sessionHTML = sessions.map(session => {
        const duration = Math.floor(session.durationSeconds / 60);
        const time = new Date(session.startTime).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        return `
            <div class="session-item">
                <span class="session-type">${session.sessionType}</span>
                <span class="session-duration">${duration}m</span>
                <span class="session-time">${time}</span>
            </div>
        `;
    }).join('');
    
    historyContainer.innerHTML = sessionHTML;
}

// Task Management Functions
function setupTaskManagement() {
    const addTaskBtn = document.getElementById('addTaskBtn');
    const taskInput = document.getElementById('taskInput');
    
    if (addTaskBtn) {
        addTaskBtn.onclick = addTask;
    }
    
    if (taskInput) {
        taskInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                addTask();
            }
        });
    }
    
    // Filter buttons
    const filterBtns = document.querySelectorAll('.filter-btn');
    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            const filter = btn.dataset.filter;
            filterTasks(filter);
        });
    });
}

async function addTask() {
    const taskInput = document.getElementById('taskInput');
    const taskPriority = document.getElementById('taskPriority');
    
    const text = taskInput.value.trim();
    const priority = parseInt(taskPriority.value) || 2;
    
    if (!text) return;
    
    try {
        const response = await fetch('/api/tasks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                text: text,
                priority: priority
            })
        });
        
        if (response.ok) {
            taskInput.value = '';
            loadTasks();
        } else {
            alert('Failed to add task');
        }
    } catch (error) {
        console.error('Error adding task:', error);
        alert('Error adding task');
    }
}

async function loadTasks() {
    try {
        const today = new Date().toISOString().split('T')[0];
        const response = await fetch(`/api/tasks/${today}`);
        
        if (response.ok) {
            const tasks = await response.json();
            displayTasks(tasks);
            updateTaskStats(tasks);
        }
    } catch (error) {
        console.error('Error loading tasks:', error);
    }
}

function displayTasks(tasks) {
    const taskList = document.getElementById('taskList');
    
    if (tasks.length === 0) {
        taskList.innerHTML = '<div class="no-tasks">No tasks for today</div>';
        return;
    }
    
    taskList.innerHTML = tasks.map(task => `
        <div class="task-item ${task.completed ? 'completed' : ''}">
            <input type="checkbox" 
                   ${task.completed ? 'checked' : ''} 
                   onchange="toggleTask(${task.id}, this.checked)">
            <span class="task-text">${escapeHtml(task.text)}</span>
            <span class="task-priority priority-${task.priority}">P${task.priority}</span>
            <button onclick="deleteTask(${task.id})" class="delete-btn">Delete</button>
        </div>
    `).join('');
}

function filterTasks(filter) {
    const taskItems = document.querySelectorAll('.task-item');
    
    taskItems.forEach(item => {
        const isCompleted = item.classList.contains('completed');
        
        if (filter === 'all' || 
            (filter === 'completed' && isCompleted) || 
            (filter === 'incomplete' && !isCompleted)) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

async function toggleTask(taskId, completed) {
    try {
        const endpoint = completed ? `/api/tasks/${taskId}/complete` : `/api/tasks/${taskId}`;
        const method = completed ? 'POST' : 'PUT';
        
        const response = await fetch(endpoint, { method });
        
        if (response.ok) {
            loadTasks();
        }
    } catch (error) {
        console.error('Error toggling task:', error);
    }
}

async function deleteTask(taskId) {
    if (!confirm('Delete this task?')) return;
    
    try {
        const response = await fetch(`/api/tasks/${taskId}`, { method: 'DELETE' });
        
        if (response.ok) {
            loadTasks();
        }
    } catch (error) {
        console.error('Error deleting task:', error);
    }
}

function updateTaskStats(tasks) {
    const completed = tasks.filter(t => t.completed).length;
    const total = tasks.length;
    
    const statsEl = document.getElementById('taskStatsText');
    if (statsEl) {
        statsEl.textContent = `${completed}/${total} tasks completed`;
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}