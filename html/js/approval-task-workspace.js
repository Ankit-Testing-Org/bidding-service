'use strict';

const API_BASE_URL = 'http://localhost:8080/api';
const REQUEST_FIELDS = {
    clarification: 'comment',
    reassignUserId: 'userId',
    reassignComment: 'comment'
};
let state = emptyState();
let previewObjectUrl = null;

function emptyState() {
    return {
        loaded: false,
        search: null,
        taskId: null,
        canAssignToSelf: false,
        canApprove: false,
        alreadyDecided: false,
        preparedDocument: null,
        highlights: [],
        lotHighlights: [],
        history: []
    };
}

function token() {
    return window.keycloak?.token || sessionStorage.getItem('access_token') || localStorage.getItem('access_token') || '';
}

function headers(json = false) {
    const h = {};
    const t = token();
    if (t) h.Authorization = 'Bearer ' + t;
    if (json) h['Content-Type'] = 'application/json';
    return h;
}
async function api(path, options = {}) {
    const response = await fetch(API_BASE_URL + path, {
        ...options,
        headers: {
            ...headers(Boolean(options.body)),
            ...(options.headers || {})
        }
    });
    let payload = null;
    const ct = response.headers.get('content-type') || '';
    if (ct.includes('json')) payload = await response.json();
    if (!response.ok || payload?.success === false) {
        const message = payload?.message || payload?.error || ('Request failed with status ' + response.status);
        throw new Error(message);
    }
    return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload;
}

function text(v, f = 'N/A') {
    return v === null || v === undefined || v === '' ? f : String(v)
}

function esc(v) {
    return text(v, '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#039;')
}

function fmt(v) {
    return text(v, 'Unknown').replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase())
}

function toast(message, error = false) {
    const e = document.getElementById('toast');
    e.textContent = message;
    e.className = 'toast' + (error ? ' error' : '');
    e.style.display = 'block';
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => e.style.display = 'none', 3200)
}

function badge(el, label, color) {
    el.textContent = label;
    el.className = 'badge ' + color
}

function loading(btn, on, label) {
    btn.disabled = on;
    btn.innerHTML = on ? '<span class="spinner"></span> ' + label : label
}
async function loadWorkspace() {
    const search = document.getElementById('searchValue').value.trim();
    if (!search) {
        toast('Contract ID, task ID, or contract number is required', true);
        return
    }
    const b = document.getElementById('searchBtn');
    loading(b, true, 'Searching');
    try {
        const data = await api('/approval-tasks/workspace?search=' + encodeURIComponent(search));
        applyWorkspace(data, search);
        toast('Approval task workspace loaded')
    } catch (e) {
        toast(e.message, true)
    } finally {
        loading(b, false, 'Search')
    }
}

function applyWorkspace(data, search) {
    state = {
        ...emptyState(),
        ...data,
        loaded: true,
        search,
        preparedDocument: data.preparedBidDocument || data.preparedDocument || null,
        highlights: data.highlights || [],
        lotHighlights: data.lotHighlights || [],
        history: data.history || []
    };
    document.getElementById('empty').style.display = 'none';
    document.getElementById('layout').style.display = 'grid';
    document.getElementById('refreshBtn').disabled = false;
    renderAll()
}

function renderAll() {
    badge(document.getElementById('searchBadge'), 'Loaded', 'green');
    document.getElementById('taskStatus').textContent = fmt(state.taskStatus);
    badge(document.getElementById('taskBadge'), fmt(state.taskStatus), statusColor(state.taskStatus));
    document.getElementById('assignedTo').textContent = text(state.assignedTo || state.assignedUserName || state.assignedGroup, 'Unassigned');
    document.getElementById('assignBtn').disabled = !state.canAssignToSelf;
    badge(document.getElementById('assignmentBadge'), state.assignedTo || state.assignedUserName ? 'Assigned' : state.canAssignToSelf ? 'Available' : 'Unassigned', state.assignedTo || state.assignedUserName ? 'green' : state.canAssignToSelf ? 'yellow' : 'gray');
    document.getElementById('canApprove').textContent = state.canApprove ? 'Yes' : 'No';
    badge(document.getElementById('authorityBadge'), state.canApprove ? 'Can decide' : 'Read only', state.canApprove ? 'green' : 'gray');
    document.getElementById('stage').textContent = text(state.stageName, 'None');
    badge(document.getElementById('stageBadge'), text(state.stageName, 'Not loaded'), state.stageName ? 'blue' : 'gray');
    document.getElementById('contextTitle').textContent = [state.contractNumber || state.contractId, state.stageName].filter(Boolean).join(' • ');
    renderHighlights();
    renderLots();
    renderProposal();
    renderHistory();
    renderDocument();
    renderDecision()
}

function emptyMarkup(message) {
    return '<div class="readonly">' + esc(message) + '</div>'
}

function renderHighlights() {
    const e = document.getElementById('highlightGrid');
    e.innerHTML = state.highlights.length ? state.highlights.map(h => '<article class="item"><div class="item-top"><h3>' + esc(h.title || h.name || 'Highlight') + '</h3><span class="badge ' + riskColor(h.riskLevel) + '">' + esc(h.riskLevel || 'Info') + '</span></div><p>' + esc(h.description || h.summary) + '</p><div class="small">' + esc(h.reference || h.sourceReference || '') + '</div></article>').join('') : emptyMarkup('No AI contract highlights are available.')
}

function renderLots() {
    const e = document.getElementById('lotList');
    e.innerHTML = state.lotHighlights.length ? state.lotHighlights.map(l => '<article class="item lot"><div><h3>' + esc(l.lotName || l.name || 'Lot') + '</h3><p>' + esc(l.summary || l.description) + '</p></div><span class="badge ' + (l.qualified === true ? 'green' : l.qualified === false ? 'red' : 'gray') + '">' + (l.qualified === true ? 'Qualified' : l.qualified === false ? 'Disqualified' : 'Pending') + '</span></article>').join('') : emptyMarkup('No lot-level highlights are available.')
}

function renderProposal() {
    const p = state.proposal,
        e = document.getElementById('proposalGrid');
    if (!p) {
        e.innerHTML = emptyMarkup('No proposal summary is available.');
        return
    }
    const pairs = [
        ['Proposal Number', p.proposalNumber],
        ['Proposal Value', p.proposalValue],
        ['Selected Lots', Array.isArray(p.selectedLots) ? p.selectedLots.join(', ') : p.selectedLots],
        ['Submitted By', p.submittedBy],
        ['Submitted At', p.submittedAt],
        ['Proposal Status', p.status]
    ];
    e.innerHTML = pairs.map(([l, v]) => '<article class="item"><div class="small">' + esc(l) + '</div><div class="metric-value">' + esc(text(v)) + '</div></article>').join('')
}

function renderHistory() {
    const e = document.getElementById('historyList');
    e.innerHTML = state.history.length ? state.history.map(h => '<article class="item"><h3>' + esc(h.action || 'Workflow event') + '</h3><p>' + esc(h.message || h.comment) + '</p><div class="small">' + esc(h.performedBy || h.actor || 'System') + ' | ' + esc(h.performedAt || h.createdAt || '') + '</div></article>').join('') : emptyMarkup('No approval history is available.')
}
async function renderDocument() {
    cleanupPreview();
    const d = state.preparedDocument,
        title = document.getElementById('documentTitle'),
        frame = document.getElementById('docFrame'),
        empty = document.getElementById('docEmpty');
    document.getElementById('openBtn').disabled = !d;
    document.getElementById('downloadBtn').disabled = !d;
    if (!d) {
        title.textContent = 'No document loaded';
        frame.style.display = 'none';
        empty.style.display = 'grid';
        return
    }
    title.textContent = text(d.fileName, 'Prepared bid document');
    const url = d.previewUrl || d.documentUrl || d.downloadUrl;
    if (!url) {
        frame.style.display = 'none';
        empty.style.display = 'grid';
        return
    }
    try {
        previewObjectUrl = await authenticatedBlobUrl(url);
        frame.src = previewObjectUrl;
        frame.style.display = 'block';
        empty.style.display = 'none'
    } catch (e) {
        frame.style.display = 'none';
        empty.style.display = 'grid';
        toast('Document preview could not be loaded: ' + e.message, true)
    }
}
async function authenticatedBlobUrl(url) {
    const response = await fetch(resolveUrl(url), {
        headers: headers()
    });
    if (!response.ok) throw new Error('status ' + response.status);
    return URL.createObjectURL(await response.blob())
}

function resolveUrl(url) {
    if (/^https?:\/\//i.test(url)) return url;
    if (url.startsWith('/')) return url;
    return API_BASE_URL + '/' + url.replace(/^\//, '')
}

function cleanupPreview() {
    if (previewObjectUrl) {
        URL.revokeObjectURL(previewObjectUrl);
        previewObjectUrl = null
    }
}

function renderDecision() {
    const allowed = state.canApprove && !state.alreadyDecided;
    document.getElementById('controls').hidden = !allowed;
    document.getElementById('readonly').hidden = allowed;
    badge(document.getElementById('decisionBadge'), state.alreadyDecided ? 'Already decided' : allowed ? 'Action required' : 'Read only', state.alreadyDecided ? 'green' : allowed ? 'yellow' : 'gray');
    document.getElementById('decisionSubtitle').textContent = state.alreadyDecided ? 'The completed task remains available for audit review.' : allowed ? 'This task is assigned to you and requires your decision.' : 'Decision controls are unavailable for your current assignment or authority.';
    if (allowed) loadEligibleUsers()
}
async function assignToMe() {
    if (!state.taskId) return;
    const b = document.getElementById('assignBtn');
    loading(b, true, 'Assigning');
    try {
        const data = await api('/approval-tasks/' + encodeURIComponent(state.taskId) + '/assign-to-me', {
            method: 'POST'
        });
        state = {
            ...state,
            ...data,
            canAssignToSelf: false
        };
        toast(data?.message || 'Approval task assigned successfully');
        await reload()
    } catch (e) {
        toast(e.message, true)
    } finally {
        loading(b, false, 'Assign to me')
    }
}
async function submitDecision(decision) {
    const comment = document.getElementById('comment').value.trim();
    if (!comment) {
        toast('Enter a decision comment', true);
        return
    }
    await action('/decision', {
        decision,
        comment
    }, decision === 'APPROVED' ? 'Task approved' : 'Task rejected')
}
async function requestClarification() {
    const comment = document.getElementById('comment').value.trim();
    if (!comment) {
        toast('Enter the clarification request', true);
        return
    }
    await action('/clarification', {
        [REQUEST_FIELDS.clarification]: comment
    }, 'Clarification requested')
}
async function reassignTask() {
    const userId = document.getElementById('reassignUser').value,
        comment = document.getElementById('comment').value.trim();
    if (!userId) {
        toast('Select an eligible approver', true);
        return
    }
    if (!comment) {
        toast('Enter a reassignment reason', true);
        return
    }
    await action('/reassign', {
        [REQUEST_FIELDS.reassignUserId]: Number(userId),
        [REQUEST_FIELDS.reassignComment]: comment
    }, 'Task reassigned')
}
async function action(suffix, body, success) {
    try {
        await api('/approval-tasks/' + encodeURIComponent(state.taskId) + suffix, {
            method: 'POST',
            body: JSON.stringify(body)
        });
        toast(success);
        await reload()
    } catch (e) {
        toast(e.message, true)
    }
}
async function loadEligibleUsers() {
    const select = document.getElementById('reassignUser');
    select.innerHTML = '<option value="">Loading eligible approvers...</option>';
    try {
        const users = await api('/approval-tasks/' + encodeURIComponent(state.taskId) + '/eligible-users');
        select.innerHTML = '<option value="">Select user</option>' + ((users || []).map(u => '<option value="' + esc(u.userId || u.id) + '">' + esc(u.displayName || u.fullName || u.name || u.email) + '</option>').join(''));
        if (!users?.length) select.innerHTML = '<option value="">No eligible approvers</option>'
    } catch (e) {
        select.innerHTML = '<option value="">Unable to load approvers</option>';
        toast(e.message, true)
    }
}
async function reload() {
    if (!state.search) return;
    try {
        const data = await api('/approval-tasks/workspace?search=' + encodeURIComponent(state.search));
        applyWorkspace(data, state.search)
    } catch (e) {
        toast(e.message, true)
    }
}
async function downloadDocument(open = false) {
    const d = state.preparedDocument;
    if (!d) return;
    const url = d.documentUrl || d.downloadUrl || d.previewUrl;
    try {
        const response = await fetch(resolveUrl(url), {
            headers: headers()
        });
        if (!response.ok) throw new Error('Download failed with status ' + response.status);
        const blobUrl = URL.createObjectURL(await response.blob());
        if (open) window.open(blobUrl, '_blank', 'noopener');
        else {
            const a = document.createElement('a');
            a.href = blobUrl;
            a.download = d.fileName || 'prepared-bid-document';
            document.body.appendChild(a);
            a.click();
            a.remove();
            setTimeout(() => URL.revokeObjectURL(blobUrl), 1000)
        }
    } catch (e) {
        toast(e.message, true)
    }
}

function reset() {
    cleanupPreview();
    state = emptyState();
    document.getElementById('empty').style.display = 'grid';
    document.getElementById('layout').style.display = 'none';
    document.getElementById('refreshBtn').disabled = true;
    document.getElementById('searchValue').value = '';
    badge(document.getElementById('searchBadge'), 'Search required', 'blue');
    document.getElementById('taskStatus').textContent = 'Unknown';
    badge(document.getElementById('taskBadge'), 'Not loaded', 'gray');
    document.getElementById('assignedTo').textContent = 'None';
    document.getElementById('assignBtn').disabled = true;
    badge(document.getElementById('assignmentBadge'), 'Waiting', 'gray');
    document.getElementById('canApprove').textContent = 'No';
    badge(document.getElementById('authorityBadge'), 'Read only', 'gray');
    document.getElementById('stage').textContent = 'None';
    badge(document.getElementById('stageBadge'), 'Not loaded', 'gray')
}

function statusColor(s) {
    s = String(s || '').toUpperCase();
    return s === 'APPROVED' ? 'green' : s === 'REJECTED' ? 'red' : ['PENDING', 'IN_PROGRESS'].includes(s) ? 'yellow' : 'gray'
}

function riskColor(r) {
    r = String(r || '').toLowerCase();
    return r === 'high' ? 'red' : r === 'medium' ? 'yellow' : r === 'low' ? 'green' : 'blue'
}
document.getElementById('searchBtn').addEventListener('click', loadWorkspace);
document.getElementById('searchValue').addEventListener('keydown', e => {
    if (e.key === 'Enter') loadWorkspace()
});
document.getElementById('refreshBtn').addEventListener('click', reload);
document.getElementById('resetBtn').addEventListener('click', reset);
document.getElementById('assignBtn').addEventListener('click', assignToMe);
document.querySelectorAll('[data-action]').forEach(b => b.addEventListener('click', () => submitDecision(b.dataset.action)));
document.getElementById('clarifyBtn').addEventListener('click', requestClarification);
document.getElementById('reassignBtn').addEventListener('click', reassignTask);
document.getElementById('openBtn').addEventListener('click', () => downloadDocument(true));
document.getElementById('downloadBtn').addEventListener('click', () => downloadDocument(false));
document.querySelectorAll('.tab').forEach(b => b.addEventListener('click', () => {
    document.querySelectorAll('.tab,.tab-pane').forEach(e => e.classList.remove('active'));
    b.classList.add('active');
    document.getElementById(b.dataset.tab).classList.add('active')
}));
window.addEventListener('beforeunload', cleanupPreview);
reset();