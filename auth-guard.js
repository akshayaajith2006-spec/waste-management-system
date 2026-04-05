/* =============================================
   EcoPickup · auth-guard.js
   Role-Based Access Control (RBAC) Guard
   ============================================= */

// ✅ Initialize Firebase if not already initialized
if (!firebase.apps.length) {
    const firebaseConfig = {
        apiKey: "AIzaSyAQTRW_YFVBCuuNlOe4wZ0rvEQnKDHrT-c",
        authDomain: "waste-management-system-7b2d4.firebaseapp.com",
        projectId: "waste-management-system-7b2d4",
        storageBucket: "waste-management-system-7b2d4.firebasestorage.app",
        messagingSenderId: "791452329907",
        appId: "1:791452329907:web:9f1909e3d2bff35f354dc6"
    };
    firebase.initializeApp(firebaseConfig);
}

const authGuard = firebase.auth();
const dbGuard = firebase.firestore();

// Guard Execution
authGuard.onAuthStateChanged(async (user) => {
    const path = window.location.pathname;
    
    // 1. If NOT logged in (and not on login/splash) → Redirect to login
    if (!user) {
        if (!path.includes('login.html') && !path.includes('splash.html') && path !== '/') {
            window.location.href = 'login.html';
        }
        return;
    }

    // 2. Fetch user role from Firestore
    try {
        const userDoc = await dbGuard.collection('users').doc(user.uid).get();
        if (!userDoc.exists) {
            // New user without doc? Redirect to login to complete profile
            console.warn("User document not found.");
            return;
        }

        const userData = userDoc.data();
        const role = userData.role; // 'admin' or 'user'
        const isAdminPage = path.includes('admin_') || path.includes('adminprofile');

        // 3. RBAC Enforcement
        if (role === 'admin') {
            // Admins shouldn't be on user pages (optional, but keeps flow clean)
            if (path.includes('index.html') || path.includes('request.html') || path.includes('status.html') || path.includes('userprofile.html')) {
                // window.location.href = 'admin_pending.html';
            }
        } else {
            // Regular users MUST NOT be on admin pages
            if (isAdminPage) {
                console.error("Unauthorized: Admin access required.");
                window.location.href = 'index.html';
            }
        }
    } catch (error) {
        console.error("Auth Guard Error:", error);
    }
});
