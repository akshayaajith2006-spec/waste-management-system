/* ================================================================
   EcoPickup · validate.js
   Member 1 – Frontend Validation Helpers
   Used in: request.html, login.html, status.html
   ================================================================ */

/* ── Field value getter ─────────────────────────── */
function val(id) {
  const el = document.getElementById(id);
  return el ? el.value.trim() : '';
}

/* ── Show error message ─────────────────────────── */
function setError(id, msg) {
  const el = document.getElementById(id);
  if (el) { el.textContent = msg; el.style.display = 'block'; }
}

/* ── Clear error message ────────────────────────── */
function clearError(id) {
  const el = document.getElementById(id);
  if (el) { el.textContent = ''; el.style.display = 'none'; }
}

/* ── Clear all errors ───────────────────────────── */
function clearAllErrors(...ids) {
  ids.forEach(clearError);
}

/* ── Validate: not empty ────────────────────────── */
function isNotEmpty(value) {
  return value !== null && value.trim().length > 0;
}

/* ── Validate: 10-digit phone ───────────────────── */
function isValidPhone(phone) {
  return /^\d{10}$/.test(phone);
}

/* ── Validate: email format ─────────────────────── */
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

/* ── Validate: future or today date ────────────── */
function isValidPickupDate(dateStr) {
  if (!dateStr) return false;
  const picked = new Date(dateStr);
  const today  = new Date();
  today.setHours(0, 0, 0, 0);
  return picked >= today;
}

/* ── Validate: Request ID format ────────────────── */
function isValidRequestId(id) {
  return /^REQ\d+$/i.test(id.trim());
}

/* ── Validate full signup form ──────────────────── */
function validateSignup(nameId, emailId, phoneId, passId,
                        nameErrId, emailErrId, phoneErrId, passErrId) {
  clearAllErrors(nameErrId, emailErrId, phoneErrId, passErrId);
  let ok = true;

  if (!isNotEmpty(val(nameId)))
    { setError(nameErrId, 'Full name is required'); ok = false; }

  if (!isValidEmail(val(emailId)))
    { setError(emailErrId, 'Enter a valid email address'); ok = false; }

  if (!isValidPhone(val(phoneId)))
    { setError(phoneErrId, 'Enter a valid 10-digit phone number'); ok = false; }

  if (val(passId).length < 6)
    { setError(passErrId, 'Password must be at least 6 characters'); ok = false; }

  return ok;
}

/* ── Validate request form step 1 ───────────────── */
function validateRequestStep1(nameId, phoneId, addressId,
                               nameErr, phoneErr, addrErr) {
  clearAllErrors(nameErr, phoneErr, addrErr);
  let ok = true;

  if (!isNotEmpty(val(nameId)))
    { setError(nameErr, 'Full name is required'); ok = false; }

  if (!isValidPhone(val(phoneId)))
    { setError(phoneErr, 'Enter a valid 10-digit phone number'); ok = false; }

  if (!isNotEmpty(val(addressId)))
    { setError(addrErr, 'Address is required'); ok = false; }

  return ok;
}

/* ── Validate status track form ─────────────────── */
function validateStatusForm(idFieldId, idErrId) {
  clearError(idErrId);
  const id = val(idFieldId);

  if (!isNotEmpty(id))
    { setError(idErrId, 'Please enter your Request ID'); return false; }

  if (!isValidRequestId(id))
    { setError(idErrId, 'Format: REQ followed by numbers (e.g. REQ1001)'); return false; }

  return true;
}

/* ── Format date to readable string ────────────── */
function formatDate(dateStr) {
  if (!dateStr) return '–';
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-IN', { day:'numeric', month:'long', year:'numeric' });
}