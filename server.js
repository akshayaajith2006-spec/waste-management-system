const express = require('express');
const path    = require('path');

const app  = express();
const PORT = process.env.PORT || 5501;

// Serve static files
app.use(express.static(path.join(__dirname)));

// Root → splash screen
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'splash.html'));
});

// 404 fallback
app.use((req, res) => {
  res.status(404).send(`
    <div style="font-family:sans-serif;text-align:center;padding:60px;">
      <h2>404 – Page Not Found</h2>
      <p><a href="/">Go to Home</a></p>
    </div>
  `);
});

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});