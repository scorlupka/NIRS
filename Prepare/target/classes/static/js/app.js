const apiUrl = '/api/auth';

async function postJson(url, payload) {
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
    });
    const data = await res.json();
    return { ok: res.ok, data };
}

function setStatus(el, ok, message) {
    if (!el) return;
    el.className = ok ? 'status success' : 'status error';
    el.textContent = message;
}

function saveSession(user) {
    localStorage.setItem('hotelUser', JSON.stringify(user));
}

function loadSession() {
    const raw = localStorage.getItem('hotelUser');
    return raw ? JSON.parse(raw) : null;
}

function clearSession() {
    localStorage.removeItem('hotelUser');
}

function renderSessionInfo() {
    const holder = document.getElementById('session-info');
    if (!holder) return;
    const user = loadSession();
    if (!user) {
        holder.innerHTML = '<span>Не авторизованы</span>';
        return;
    }
    holder.innerHTML = `<span>${user.username}</span> <span class="tag">${user.role}</span>`;
}

function applyRoleUi() {
    const user = loadSession();

    const btnLogin = document.getElementById('btn-login');
    const btnRegister = document.getElementById('btn-register');
    const btnMyOrders = document.getElementById('btn-my-orders');
    const btnNewOrder = document.getElementById('btn-new-order');
    const btnAdminPanel = document.getElementById('btn-admin-panel');

    if (!user) {
        if (btnMyOrders) btnMyOrders.style.display = 'none';
        if (btnNewOrder) btnNewOrder.style.display = 'none';
        if (btnAdminPanel) btnAdminPanel.style.display = 'none';
    } else {
        if (btnLogin) btnLogin.style.display = 'none';
        if (btnRegister) btnRegister.style.display = 'none';

        if (user.role === 'ADMIN') {
            if (btnAdminPanel) btnAdminPanel.style.display = 'inline-block';
        } else {
            if (btnAdminPanel) btnAdminPanel.style.display = 'none';
        }
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const status = document.getElementById('login-status');
    const payload = {
        username: event.target.username.value,
        password: event.target.password.value
    };
    const { ok, data } = await postJson(`${apiUrl}/login`, payload);
    if (ok) {
        saveSession(data);
        setStatus(status, true, 'Вход выполнен');
        setTimeout(() => window.location.href = '/', 500);
    } else {
        setStatus(status, false, data.error || 'Ошибка входа');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const status = document.getElementById('register-status');
    const payload = {
        username: event.target.username.value,
        password: event.target.password.value,
        role: event.target.role.value,
        nameLastname: event.target.nameLastname.value,
        phone: event.target.phone.value,
        passportSeria: event.target.passportSeria.value,
        passportNumber: event.target.passportNumber.value,
        clientType: event.target.clientType.value
    };

    const { ok, data } = await postJson(`${apiUrl}/register`, payload);
    if (ok) {
        saveSession(data);
        setStatus(status, true, 'Регистрация завершена');
        setTimeout(() => window.location.href = '/', 500);
    } else {
        setStatus(status, false, data.error || 'Ошибка регистрации');
    }
}

function handleLogout(event) {
    event.preventDefault();
    clearSession();
    window.location.href = '/login';
}

document.addEventListener('DOMContentLoaded', () => {
    renderSessionInfo();
    applyRoleUi();
    const loginForm = document.getElementById('login-form');
    if (loginForm) loginForm.addEventListener('submit', handleLogin);

    const registerForm = document.getElementById('register-form');
    if (registerForm) registerForm.addEventListener('submit', handleRegister);

    const logoutButton = document.getElementById('logout-btn');
    if (logoutButton) logoutButton.addEventListener('click', handleLogout);
});

