const AUTH_KEY = 'eventhub_auth';
const USER_KEY = 'eventhub_user';

function getAuthHeader() {
    return sessionStorage.getItem(AUTH_KEY);
}

function getUser() {
    return JSON.parse(sessionStorage.getItem(USER_KEY));
}

function isLoggedIn() {
    return !!getAuthHeader();
}

function logout() {
    sessionStorage.removeItem(AUTH_KEY);
    sessionStorage.removeItem(USER_KEY);
    window.location.href = '/html/login.html';
}

async function apiFetch(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    if (isLoggedIn()) {
        headers['Authorization'] = getAuthHeader();
    }
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401) {
        logout();
        return null;
    }
    return response;
}

function renderNavbar(containerId = 'navbar') {
    const user = getUser();
    const navbar = document.getElementById(containerId);
    if (!navbar) return;

    let links = `<a href="/html/events.html">Eventi</a> `;

    if (user) {
        links += `<a href="/html/my-bookings.html">Le mie prenotazioni</a> `;
        links += `<a href="/html/profile.html">Profilo</a> `;
        if (user.role === 'ORGANIZER' || user.role === 'ADMIN') {
            links += `<a href="/html/organizer-events.html">I miei eventi</a> `;
        }
        if (user.role === 'ADMIN') {
            links += `<a href="/html/admin.html">Admin</a> `;
        }
        links += `<span>${user.email} [${user.role}]</span> `;
        links += `<button onclick="logout()">Logout</button>`;
    } else {
        links += `<a href="/html/login.html">Login</a> `;
        links += `<a href="/html/signup.html">Registrati</a>`;
    }

    navbar.innerHTML = links;
}