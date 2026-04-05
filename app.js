/* =============================================
   EcoPickup · app.js  (FIXED)
   ============================================= */
'use strict';

/* ── Waste selection ─────────────────────────── */
window.selectWaste = function (el, type) {
  document.querySelectorAll('.waste-option').forEach(o => o.classList.remove('selected'));
  el.classList.add('selected');
  const inp = document.getElementById('wasteType');
  if (inp) inp.value = type;
  const err = document.getElementById('wasteError');
  if (err) err.textContent = '';
};

/* ── Helpers ─────────────────────────────────── */
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

/* ── Step navigation ─────────────────────────── */
window.goStep = function (step) {
  if (step === 2) {
    const name = val('name'), phone = val('phone'), address = val('address');
    let ok = true;
    ['nameError','phoneError','addressError'].forEach(id => clearError(id));
    if (!name)   { setError('nameError', 'Name is required'); ok = false; }
    if (!/^\d{10}$/.test(phone)) { setError('phoneError', 'Enter valid 10-digit phone'); ok = false; }
    if (!address){ setError('addressError', 'Address is required'); ok = false; }
    if (!ok) return;
  }
  if (step === 3) {
    const wt = val('wasteType');
    clearError('wasteError');
    if (!wt) { setError('wasteError', 'Please select a waste type'); return; }
    setText('sumName',  val('name'));
    setText('sumPhone', val('phone'));
    setText('sumWaste', wt);
    const d = val('pickupDate');
    setText('sumDate', d ? formatDate(d) : '–');
  }
  document.querySelectorAll('.form-step').forEach(s => s.classList.remove('active'));
  const next = document.getElementById('formStep' + step);
  if (next) next.classList.add('active');
  for (let i = 1; i <= 3; i++) {
    const dot = document.getElementById('step' + i + '-dot');
    if (dot) dot.classList.toggle('active', i <= step);
  }
  for (let i = 1; i <= 2; i++) {
    const line = document.getElementById('line' + i);
    if (line) line.classList.toggle('active', i < step);
  }
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

/* ── Date live update ────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  const pd = document.getElementById('pickupDate');
  if (pd) {
    pd.min = new Date().toISOString().split('T')[0];
    pd.addEventListener('change', () => {
      setText('sumDate', pd.value ? formatDate(pd.value) : '–');
    });
  }
});