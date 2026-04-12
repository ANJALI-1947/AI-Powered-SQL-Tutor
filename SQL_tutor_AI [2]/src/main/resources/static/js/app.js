var editor     = document.getElementById('sql-editor');
var statusMsg  = document.getElementById('status-msg');
var rowCount   = document.getElementById('row-count');
var spinner    = document.getElementById('spinner');
var errorBox   = document.getElementById('error-box');
var tableWrap  = document.getElementById('table-wrap');
var resultHead = document.getElementById('result-head');
var resultBody = document.getElementById('result-body');
var placeholder = document.getElementById('placeholder');
var chatMsgs   = document.getElementById('chat-messages');
var chatInput  = document.getElementById('chat-input');
var aiTyping   = document.getElementById('ai-typing');
var currentSql = '';

// ── CSRF ─────────────────────────────────────────────
function csrf() {
    var m = document.querySelector('meta[id="_csrf"]');
    return m ? m.getAttribute('content') : '';
}
function csrfHeader() {
    var m = document.querySelector('meta[id="_csrf_header"]');
    return m ? m.getAttribute('content') : 'X-CSRF-TOKEN';
}
function headers() {
    var h = { 'Content-Type': 'application/json' };
    h[csrfHeader()] = csrf();
    return h;
}

// ── INIT ─────────────────────────────────────────────
window.addEventListener('DOMContentLoaded', function() {
    editor.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 'Enter') { e.preventDefault(); runQuery(); }
        if (e.key === 'Tab') {
            e.preventDefault();
            var s = editor.selectionStart;
            editor.value = editor.value.slice(0, s) + '  ' + editor.value.slice(editor.selectionEnd);
            editor.selectionStart = editor.selectionEnd = s + 2;
        }
    });
    if (chatMsgs) chatMsgs.scrollTop = chatMsgs.scrollHeight;
});

// ── RUN QUERY ─────────────────────────────────────────
function runQuery() {
    var sql = editor.value.trim();
    if (!sql) { setStatus('⚠️ Enter a SQL query.', 'warn'); return; }
    currentSql = sql;
    showState('loading');
    setStatus('Executing...', 'muted');

    fetch('/api/query', { method: 'POST', headers: headers(), body: JSON.stringify({ sql: sql }) })
    .then(function(r) {
        if (r.status === 401 || r.status === 403) {
            window.location.href = '/login?expired=true';
            return null;
        }
        // ✅ FIX: Always parse JSON regardless of status code
        return r.json();
    })
    .then(function(data) {
        if (!data) return;
        if (data.success) {
            // ✅ FIX: Check for SELECT results (columns present) vs UPDATE results
            if (data.columns && data.columns.length > 0) {
                // SELECT query — render table even if 0 rows
                renderTable(data.columns, data.rows || []);
                var rowLen = (data.rows || []).length;
                rowCount.textContent = rowLen + ' row(s) · ' + data.executionTimeMs + 'ms';
                setStatus('✅ ' + data.message, 'ok');
            } else {
                // UPDATE/INSERT — no result set
                showState('empty');
                rowCount.textContent = data.executionTimeMs + 'ms';
                setStatus('✅ ' + data.message, 'ok');
            }
        } else {
            showState('error', data.message);
            setStatus('❌ Query failed', 'error');
        }
    })
    .catch(function(e) {
        showState('error', 'Network error: ' + e.message);
        setStatus('❌ Error', 'error');
    });
}

// ── RENDER TABLE ──────────────────────────────────────
function renderTable(cols, rows) {
    resultHead.innerHTML = '';
    resultBody.innerHTML = '';

    // Header row
    var hr = document.createElement('tr');
    cols.forEach(function(c) {
        var th = document.createElement('th');
        th.textContent = c;
        hr.appendChild(th);
    });
    resultHead.appendChild(hr);

    // ✅ FIX: Show "no rows" message inside table when result is empty
    if (!rows || rows.length === 0) {
        var tr = document.createElement('tr');
        var td = document.createElement('td');
        td.colSpan = cols.length;
        td.textContent = 'No rows returned';
        td.style.textAlign = 'center';
        td.style.opacity = '0.6';
        tr.appendChild(td);
        resultBody.appendChild(tr);
    } else {
        // Data rows — rows is array of arrays
        rows.forEach(function(row) {
            var tr = document.createElement('tr');
            row.forEach(function(cell) {
                var td = document.createElement('td');
                if (cell === null || cell === 'NULL') {
                    td.textContent = 'NULL';
                    td.className = 'cell-null';
                } else if (cell !== '' && !isNaN(cell)) {
                    td.textContent = cell;
                    td.className = 'cell-num';
                } else {
                    td.textContent = cell;
                }
                tr.appendChild(td);
            });
            resultBody.appendChild(tr);
        });
    }

    showState('table');
}

// ── UI STATE ──────────────────────────────────────────
function showState(state, msg) {
    placeholder.hidden = true;
    spinner.hidden     = true;
    errorBox.hidden    = true;
    tableWrap.hidden   = true;
    if      (state === 'loading') spinner.hidden = false;
    else if (state === 'error')   { errorBox.textContent = '⚠️ ' + (msg || ''); errorBox.hidden = false; }
    else if (state === 'table')   tableWrap.hidden = false;
    else                          placeholder.hidden = false;
}

function setStatus(text, type) {
    var colors = { ok: '#a6e3a1', error: '#f38ba8', warn: '#f9e2af', muted: '#6c7086' };
    statusMsg.textContent = text;
    statusMsg.style.color = colors[type] || colors.muted;
}

function clearAll()  {
    editor.value = '';
    showState('empty');
    rowCount.textContent = '';
    setStatus('', 'muted');
    editor.focus();
}

function setQuery(sql) { editor.value = sql; editor.focus(); }

function toggleCols(el) {
    var c = el.nextElementSibling;
    var open = c.classList.toggle('open');
    el.querySelector('span').textContent = open ? '▼' : '▶';
}

function formatSQL() {
    var kws = ['SELECT','FROM','WHERE','JOIN','LEFT JOIN','RIGHT JOIN','INNER JOIN',
               'ON','GROUP BY','ORDER BY','HAVING','LIMIT','INSERT INTO','VALUES',
               'UPDATE','SET','DELETE'];
    var sql = editor.value.trim();
    kws.forEach(function(kw) {
        var re = new RegExp('\\b' + kw + '\\b', 'gi');
        sql = sql.replace(re, '\n' + kw.toUpperCase());
    });
    editor.value = sql.replace(/^\n/, '').replace(/,\s*/g, ', ');
}

// ── AI CHAT ───────────────────────────────────────────
function sendChat() {
    var msg = chatInput.value.trim();
    if (!msg) return;
    chatInput.value = '';
    appendChatMsg('user', 'You', msg);
    aiTyping.hidden = false;
    chatMsgs.scrollTop = chatMsgs.scrollHeight;

    fetch('/api/chat', {
        method: 'POST',
        headers: headers(),
        body: JSON.stringify({ message: msg, currentSql: currentSql })
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        aiTyping.hidden = true;
        if (data.response) appendChatMsg('ai', '🤖 AI Tutor', data.response);
        else if (data.error) appendChatMsg('ai', '❌ Error', data.error);
        else appendChatMsg('ai', '❌ Error', 'Unexpected response from AI.');
    })
    .catch(function(e) {
        aiTyping.hidden = true;
        appendChatMsg('ai', '❌ Error', 'Network error: ' + e.message);
    });
}

function quickAsk(q)  { chatInput.value = q; sendChat(); }

function askAIAboutQuery() {
    var sql = editor.value.trim();
    chatInput.value = sql
        ? 'Please review this SQL query and explain what it does:\n' + sql
        : 'How do I write a basic SELECT query?';
    sendChat();
}

function appendChatMsg(role, sender, text) {
    var div     = document.createElement('div');
    div.className = 'chat-msg ' + (role === 'user' ? 'msg-user' : 'msg-ai');
    var roleDiv = document.createElement('div');
    roleDiv.className = 'msg-role';
    roleDiv.textContent = sender;
    var textDiv = document.createElement('div');
    textDiv.className = 'msg-text';
    textDiv.textContent = text;
    var timeDiv = document.createElement('div');
    timeDiv.className = 'msg-time';
    timeDiv.textContent = new Date().toLocaleTimeString();
    div.appendChild(roleDiv);
    div.appendChild(textDiv);
    div.appendChild(timeDiv);
    chatMsgs.appendChild(div);
    chatMsgs.scrollTop = chatMsgs.scrollHeight;
}

function clearChat() {
    if (!confirm('Clear all chat history?')) return;
    fetch('/api/chat/clear', {
        method: 'DELETE',
        headers: { [csrfHeader()]: csrf() }
    })
    .then(function() {
        chatMsgs.innerHTML = '<div class="chat-welcome"><p>Chat cleared. Ask me anything!</p></div>';
    })
    .catch(function(e) { alert('Error: ' + e.message); });
}
