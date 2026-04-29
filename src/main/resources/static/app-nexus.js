let trendsChart, riskChart, volumeChart;
let lastAuditData = [];
let lastAlertData = [];
let simulatorInterval = null;

let lastAlertHash = "";
let lastAuditHash = "";

document.addEventListener('DOMContentLoaded', async () => {
    const user = await checkAuth();
    if (!user) {
        window.location.href = '/landing.html';
        return;
    }

    initCharts();
    setupUI(user);
    refreshAllData();

    const singleTxnForm = document.getElementById('single-txn-form');
    if (singleTxnForm) {
        singleTxnForm.addEventListener('submit', handleSingleTransaction);
    }
    
    const csvForm = document.getElementById('csv-form');
    if (csvForm) {
        csvForm.addEventListener('submit', handleCsvUpload);
    }
    
    const refreshBtn = document.getElementById('refresh-data');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', refreshAllData);
    }
    
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
    
    const searchInput = document.getElementById('table-search');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }

    const typeSelect = document.getElementById('txn-type');
    if (typeSelect) {
        typeSelect.addEventListener('change', handleTxnTypeChange);
        // Trigger initial state
        handleTxnTypeChange({target: typeSelect});
    }
    
    setInterval(refreshAllData, 5000);
});

async function checkAuth() {
    try {
        const response = await fetch('/api/auth/me');
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('user', JSON.stringify(data));
            return data;
        }
    } catch (e) {}
    return null;
}

function setupUI(user) {
    const nameEl = document.getElementById('user-name');
    if (nameEl) nameEl.textContent = user.fullName;
    
    const roleEl = document.getElementById('user-role');
    if (roleEl) roleEl.textContent = user.role === 'ADMIN' ? 'System Administrator' : 'Security Analyst';
    
    if (user.role === 'CUSTOMER') {
        const sourceEl = document.getElementById('txn-source');
        if(sourceEl) sourceEl.value = user.accountId;
        const batchEl = document.getElementById('batch-section');
        if(batchEl) batchEl.classList.add('d-none');
    } else if (user.role === 'ADMIN') {
        document.querySelectorAll('.simulator-btn').forEach(b => b.classList.remove('d-none'));
    }
}

function showToast(msg, isError = false) {
    const toastEl = document.getElementById('liveToast');
    const toastMsg = document.getElementById('toast-msg');
    if(!toastEl || !toastMsg) return;
    
    toastEl.style.borderColor = isError ? 'var(--danger)' : 'var(--brand)';
    toastMsg.textContent = msg;
    
    toastEl.style.display = 'block';
    setTimeout(() => {
        toastEl.style.display = 'none';
    }, 4000);
}

async function handleLogout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    localStorage.removeItem('user');
    window.location.href = '/landing.html';
}

function renderAlerts(alerts) {
    const tbody = document.getElementById('alerts-table-body');
    if (!tbody) return;
    if (alerts && alerts.length > 0) {
        tbody.innerHTML = alerts.map(alert => `
            <tr class="txn-row">
                <td class="mono" style="color: var(--navy); font-weight: 700;">${alert.transactionId}</td>
                <td><span class="risk-indicator ${alert.riskLevel === 'HIGH' ? 'risk-high' : 'risk-mid'}">${alert.riskLevel}</span></td>
                <td style="font-weight: 700; color: ${alert.riskScore > 80 ? 'var(--red)' : 'var(--amber)'};">${alert.riskScore}/100</td>
                <td style="font-size: 12px; color: var(--muted);">${alert.triggeredRules}</td>
                <td style="font-size: 12px; color: var(--muted);">${formatDate(alert.createdAt)}</td>
                <td>
                    <button class="btn-outline" style="padding: 4px 10px; font-size: 11px;" onclick='openXAIModal(${JSON.stringify(alert).replace(/'/g, "&apos;")})'>
                        Details →
                    </button>
                </td>
            </tr>
        `).join('');
    } else {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 40px; color: var(--muted);">No fraud detected.</td></tr>';
    }
}

function renderAudits(audits, alertMap) {
    const tbody = document.getElementById('audits-table-body');
    if (!tbody) return;
    if (audits && audits.length > 0) {
        tbody.innerHTML = audits.map(audit => {
            const alert = alertMap ? alertMap.get(audit.transactionId) : null;
            const riskClass = audit.flagged ? 'risk-high' : (audit.riskScore > 40 ? 'risk-mid' : 'risk-low');
            const rules = alert && alert.triggeredRules ? alert.triggeredRules : '—';
            
            return `
            <tr class="txn-row" onclick='handleRowClick(${JSON.stringify(audit).replace(/'/g, "&apos;")}, ${alert ? JSON.stringify(alert).replace(/'/g, "&apos;") : "null"})'>
                <td class="mono" style="color: var(--brand); font-weight: 700;">${audit.transactionId}</td>
                <td>
                    <div style="font-weight: 700;">₹${parseFloat(audit.amount).toLocaleString()}</div>
                    <div style="font-size: 10px; color: var(--text-muted);">${audit.transactionType}</div>
                </td>
                <td>
                    <span class="risk-indicator ${riskClass}">
                        ${audit.flagged ? 'BLOCK' : 'PASS'} [${audit.riskScore}]
                    </span>
                </td>
                <td style="font-size: 11px; color: var(--text-muted); max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${rules}</td>
                <td class="mono" style="font-size: 11px; color: var(--text-muted);">${formatDate(audit.transactionTime)}</td>
            </tr>
            `;
        }).join('');
    } else {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 40px; color: var(--muted);">Awaiting transactions.</td></tr>';
    }
}

async function refreshAllData() {
    try {
        const [alerts, audits] = await Promise.all([
            fetch('/api/fraud/alerts').then(r => r.json()),
            fetch('/api/fraud/audits').then(r => r.json())
        ]);

        const currentAlertHash = alerts.length > 0 ? (alerts[0].transactionId + alerts[0].createdAt) : "empty";
        const currentAuditHash = audits.length > 0 ? (audits[0].transactionId + audits[0].transactionTime) : "empty";

        if (currentAuditHash !== lastAuditHash || currentAlertHash !== lastAlertHash) {
            lastAlertData = alerts;
            lastAuditData = audits;
            
            const alertMap = new Map(alerts.map(a => [a.transactionId, a]));
            renderAudits(audits.slice(0, 50), alertMap);
            
            lastAuditHash = currentAuditHash;
            lastAlertHash = currentAlertHash;
        }

        updateTicker(alerts, audits);
        updateCharts(alerts, audits);
    } catch (e) {
        console.error("Data refresh failed", e);
    }
}

function openXAIModal(alert) {
    const rules = alert.triggeredRules ? alert.triggeredRules.split(',') : [];
    
    let rulesHtml = rules.map(rule => `
        <div style="background: rgba(255,255,255,0.03); border-left: 2px solid var(--brand); padding: 15px; margin-bottom: 12px; border-radius: 4px;">
            <div style="font-weight: 800; color: var(--text); font-size: 12px; text-transform: uppercase;">${rule.trim()}</div>
            <div style="font-size: 12px; color: var(--text-muted); margin-top: 5px;">Node behavioral anomaly detected based on historical velocity and payload variance.</div>
        </div>
    `).join('');

    document.getElementById('xai-txn-id').textContent = alert.transactionId;
    const modalBody = document.getElementById('xai-modal-body');
    modalBody.innerHTML = `
        <div style="display: grid; grid-template-columns: 350px 1fr; gap: 40px;">
            <div style="background: var(--bg); padding: 25px; border-radius: 8px; border: 1px solid var(--border);">
                <div style="margin-bottom: 20px;">
                    <div style="font-size: 10px; font-weight: 800; color: var(--text-muted); margin-bottom: 5px;">SECURITY POSTURE</div>
                    <div style="font-size: 24px; font-weight: 800; color: ${alert.riskScore > 70 ? 'var(--danger)' : 'var(--warning)'}; font-family: var(--font-mono);">${alert.riskScore}/100</div>
                </div>
                
                <div style="display: flex; flex-direction: column; gap: 12px;">
                    <div style="display:flex; justify-content:space-between; font-size: 12px; border-bottom: 1px solid var(--border); padding-bottom: 6px;">
                        <span style="color:var(--text-muted);">Risk Category</span>
                        <span style="font-weight:700;">${alert.riskLevel}</span>
                    </div>
                    <div style="display:flex; justify-content:space-between; font-size: 12px; border-bottom: 1px solid var(--border); padding-bottom: 6px;">
                        <span style="color:var(--text-muted);">Ingestion Time</span>
                        <span class="mono">${formatDate(alert.createdAt)}</span>
                    </div>
                </div>

                <div style="margin-top: 30px; padding: 15px; background: rgba(79, 70, 229, 0.1); border: 1px solid var(--brand); border-radius: 6px;">
                    <div style="font-weight: 800; color: var(--brand); font-size: 11px; margin-bottom: 8px;">NEXUS VERDICT</div>
                    <p style="font-size: 12px; color: var(--text); line-height: 1.6;">Connection severed. The transaction parameters align with established fraud vectors. Human intervention is required.</p>
                </div>
            </div>
            
            <div>
                <div style="font-size: 10px; font-weight: 800; color: var(--text-muted); margin-bottom: 15px; letter-spacing: 1px;">HEURISTIC ANALYSIS</div>
                ${rulesHtml || '<div style="color: var(--text-muted); font-size: 13px;">No suspicious heuristics triggered.</div>'}
            </div>
        </div>
    `;
    
    document.getElementById('xaiModal').style.display = 'flex';
}

function updateTicker(alerts, audits) {
    const total = audits.length;
    const flagged = alerts.length;
    const rate = total > 0 ? Math.round(((total - flagged) / total) * 100) : 100;
    let totalVolume = audits.reduce((sum, a) => sum + parseFloat(a.amount), 0);

    const statTotal = document.getElementById('stat-total');
    if(statTotal) statTotal.textContent = total;
    
    const statFlagged = document.getElementById('stat-flagged');
    if(statFlagged) statFlagged.textContent = flagged;
    
    const statRate = document.getElementById('stat-rate');
    if(statRate) statRate.textContent = rate + '%';
    
    const statVolume = document.getElementById('stat-volume');
    if(statVolume) statVolume.textContent = '₹' + (totalVolume > 1000000 ? (totalVolume/1000000).toFixed(2) + 'M' : Math.round(totalVolume).toLocaleString());
}

function handleTxnTypeChange(e) {
    const type = e.target.value;
    const sourceContainer = document.getElementById('txn-source').parentElement;
    const destContainer = document.getElementById('txn-dest').parentElement;
    const sourceInput = document.getElementById('txn-source');
    const destInput = document.getElementById('txn-dest');
    
    if (type === 'DEPOSIT') {
        sourceContainer.classList.add('d-none');
        sourceInput.required = false;
        destContainer.classList.remove('d-none');
        destInput.required = true;
    } else if (type === 'WITHDRAWAL') {
        sourceContainer.classList.remove('d-none');
        sourceInput.required = true;
        destContainer.classList.add('d-none');
        destInput.required = false;
    } else {
        sourceContainer.classList.remove('d-none');
        sourceInput.required = true;
        destContainer.classList.remove('d-none');
        destInput.required = true;
    }
}

function handleSearch(e) {
    const term = e.target.value.toLowerCase();
    const rows = document.querySelectorAll('.txn-row');
    rows.forEach(row => {
        row.style.display = row.textContent.toLowerCase().includes(term) ? '' : 'none';
    });
}

async function handleSingleTransaction(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    if(btn) btn.disabled = true;
    
    // Add destination account!
    const destInput = document.getElementById('txn-dest');
    
    const type = document.getElementById('txn-type').value;
    const payload = {
        transactionId: document.getElementById('txn-id').value || 'TXN-' + Math.floor(Math.random()*10000),
        transactionType: type,
        amount: parseFloat(document.getElementById('txn-amount').value),
        transactionTime: new Date().toISOString().split('.')[0],
        sourceAccountId: type === 'DEPOSIT' ? null : (document.getElementById('txn-source').value || null),
        destinationAccountId: type === 'WITHDRAWAL' ? null : (destInput ? (destInput.value || null) : null)
    };

    try {
        const response = await fetch('/api/fraud/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        
        if (response.ok) {
            showResult(data);
            showToast(data.flagged ? "SECURITY BREACH DETECTED" : "TRANSACTION VERIFIED", data.flagged);
            refreshAllData();
        } else {
            // Display actual error message from GlobalExceptionHandler (e.g. Insufficient balance)
            const errorMsg = data.message || 'Verification Failed';
            alert(`Transaction Error: ${errorMsg}`);
            showToast(`Error: ${errorMsg}`, true);
        }
    } catch (err) {
        alert('Nexus Link Error: The server is unreachable.');
        showToast('Nexus Link Error', true);
    } finally {
        if(btn) btn.disabled = false;
    }
}

function showResult(data) {
    const resultDiv = document.getElementById('single-result');
    if (!resultDiv) return;
    
    const color = data.flagged ? 'var(--danger)' : 'var(--success)';
    const bgColor = data.flagged ? 'rgba(239, 68, 68, 0.1)' : 'rgba(16, 185, 129, 0.1)';
    
    resultDiv.innerHTML = `
        <div style="background:${bgColor}; border: 1px solid ${color}; border-radius: 8px; padding: 15px; animation: slideIn 0.3s ease;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 8px;">
                <span style="font-weight:800; font-size:11px; color:${color}; letter-spacing:1px; text-transform: uppercase;">
                    ${data.flagged ? 'BLOCK' : 'PASS'}
                </span>
                <span class="mono" style="font-weight:700; color:white;">SCORE: ${data.riskScore}</span>
            </div>
            <div style="font-size: 12px; color: var(--text-muted);">
                Verdict generated by Nexus heuristic engine.
            </div>
        </div>
    `;
}

// Ensure slideIn animation exists
if (!document.getElementById('anim-style')) {
    const style = document.createElement('style');
    style.id = 'anim-style';
    style.innerHTML = `
    @keyframes slideIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    `;
    document.head.appendChild(style);
}

async function handleCsvUpload(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    if(btn) btn.disabled = true;
    const formData = new FormData();
    formData.append('file', document.getElementById('csv-file').files[0]);

    try {
        const response = await fetch('/api/fraud/analyze-csv', { method: 'POST', body: formData });
        if (response.ok) {
            const data = await response.json();
            showToast(`Ingestion Complete: ${data.flaggedTransactions} flagged.`, data.flaggedTransactions > 0);
            refreshAllData();
        } else {
            const data = await response.json();
            showToast(`Error: ${data.message || 'Batch upload failed'}`, true);
        }
    } catch (err) {
        showToast('Batch processing failed', true);
    } finally {
        if(btn) btn.disabled = false;
    }
}

function initCharts() {
    const trendsCtx = document.getElementById('trendsChart');
    if(trendsCtx) {
        trendsChart = new Chart(trendsCtx.getContext('2d'), {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    data: [],
                    borderColor: '#4f46e5',
                    tension: 0.4,
                    fill: true,
                    backgroundColor: 'rgba(79, 70, 229, 0.05)',
                    borderWidth: 2,
                    pointRadius: 0
                }]
            },
            options: { 
                responsive: true, 
                maintainAspectRatio: false,
                plugins: { legend: { display: false } }, 
                scales: { 
                    y: { display: false, beginAtZero: true, max: 100 },
                    x: { display: false } 
                } 
            }
        });
    }

    const riskCtx = document.getElementById('riskChart');
    if(riskCtx) {
        riskChart = new Chart(riskCtx.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Low', 'Mid', 'High'],
                datasets: [{
                    data: [0, 0, 0],
                    backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
                    borderWidth: 0
                }]
            },
            options: { 
                responsive: true,
                maintainAspectRatio: false,
                cutout: '80%', 
                plugins: { legend: { display: false } } 
            }
        });
    }

    const volumeCtx = document.getElementById('volumeChart');
    if(volumeCtx) {
        volumeChart = new Chart(volumeCtx.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['DEPOSIT', 'WITHDRAWAL', 'TRANSFER'],
                datasets: [{
                    data: [0, 0, 0],
                    backgroundColor: ['rgba(59, 130, 246, 0.8)', 'rgba(236, 72, 153, 0.8)', 'rgba(168, 85, 247, 0.8)'],
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { display: false, beginAtZero: true },
                    x: {
                        ticks: { color: 'rgba(255,255,255,0.5)', font: { size: 10 } },
                        grid: { display: false }
                    }
                }
            }
        });
    }
}

function updateCharts(alerts, audits) {
    if(!trendsChart || !riskChart) return;
    
    const scoreTrend = audits.slice(0, 20).reverse().map(a => a.riskScore);
    const riskCounts = audits.reduce((acc, a) => {
        if(acc[a.riskLevel] !== undefined) acc[a.riskLevel]++;
        return acc;
    }, { LOW: 0, MEDIUM: 0, HIGH: 0 });

    const typeCounts = audits.reduce((acc, a) => {
        if(acc[a.transactionType] !== undefined) acc[a.transactionType]++;
        return acc;
    }, { DEPOSIT: 0, WITHDRAWAL: 0, TRANSFER: 0 });

    trendsChart.data.labels = new Array(scoreTrend.length).fill('');
    trendsChart.data.datasets[0].data = scoreTrend;
    trendsChart.update();

    riskChart.data.datasets[0].data = [riskCounts.LOW, riskCounts.MEDIUM, riskCounts.HIGH];
    riskChart.update();

    if(volumeChart) {
        volumeChart.data.datasets[0].data = [typeCounts.DEPOSIT, typeCounts.WITHDRAWAL, typeCounts.TRANSFER];
        volumeChart.update();
    }
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

function toggleSimulator() {
    const btns = document.querySelectorAll('.simulator-btn');
    const texts = document.querySelectorAll('.simulator-text');
    if (simulatorInterval) {
        clearInterval(simulatorInterval);
        simulatorInterval = null;
        texts.forEach(t => t.textContent = 'START LIVE FEED SIMULATION');
        btns.forEach(b => {
            b.style.background = 'transparent';
            b.style.color = 'var(--danger)';
            b.style.borderColor = 'var(--danger)';
        });
        showToast("Simulation Terminated.", false);
    } else {
        texts.forEach(t => t.textContent = 'STOPPING FEED...');
        btns.forEach(b => {
            b.style.background = 'var(--danger)';
            b.style.color = 'white';
        });
        showToast("Live Traffic Feed Initiated.", false);
        simulatorInterval = setInterval(runSimulatedTransaction, 3000);
        runSimulatedTransaction();
    }
}

async function runSimulatedTransaction() {
    const isFraud = Math.random() > 0.7;
    const accIds = ['ACC1001', 'ACC1006', 'ACC1025', 'ACC1042', 'ACC1088'];
    const types = ['TRANSFER', 'WITHDRAWAL', 'DEPOSIT'];
    const type = types[Math.floor(Math.random() * types.length)];
    const payload = {
        transactionId: 'SIM-' + Math.floor(Math.random() * 100000),
        transactionType: type,
        amount: parseFloat((isFraud ? (Math.random()*500000+50000) : (Math.random()*5000+100)).toFixed(2)),
        transactionTime: new Date().toISOString().split('.')[0],
        sourceAccountId: type === 'DEPOSIT' ? null : accIds[Math.floor(Math.random() * accIds.length)],
        destinationAccountId: type === 'WITHDRAWAL' ? null : accIds[Math.floor(Math.random() * accIds.length)]
    };
    try {
        await fetch('/api/fraud/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        refreshAllData();
    } catch (e) {}
}

function handleRowClick(audit, alert) {
    if (alert) {
        openXAIModal(alert);
    } else {
        document.getElementById('txn-id').value = audit.transactionId;
        const typeSelect = document.getElementById('txn-type');
        if (typeSelect) {
            typeSelect.value = audit.transactionType;
            handleTxnTypeChange({target: typeSelect});
        }
        document.getElementById('txn-amount').value = audit.amount;
        
        const sourceInput = document.getElementById('txn-source');
        const destInput = document.getElementById('txn-dest');
        if (sourceInput && audit.sourceAccountId) sourceInput.value = audit.sourceAccountId;
        if (destInput && audit.destinationAccountId) destInput.value = audit.destinationAccountId;
        
        // Scroll to the form
        document.getElementById('analysis-section').scrollIntoView({behavior: 'smooth'});
    }
}

