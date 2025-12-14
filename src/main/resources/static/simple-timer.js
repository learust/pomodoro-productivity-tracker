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
        
        timerState.timerInterval = setInterval(() => {
            if (timerState.remainingSeconds > 0) {
                timerState.remainingSeconds--;
                updateTimerDisplay();
            } else {
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
    timerState.remainingSeconds = timerState.workMinutes * 60;
    updateTimerDisplay();
    console.log('Timer stopped and reset');
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

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Simple timer initialized');
    
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
                const breakInput = document.getElementById('breakDuration');
                if (workInput) workInput.value = timerState.workMinutes;
                if (breakInput) breakInput.value = timerState.breakMinutes;
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
            const breakInput = document.getElementById('breakDuration');
            
            if (workInput && breakInput) {
                const workMins = parseInt(workInput.value) || 25;
                const breakMins = parseInt(breakInput.value) || 5;
                configureTimer(workMins, breakMins);
                
                // Close modal
                const modal = document.getElementById('configModal');
                if (modal) {
                    modal.style.display = 'none';
                }
                
                alert(`Timer configured: ${workMins} min work, ${breakMins} min break`);
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
    
    // Initialize display
    updateTimerDisplay();
});