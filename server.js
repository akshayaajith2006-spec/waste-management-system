const express = require('express');
const path = require('path');

const app = express();
const PORT = 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Root → splash (FIRST)
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'splash.html'));
});

app.get('/splash', (req, res) => {
  res.sendFile(path.join(__dirname, 'splash.html'));
});

app.get('/login', (req, res) => {
  res.sendFile(path.join(__dirname, 'login.html'));
});

app.get('/index', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.get('/request', (req, res) => {
  res.sendFile(path.join(__dirname, 'request.html'));
});

app.get('/status', (req, res) => {
  res.sendFile(path.join(__dirname, 'status.html'));
});

app.get('/userprofile', (req, res) => {
  res.sendFile(path.join(__dirname, 'userprofile.html'));
});

app.get('/adminprofile', (req, res) => {
  res.sendFile(path.join(__dirname, 'adminprofile.html'));
});

app.get('/admin_pending', (req, res) => {
  res.sendFile(path.join(__dirname, 'admin_pending.html'));
});

app.get('/admin_inprogress', (req, res) => {
  res.sendFile(path.join(__dirname, 'admin_inprogress.html'));
});

app.get('/admin_done', (req, res) => {
  res.sendFile(path.join(__dirname, 'admin_done.html'));
});

// Static files — index: false prevents auto serving index.html
app.use(express.static(__dirname, { index: false }));

// 404 fallback
app.use((req, res) => {
  res.status(404).send('<h2>404 - Page Not Found</h2>');
});

app.listen(PORT, () => {
  console.log(`✅ Server running at http://localhost:${PORT}`);
});