/* =============================================
   EcoPickup · app.js
   Green × Gold Premium Theme
   ============================================= */

'use strict';

/* ── Local "database" ────────────────────────────────────────── */
const DB_KEY = 'ecopickup_requests';

function getRequests() {
  try { return JSON.parse(localStorage.getItem(DB_KEY)) || []; }
  catch { return []; }
}

function saveRequest(req) {
  const list = getRequests();
  list.push(req);
  localStorage.setItem(DB_KEY, JSON.stringify(list));
}

function findRequest(id) {
  return getRequests().find(r => r.id.toUpperCase() === id.toUpperCase().trim()) || null;
}

function generateId() {
  const n = 1000 + getRequests().length + 1;
  return 'REQ' + n;
}

/* ── Seed demo data if empty ─────────────────────────────────── */
(function seedDemo() {
  if (getRequests().length === 0) {
    saveRequest({
      id: 'REQ1000',
      name: 'Arjun Nair',
      phone: '9876543210',
      address: '12 MG Road, Kochi',
      wasteType: 'Food',
      pickupDate: '2025-03-20',
      status: 'Done',
      submittedAt: new Date().toISOString()
    });
    saveRequest({
      id: 'REQ1001',
      name: 'Priya Menon',
      phone: '9123456780',
      address: '45 Gandhi Nagar, Thrissur',
      wasteType: 'Plastic',
      pickupDate: '2025-03-28',
      status: 'Pending',
      submittedAt: new Date().toISOString()
    });
  }
})();

/* ═══════════════════════════════════════════════════════════════
   REQUEST PAGE — Multi-step form
═══════════════════════════════════════════════════════════════ */

let currentStep = 1;

window.goStep = function (step) {
  // Validate before moving forward
  if (step > currentStep) {
    if (!validateStep(currentStep)) return;
  }

  // Hide current, show new
  document.querySelectorAll('.form-step').forEach(s => s.classList.remove('active'));
  const next = document.getElementById('formStep' + step);
  if (next) next.classList.add('active');

  // Update progress dots
  for (let i = 1; i <= 3; i++) {
    const dot = document.getElementById('step' + i + '-dot');
    if (dot) {
      dot.classList.toggle('active', i === step);
    }
  }

  // Update connector lines
  for (let i = 1; i <= 2; i++) {
    const line = document.getElementById('line' + i);
    if (line) {
      line.classList.toggle('done', step > i);
    }
  }

  // Populate summary on step 3
  if (step === 3) populateSummary();

  currentStep = step;
  // Scroll to top of form area
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

function validateStep(step) {
  if (step === 1) {
    const name    = val('name');
    const phone   = val('phone');
    const address = val('address');
    let ok = true;

    clearError('nameError');
    clearError('phoneError');
    clearError('addressError');

    if (!name) { setError('nameError', 'Full name is required'); ok = false; }
    if (!/^\d{10}$/.test(phone)) { setError('phoneError', 'Enter a valid 10-digit phone number'); ok = false; }
    if (!address) { setError('addressError', 'Address is required'); ok = false; }
    return ok;
  }

  if (step === 2) {
    const wt = val('wasteType');
    clearError('wasteError');
    if (!wt) { setError('wasteError', 'Please select a waste type'); return false; }
    return true;
  }

  return true;
}

window.validateForm = function () {
  clearError('dateError');
  const date = val('pickupDate');
  if (!date) { setError('dateError', 'Please select a pickup date'); return false; }

  const sel = new Date(date);
  const today = new Date(); today.setHours(0,0,0,0);
  if (sel < today) { setError('dateError', 'Date cannot be in the past'); return false; }

  // Save to local DB
  let session = JSON.parse(localStorage.getItem('eco_session') || 'null');
  let uId = session && session.id ? session.id : 'GUEST';
  const req = {
    id: generateId(),
    userId:     uId,
    name:       val('name'),
    phone:      val('phone'),
    address:    val('address'),
    wasteType:  val('wasteType'),
    pickupDate: date,
    status:     'Pending',
    submittedAt: new Date().toISOString()
  };
  saveRequest(req);

  // Show success
  document.getElementById('requestForm').style.display = 'none';
  document.querySelectorAll('.progress-steps').forEach(el => el.style.display = 'none');
  const screen = document.getElementById('successScreen');
  screen.classList.add('show');
  document.getElementById('generatedId').textContent = req.id;

  return false; // prevent real submit
};

window.selectWaste = function (el, type) {
  document.querySelectorAll('.waste-option').forEach(o => o.classList.remove('selected'));
  el.classList.add('selected');
  const inp = document.getElementById('wasteType');
  if (inp) inp.value = type;
  clearError('wasteError');
};

function populateSummary() {
  setText('sumName',  val('name')       || '–');
  setText('sumPhone', val('phone')      || '–');
  setText('sumWaste', val('wasteType')  || '–');
  const d = val('pickupDate');
  setText('sumDate',  d ? formatDate(d) : '–');
}

/* ═══════════════════════════════════════════════════════════════
   STATUS PAGE — Track request
═══════════════════════════════════════════════════════════════ */

window.validateStatus = function () {
  clearError('idError');
  const id = val('requestId');
  if (!id) { setError('idError', 'Please enter a Request ID'); return false; }
  if (!/^REQ\d+$/i.test(id.trim())) { setError('idError', 'Format should be REQ followed by numbers (e.g. REQ1001)'); return false; }

  const req = findRequest(id);
  const container = document.getElementById('resultContainer');
  if (!container) return false;

  if (!req) {
    container.innerHTML = `
      <div class="status-result-card" style="text-align:center;padding:32px;">
        <div style="font-size:60px;margin-bottom:12px;">❌</div>
        <h3 style="font-family:'Playfair Display',serif;color:#064e35;margin-bottom:8px;">Not Found</h3>
        <p style="color:#5a7a65;font-size:14px;">No request found for <strong>${id.trim().toUpperCase()}</strong>.<br/>Check the ID and try again.</p>
      </div>`;
    return false;
  }

  const isPending    = req.status === 'Pending';
  const isInProgress = req.status === 'In Progress';
  const isDone       = req.status === 'Done';
  const statusColor  = isPending ? '#d4af37' : isInProgress ? '#3b82f6' : '#10b981';
  const statusBg     = isPending ? 'linear-gradient(135deg,#fef9e7,#fef3c7)'
                     : isInProgress ? 'linear-gradient(135deg,#eff6ff,#dbeafe)'
                     : 'linear-gradient(135deg,#f0fdf4,#dcfce7)';
  const statusBorder = isPending ? 'rgba(212,175,55,0.35)' : isInProgress ? 'rgba(59,130,246,0.3)' : 'rgba(16,185,129,0.3)';
  const statusIcon   = isPending ? '⏳' : isInProgress ? '🚛' : '✅';

  const timeline = buildTimeline(req);

  container.innerHTML = `
    <div class="status-result-card">
      <!-- Header -->
      <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:20px;">
        <div>
          <div style="font-size:11px;font-weight:800;color:#5a7a65;text-transform:uppercase;letter-spacing:1px;margin-bottom:4px;">Request ID</div>
          <div style="font-family:'Playfair Display',serif;font-size:22px;font-weight:700;color:#064e35;">${req.id}</div>
        </div>
        <div style="background:${statusBg};border:1.5px solid ${statusBorder};border-radius:12px;padding:8px 14px;text-align:center;">
          <div style="font-size:22px;">${statusIcon}</div>
          <div style="font-size:11px;font-weight:800;color:${statusColor};text-transform:uppercase;letter-spacing:0.5px;">${req.status}</div>
        </div>
      </div>

      <!-- Details Grid -->
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:20px;">
        ${detailCell('👤','Name', req.name)}
        ${detailCell('📞','Phone', req.phone)}
        ${detailCell('♻️','Waste', req.wasteType)}
        ${detailCell('📅','Pickup Date', formatDate(req.pickupDate))}
      </div>

      <!-- Address -->
      <div style="background:#f6fef9;border:1px solid rgba(16,185,129,0.15);border-radius:12px;padding:14px;margin-bottom:20px;">
        <div style="font-size:11px;font-weight:800;color:#5a7a65;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;">📍 Address</div>
        <div style="font-size:14px;font-weight:700;color:#0c2116;">${req.address}</div>
      </div>

      <!-- Timeline -->
      ${timeline}
    </div>`;

  container.scrollIntoView({ behavior: 'smooth', block: 'start' });
  return false;
};

function detailCell(icon, label, value) {
  return `
    <div style="background:#f6fef9;border:1px solid rgba(16,185,129,0.12);border-radius:12px;padding:12px;">
      <div style="font-size:10px;font-weight:800;color:#5a7a65;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;">${icon} ${label}</div>
      <div style="font-size:13px;font-weight:800;color:#0c2116;">${value}</div>
    </div>`;
}

function buildTimeline(req) {
  const isPending    = req.status === 'Pending';
  const isInProgress = req.status === 'In Progress';
  const isDone       = req.status === 'Done';
  const steps = [
    { icon: '📝', label: 'Request Submitted',    done: true },
    { icon: '✅', label: 'Request Confirmed',     done: true },
    { icon: '🚛', label: 'Driver Assigned',       done: isInProgress || isDone },
    { icon: '♻️', label: 'Pickup Completed',      done: isDone },
  ];

  const items = steps.map((s, i) => `
    <div style="display:flex;align-items:flex-start;gap:12px;${i < steps.length - 1 ? 'margin-bottom:16px;' : ''}">
      <div style="display:flex;flex-direction:column;align-items:center;">
        <div style="
          width:36px;height:36px;border-radius:50%;
          background:${s.done ? 'linear-gradient(135deg,#10b981,#059669)' : 'rgba(0,0,0,0.06)'};
          display:flex;align-items:center;justify-content:center;
          font-size:16px;
          box-shadow:${s.done ? '0 4px 12px rgba(16,185,129,0.3)' : 'none'};
          flex-shrink:0;
        ">${s.done ? s.icon : '○'}</div>
        ${i < steps.length - 1 ? `<div style="width:2px;height:20px;margin-top:4px;background:${steps[i+1].done ? 'linear-gradient(180deg,#10b981,#d4af37)' : 'rgba(0,0,0,0.08)'};border-radius:2px;"></div>` : ''}
      </div>
      <div style="padding-top:6px;">
        <div style="font-size:13px;font-weight:800;color:${s.done ? '#064e35' : '#9ca3af'};">${s.label}</div>
      </div>
    </div>`).join('');

  return `
    <div style="background:#f6fef9;border:1px solid rgba(16,185,129,0.15);border-radius:14px;padding:18px;">
      <div style="font-family:'Playfair Display',serif;font-size:14px;font-weight:700;color:#064e35;margin-bottom:16px;">📍 Pickup Timeline</div>
      ${items}
    </div>`;
}

/* ═══════════════════════════════════════════════════════════════
   HELPERS
═══════════════════════════════════════════════════════════════ */

function val(id) {
  const el = document.getElementById(id);
  return el ? el.value.trim() : '';
}

function setError(id, msg) {
  const el = document.getElementById(id);
  if (el) el.textContent = msg;
}

function clearError(id) {
  const el = document.getElementById(id);
  if (el) el.textContent = '';
}

function setText(id, text) {
  const el = document.getElementById(id);
  if (el) el.textContent = text;
}

function formatDate(dateStr) {
  if (!dateStr) return '–';
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' });
}

/* Live summary update on date change */
document.addEventListener('DOMContentLoaded', () => {
  const pd = document.getElementById('pickupDate');
  if (pd) {
    pd.addEventListener('change', () => {
      const el = document.getElementById('sumDate');
      if (el) el.textContent = pd.value ? formatDate(pd.value) : '–';
    });
  }
});