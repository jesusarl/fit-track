const API = {
    users: "/api/users",
    workouts: "/api/workouts",
};

const panelTitles = {
    dashboard: "Panel principal",
    user: "Nuevo usuario",
    workout: "Registrar entrenamiento",
    stats: "Estadísticas",
};

let users = [];
let toastTimeout;

document.addEventListener("DOMContentLoaded", () => {
    setupNavigation();
    setupForms();
    setupStatsSelect();
    setDefaultDate();
    loadAllData();
});

function setupNavigation() {
    document.querySelectorAll(".nav-btn").forEach((btn) => {
        btn.addEventListener("click", () => showPanel(btn.dataset.panel));
    });

    document.querySelectorAll("[data-go]").forEach((btn) => {
        btn.addEventListener("click", () => showPanel(btn.dataset.go));
    });

    document.getElementById("refresh-btn").addEventListener("click", loadAllData);
}

function showPanel(panelId) {
    document.querySelectorAll(".nav-btn").forEach((btn) => {
        btn.classList.toggle("active", btn.dataset.panel === panelId);
    });

    document.querySelectorAll(".panel").forEach((panel) => {
        panel.classList.toggle("active", panel.id === `panel-${panelId}`);
    });

    document.getElementById("panel-title").textContent = panelTitles[panelId] || "FitTrack";
}

function setupForms() {
    document.getElementById("user-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = event.currentTarget;
        const payload = {
            nombre: form.nombre.value.trim(),
            email: form.email.value.trim(),
        };

        try {
            await apiPost(API.users, payload);
            form.reset();
            showToast("Usuario creado correctamente", "success");
            await loadAllData();
            showPanel("dashboard");
        } catch (error) {
            showToast(error.message, "error");
        }
    });

    document.getElementById("workout-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = event.currentTarget;
        const payload = {
            usuarioId: Number(form.usuarioId.value),
            tipoEjercicio: form.tipoEjercicio.value,
            distanciaKm: Number(form.distanciaKm.value),
            tiempoMinutos: Number(form.tiempoMinutos.value),
            fecha: form.fecha.value,
        };

        try {
            const workout = await apiPost(API.workouts, payload);
            form.reset();
            setDefaultDate();
            showToast(
                `Entrenamiento guardado. Ritmo: ${formatPace(workout.ritmoPromedio)} min/km`,
                "success"
            );
            await loadAllData();
            document.getElementById("stats-user-select").value = String(payload.usuarioId);
            await loadUserStats(payload.usuarioId);
            showPanel("stats");
        } catch (error) {
            showToast(error.message, "error");
        }
    });
}

function setupStatsSelect() {
    document.getElementById("stats-user-select").addEventListener("change", async (event) => {
        const userId = event.target.value;
        if (!userId) {
            document.getElementById("stats-empty").classList.remove("hidden");
            document.getElementById("stats-content").classList.add("hidden");
            return;
        }
        await loadUserStats(userId);
    });
}

function setDefaultDate() {
    const dateInput = document.querySelector('#workout-form input[name="fecha"]');
    if (dateInput) {
        dateInput.value = new Date().toISOString().slice(0, 10);
    }
}

async function loadAllData() {
    try {
        users = await apiGet(API.users);
        renderUsers();
        populateUserSelects();
        await refreshDashboardStats();
    } catch (error) {
        showToast("No se pudo conectar con la API. ¿Está la app en ejecución?", "error");
    }
}

function renderUsers() {
    const container = document.getElementById("users-list");

    if (!users.length) {
        container.className = "users-list empty-state";
        container.textContent = "No hay usuarios todavía. Crea el primero en la sección «Nuevo usuario».";
        return;
    }

    container.className = "users-list";
    container.innerHTML = users
        .map(
            (user) => `
            <article class="user-item">
                <div>
                    <strong>${escapeHtml(user.nombre)}</strong>
                    <span>${escapeHtml(user.email)}</span>
                </div>
                <span class="badge">ID ${user.id}</span>
            </article>
        `
        )
        .join("");
}

function populateUserSelects() {
    const options = users
        .map((user) => `<option value="${user.id}">${escapeHtml(user.nombre)}</option>`)
        .join("");

    const placeholder = '<option value="">Selecciona un usuario</option>';
    document.getElementById("workout-user-select").innerHTML = placeholder + options;
    document.getElementById("stats-user-select").innerHTML = placeholder + options;
}

async function refreshDashboardStats() {
    document.getElementById("stat-users").textContent = String(users.length);

    if (!users.length) {
        document.getElementById("stat-workouts").textContent = "0";
        document.getElementById("stat-distance").textContent = "0 km";
        document.getElementById("stat-time").textContent = "0 min";
        return;
    }

    const summaries = await Promise.all(
        users.map((user) => apiGet(`${API.workouts}/user/${user.id}`))
    );

    const totalWorkouts = summaries.reduce((acc, item) => acc + item.workouts.length, 0);
    const totalDistance = summaries.reduce((acc, item) => acc + item.distanciaTotalKm, 0);
    const totalTime = summaries.reduce((acc, item) => acc + item.tiempoTotalMinutos, 0);

    document.getElementById("stat-workouts").textContent = String(totalWorkouts);
    document.getElementById("stat-distance").textContent = `${formatNumber(totalDistance)} km`;
    document.getElementById("stat-time").textContent = `${totalTime} min`;
}

async function loadUserStats(userId) {
    try {
        const data = await apiGet(`${API.workouts}/user/${userId}`);

        document.getElementById("stats-empty").classList.add("hidden");
        document.getElementById("stats-content").classList.remove("hidden");

        document.getElementById("user-distance").textContent = `${formatNumber(data.distanciaTotalKm)} km`;
        document.getElementById("user-time").textContent = `${data.tiempoTotalMinutos} min`;
        document.getElementById("user-workouts-count").textContent = String(data.workouts.length);

        const tbody = document.getElementById("workouts-table-body");

        if (!data.workouts.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="empty-state">Este usuario aún no tiene entrenamientos registrados.</td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = data.workouts
            .map(
                (workout) => `
                <tr>
                    <td>${formatDate(workout.fecha)}</td>
                    <td>
                        <span class="type-pill ${workout.tipoEjercicio === "RUNNING" ? "running" : "cycling"}">
                            ${workout.tipoEjercicio === "RUNNING" ? "Running" : "Ciclismo"}
                        </span>
                    </td>
                    <td>${formatNumber(workout.distanciaKm)} km</td>
                    <td>${workout.tiempoMinutos} min</td>
                    <td>${formatPace(workout.ritmoPromedio)} min/km</td>
                </tr>
            `
            )
            .join("");
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function apiGet(url) {
    const response = await fetch(url);
    return handleResponse(response);
}

async function apiPost(url, body) {
    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });
    return handleResponse(response);
}

async function handleResponse(response) {
    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
        ? await response.json()
        : null;

    if (!response.ok) {
        throw new Error(data?.error || "Ocurrió un error inesperado");
    }

    return data;
}

function showToast(message, type = "success") {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.className = `toast show ${type}`;

    clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => {
        toast.classList.remove("show");
    }, 3500);
}

function formatNumber(value) {
    return Number(value).toLocaleString("es-ES", {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2,
    });
}

function formatPace(value) {
    return Number(value).toLocaleString("es-ES", {
        minimumFractionDigits: 1,
        maximumFractionDigits: 2,
    });
}

function formatDate(isoDate) {
    const [year, month, day] = isoDate.split("-");
    return `${day}/${month}/${year}`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
