const API = 'http://localhost:8080';

const JOURS = [
    ['MONDAY', 'Lundi'], ['TUESDAY', 'Mardi'], ['WEDNESDAY', 'Mercredi'],
    ['THURSDAY', 'Jeudi'], ['FRIDAY', 'Vendredi'], ['SATURDAY', 'Samedi'], ['SUNDAY', 'Dimanche']
];

const STATUT_BADGE = {
    PLANIFIE: 'secondary', CONFIRME: 'success', ANNULE: 'danger', TERMINE: 'dark'
};

function esc(value) {
    if (value === null || value === undefined) return '';
    return String(value).replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
}

async function api(path, options = {}) {
    const headers = {};
    if (options.body) headers['Content-Type'] = 'application/json';
    const token = localStorage.getItem('token');
    if (token) headers['Authorization'] = 'Bearer ' + token;
    const res = await fetch(API + path, {
        method: options.method || 'GET',
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined
    });
    const text = await res.text();
    let data = null;
    if (text) {
        try { data = JSON.parse(text); } catch (e) { data = text; }
    }
    if (!res.ok) {
        throw new Error((data && data.message) ? data.message : 'Erreur ' + res.status);
    }
    return data;
}

function getRoles() {
    return JSON.parse(localStorage.getItem('roles') || '[]');
}

function primaryRole() {
    const roles = getRoles();
    if (roles.includes('ROLE_ADMIN')) return 'ADMIN';
    if (roles.includes('ROLE_MEDECIN')) return 'MEDECIN';
    if (roles.includes('ROLE_PATIENT')) return 'PATIENT';
    return null;
}

function saveAuth(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('roles', JSON.stringify(data.roles || []));
    localStorage.setItem('email', data.email || '');
}

function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('email');
}

function msg(text, type = 'success') {
    const box = document.getElementById('messages');
    const id = 'm' + Date.now();
    box.insertAdjacentHTML('afterbegin',
        `<div id="${id}" class="alert alert-${type} alert-dismissible fade show">${esc(text)}
         <button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`);
    setTimeout(() => document.getElementById(id)?.remove(), 5000);
}

const content = () => document.getElementById('content');

const MENUS = {
    ADMIN: [
        { id: 'specialites', label: 'Spécialités', render: renderSpecialites },
        { id: 'medecins', label: 'Médecins', render: renderMedecinsAdmin },
        { id: 'patients', label: 'Patients', render: renderPatients }
    ],
    MEDECIN: [
        { id: 'mesrdv', label: 'Mes rendez-vous', render: renderRdvMedecin },
        { id: 'consultation', label: 'Nouvelle consultation', render: renderConsultation },
        { id: 'ordonnance', label: 'Nouvelle ordonnance', render: renderOrdonnance }
    ],
    PATIENT: [
        { id: 'prendre', label: 'Prendre rendez-vous', render: renderPrendreRdv },
        { id: 'mesrdv', label: 'Mes rendez-vous', render: renderRdvPatient },
        { id: 'dossier', label: 'Mon dossier', render: renderDossier },
        { id: 'ordonnances', label: 'Mes ordonnances', render: renderMesOrdonnances }
    ]
};

function showApp() {
    document.getElementById('login-view').classList.add('d-none');
    document.getElementById('app-view').classList.remove('d-none');
    document.getElementById('navbar').classList.remove('d-none');
    const role = primaryRole();
    document.getElementById('user-info').textContent = localStorage.getItem('email') + ' (' + role + ')';
    const menu = document.getElementById('menu');
    const items = MENUS[role] || [];
    menu.innerHTML = items.map((it, i) =>
        `<li class="nav-item"><button class="nav-link ${i === 0 ? 'active' : ''}" data-id="${it.id}">${it.label}</button></li>`
    ).join('');
    menu.querySelectorAll('button').forEach(btn => {
        btn.addEventListener('click', () => {
            menu.querySelectorAll('button').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const item = items.find(x => x.id === btn.dataset.id);
            item.render();
        });
    });
    if (items.length) items[0].render();
}

function showLogin() {
    document.getElementById('app-view').classList.add('d-none');
    document.getElementById('navbar').classList.add('d-none');
    document.getElementById('login-view').classList.remove('d-none');
}

async function loading() {
    content().innerHTML = '<div class="text-center text-muted py-5">Chargement…</div>';
}

async function renderSpecialites() {
    await loading();
    const specs = await api('/api/specialites');
    content().innerHTML = `
        <div class="row">
            <div class="col-lg-7">
                <div class="card"><div class="card-body">
                    <h5 class="card-title">Spécialités</h5>
                    <table class="table table-sm">
                        <thead><tr><th>#</th><th>Nom</th><th>Description</th><th></th></tr></thead>
                        <tbody>${specs.map(s => `<tr>
                            <td>${s.id}</td><td>${esc(s.nom)}</td><td>${esc(s.description)}</td>
                            <td><button class="btn btn-sm btn-outline-danger" data-del="${s.id}">Suppr.</button></td>
                        </tr>`).join('')}</tbody>
                    </table>
                </div></div>
            </div>
            <div class="col-lg-5">
                <div class="card"><div class="card-body">
                    <h6 class="card-title">Nouvelle spécialité</h6>
                    <form id="spec-form">
                        <input class="form-control mb-2" id="spec-nom" placeholder="Nom" required>
                        <input class="form-control mb-2" id="spec-desc" placeholder="Description">
                        <button class="btn btn-primary w-100">Ajouter</button>
                    </form>
                </div></div>
            </div>
        </div>`;
    document.getElementById('spec-form').addEventListener('submit', async e => {
        e.preventDefault();
        try {
            await api('/api/specialites', { method: 'POST', body: {
                nom: document.getElementById('spec-nom').value,
                description: document.getElementById('spec-desc').value
            }});
            msg('Spécialité ajoutée');
            renderSpecialites();
        } catch (err) { msg(err.message, 'danger'); }
    });
    content().querySelectorAll('[data-del]').forEach(b => b.addEventListener('click', async () => {
        try { await api('/api/specialites/' + b.dataset.del, { method: 'DELETE' }); msg('Supprimée'); renderSpecialites(); }
        catch (err) { msg(err.message, 'danger'); }
    }));
}

async function renderMedecinsAdmin() {
    await loading();
    const [medecins, specs] = await Promise.all([api('/api/medecins'), api('/api/specialites')]);
    content().innerHTML = `
        <div class="row">
            <div class="col-lg-7">
                <div class="card"><div class="card-body">
                    <h5 class="card-title">Médecins</h5>
                    <table class="table table-sm">
                        <thead><tr><th>#</th><th>Nom</th><th>Spécialité</th><th>Durée</th></tr></thead>
                        <tbody>${medecins.map(m => `<tr>
                            <td>${m.id}</td><td>Dr. ${esc(m.prenom)} ${esc(m.nom)}</td>
                            <td>${esc(m.specialiteNom)}</td><td>${m.dureeConsultationMinutes} min</td>
                        </tr>`).join('')}</tbody>
                    </table>
                </div></div>
            </div>
            <div class="col-lg-5">
                <div class="card"><div class="card-body">
                    <h6 class="card-title">Nouveau médecin</h6>
                    <form id="med-form">
                        <div class="row g-2">
                            <div class="col"><input class="form-control" id="med-nom" placeholder="Nom" value="Bousquet" required></div>
                            <div class="col"><input class="form-control" id="med-prenom" placeholder="Prénom" value="Emile" required></div>
                        </div>
                        <input type="email" class="form-control mt-2" id="med-email" placeholder="Courriel" required>
                        <input type="password" class="form-control mt-2" id="med-pass" placeholder="Mot de passe" required>
                        <select class="form-select mt-2" id="med-spec" required>
                            <option value="">— Spécialité —</option>
                            ${specs.map(s => `<option value="${s.id}">${esc(s.nom)}</option>`).join('')}
                        </select>
                        <input type="number" class="form-control mt-2" id="med-duree" placeholder="Durée consultation (min)" value="30" required>
                        <label class="form-label mt-3 mb-1">Horaires de travail</label>
                        <div id="horaires"></div>
                        <button type="button" class="btn btn-sm btn-outline-secondary mt-1" id="add-horaire">+ Ajouter une plage</button>
                        <button class="btn btn-primary w-100 mt-3">Créer le médecin</button>
                    </form>
                </div></div>
            </div>
        </div>`;
    const horaires = document.getElementById('horaires');
    const addHoraire = () => horaires.insertAdjacentHTML('beforeend', `
        <div class="row g-1 mb-1 horaire-row">
            <div class="col-5"><select class="form-select form-select-sm h-jour">
                ${JOURS.map(j => `<option value="${j[0]}">${j[1]}</option>`).join('')}</select></div>
            <div class="col"><input type="time" class="form-control form-control-sm h-debut" value="09:00"></div>
            <div class="col"><input type="time" class="form-control form-control-sm h-fin" value="12:00"></div>
        </div>`);
    addHoraire();
    document.getElementById('add-horaire').addEventListener('click', addHoraire);
    document.getElementById('med-form').addEventListener('submit', async e => {
        e.preventDefault();
        const hors = [...horaires.querySelectorAll('.horaire-row')].map(r => ({
            jourSemaine: r.querySelector('.h-jour').value,
            heureDebut: r.querySelector('.h-debut').value + ':00',
            heureFin: r.querySelector('.h-fin').value + ':00'
        }));
        try {
            await api('/api/medecins', { method: 'POST', body: {
                nom: document.getElementById('med-nom').value,
                prenom: document.getElementById('med-prenom').value,
                email: document.getElementById('med-email').value,
                motDePasse: document.getElementById('med-pass').value,
                specialiteId: Number(document.getElementById('med-spec').value),
                dureeConsultationMinutes: Number(document.getElementById('med-duree').value),
                horaires: hors
            }});
            msg('Médecin créé');
            renderMedecinsAdmin();
        } catch (err) { msg(err.message, 'danger'); }
    });
}

async function renderPatients() {
    await loading();
    const patients = await api('/api/patients');
    content().innerHTML = `<div class="card"><div class="card-body">
        <h5 class="card-title">Patients</h5>
        <table class="table table-sm">
            <thead><tr><th>#</th><th>Nom</th><th>Courriel</th><th>Téléphone</th><th>Dossier</th></tr></thead>
            <tbody>${patients.map(p => `<tr>
                <td>${p.id}</td><td>${esc(p.prenom)} ${esc(p.nom)}</td><td>${esc(p.email)}</td>
                <td>${esc(p.telephone)}</td><td>${esc(p.numeroDossier)}</td>
            </tr>`).join('')}</tbody>
        </table></div></div>`;
}

function rdvRow(r, actions) {
    return `<tr>
        <td>${r.id}</td>
        <td>${esc(r.dateHeureDebut.replace('T', ' '))}</td>
        <td>${esc(r.medecinNom)}</td>
        <td>${esc(r.patientNom)}</td>
        <td><span class="badge bg-${STATUT_BADGE[r.statut] || 'secondary'}">${r.statut}</span></td>
        <td>${actions}</td>
    </tr>`;
}

async function changerStatut(id, statut) {
    try { await api('/api/rendez-vous/' + id + '/statut', { method: 'PATCH', body: { statut } }); msg('Statut: ' + statut); }
    catch (err) { msg(err.message, 'danger'); }
}

async function renderRdvMedecin() {
    await loading();
    const rdvs = await api('/api/rendez-vous/medecin');
    content().innerHTML = `<div class="card"><div class="card-body">
        <h5 class="card-title">Mes rendez-vous</h5>
        <table class="table table-sm align-middle">
            <thead><tr><th>#</th><th>Date</th><th>Médecin</th><th>Patient</th><th>Statut</th><th>Actions</th></tr></thead>
            <tbody>${rdvs.map(r => {
                let a = '';
                if (r.statut === 'PLANIFIE') a = `<button class="btn btn-sm btn-success" data-act="CONFIRME" data-id="${r.id}">Confirmer</button> `;
                if (r.statut === 'CONFIRME') a += `<button class="btn btn-sm btn-dark" data-act="TERMINE" data-id="${r.id}">Terminer</button> `;
                if (r.statut === 'PLANIFIE' || r.statut === 'CONFIRME') a += `<button class="btn btn-sm btn-outline-danger" data-act="ANNULE" data-id="${r.id}">Annuler</button>`;
                return rdvRow(r, a);
            }).join('')}</tbody>
        </table></div></div>`;
    content().querySelectorAll('[data-act]').forEach(b => b.addEventListener('click', async () => {
        await changerStatut(b.dataset.id, b.dataset.act);
        renderRdvMedecin();
    }));
}

async function rdvSelectOptions() {
    const rdvs = await api('/api/rendez-vous/medecin');
    return rdvs.map(r => `<option value="${r.id}">#${r.id} — ${esc(r.patientNom)} — ${r.dateHeureDebut.replace('T', ' ')} (${r.statut})</option>`).join('');
}

async function renderConsultation() {
    await loading();
    const options = await rdvSelectOptions();
    content().innerHTML = `<div class="card"><div class="card-body" style="max-width:640px">
        <h5 class="card-title">Nouvelle consultation</h5>
        <form id="cons-form">
            <label class="form-label">Rendez-vous</label>
            <select class="form-select mb-2" id="cons-rdv" required>${options}</select>
            <textarea class="form-control mb-2" id="cons-notes" placeholder="Notes du médecin" rows="3" required></textarea>
            <textarea class="form-control mb-2" id="cons-diag" placeholder="Diagnostic" rows="2" required></textarea>
            <button class="btn btn-primary">Enregistrer la consultation</button>
        </form></div></div>`;
    document.getElementById('cons-form').addEventListener('submit', async e => {
        e.preventDefault();
        try {
            await api('/api/consultations', { method: 'POST', body: {
                rendezVousId: Number(document.getElementById('cons-rdv').value),
                notes: document.getElementById('cons-notes').value,
                diagnostic: document.getElementById('cons-diag').value
            }});
            msg('Consultation enregistrée (le rendez-vous passe à TERMINÉ)');
            renderConsultation();
        } catch (err) { msg(err.message, 'danger'); }
    });
}

async function renderOrdonnance() {
    await loading();
    const options = await rdvSelectOptions();
    content().innerHTML = `<div class="card"><div class="card-body" style="max-width:720px">
        <h5 class="card-title">Nouvelle ordonnance</h5>
        <form id="ord-form">
            <label class="form-label">Rendez-vous</label>
            <select class="form-select mb-2" id="ord-rdv" required>${options}</select>
            <input type="number" class="form-control mb-2" id="ord-duree" placeholder="Durée de validité (jours)" value="30" required>
            <label class="form-label">Médicaments</label>
            <div id="lignes"></div>
            <button type="button" class="btn btn-sm btn-outline-secondary" id="add-ligne">+ Ajouter un médicament</button>
            <button class="btn btn-primary w-100 mt-3">Rédiger l'ordonnance</button>
        </form></div></div>`;
    const lignes = document.getElementById('lignes');
    const addLigne = () => lignes.insertAdjacentHTML('beforeend', `
        <div class="row g-1 mb-1 ligne-row">
            <div class="col"><input class="form-control form-control-sm l-med" placeholder="Médicament" required></div>
            <div class="col"><input class="form-control form-control-sm l-pos" placeholder="Posologie" required></div>
            <div class="col"><input class="form-control form-control-sm l-duree" placeholder="Durée"></div>
        </div>`);
    addLigne();
    document.getElementById('add-ligne').addEventListener('click', addLigne);
    document.getElementById('ord-form').addEventListener('submit', async e => {
        e.preventDefault();
        const items = [...lignes.querySelectorAll('.ligne-row')].map(r => ({
            medicament: r.querySelector('.l-med').value,
            posologie: r.querySelector('.l-pos').value,
            dureeTraitement: r.querySelector('.l-duree').value
        }));
        try {
            await api('/api/ordonnances', { method: 'POST', body: {
                rendezVousId: Number(document.getElementById('ord-rdv').value),
                dureeValiditeJours: Number(document.getElementById('ord-duree').value),
                lignes: items
            }});
            msg('Ordonnance rédigée');
            renderOrdonnance();
        } catch (err) { msg(err.message, 'danger'); }
    });
}

async function renderPrendreRdv() {
    await loading();
    const medecins = await api('/api/medecins');
    content().innerHTML = `<div class="card"><div class="card-body" style="max-width:720px">
        <h5 class="card-title">Prendre un rendez-vous</h5>
        <div class="row g-2 align-items-end">
            <div class="col-md-5">
                <label class="form-label">Médecin</label>
                <select class="form-select" id="rdv-medecin">
                    ${medecins.map(m => `<option value="${m.id}">Dr. ${esc(m.prenom)} ${esc(m.nom)} — ${esc(m.specialiteNom)}</option>`).join('')}
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label">Date</label>
                <input type="date" class="form-control" id="rdv-date">
            </div>
            <div class="col-md-3">
                <button class="btn btn-outline-primary w-100" id="voir-dispo">Voir disponibilités</button>
            </div>
        </div>
        <input class="form-control mt-2" id="rdv-motif" placeholder="Motif (optionnel)">
        <div id="slots" class="mt-3"></div>
    </div></div>`;
    document.getElementById('voir-dispo').addEventListener('click', async () => {
        const medId = document.getElementById('rdv-medecin').value;
        const date = document.getElementById('rdv-date').value;
        if (!date) { msg('Choisissez une date', 'warning'); return; }
        const slotsBox = document.getElementById('slots');
        slotsBox.innerHTML = 'Chargement…';
        try {
            const slots = await api(`/api/medecins/${medId}/disponibilites?date=${date}`);
            if (!slots.length) { slotsBox.innerHTML = '<div class="alert alert-warning">Aucun créneau libre ce jour-là.</div>'; return; }
            slotsBox.innerHTML = '<p class="mb-2">Créneaux libres :</p>' + slots.map(s =>
                `<button class="btn btn-outline-success btn-sm slot-btn" data-debut="${s.heureDebut}">${s.heureDebut.slice(0,5)}</button>`
            ).join('');
            slotsBox.querySelectorAll('.slot-btn').forEach(b => b.addEventListener('click', async () => {
                try {
                    await api('/api/rendez-vous', { method: 'POST', body: {
                        medecinId: Number(medId),
                        dateHeureDebut: `${date}T${b.dataset.debut}`,
                        motif: document.getElementById('rdv-motif').value
                    }});
                    msg('Rendez-vous réservé !');
                    document.getElementById('voir-dispo').click();
                } catch (err) { msg(err.message, 'danger'); }
            }));
        } catch (err) { slotsBox.innerHTML = ''; msg(err.message, 'danger'); }
    });
}

async function renderRdvPatient() {
    await loading();
    const rdvs = await api('/api/rendez-vous/patient');
    content().innerHTML = `<div class="card"><div class="card-body">
        <h5 class="card-title">Mes rendez-vous</h5>
        <table class="table table-sm align-middle">
            <thead><tr><th>#</th><th>Date</th><th>Médecin</th><th>Patient</th><th>Statut</th><th></th></tr></thead>
            <tbody>${rdvs.map(r => {
                const a = (r.statut === 'PLANIFIE' || r.statut === 'CONFIRME')
                    ? `<button class="btn btn-sm btn-outline-danger" data-id="${r.id}">Annuler</button>` : '';
                return rdvRow(r, a);
            }).join('')}</tbody>
        </table></div></div>`;
    content().querySelectorAll('[data-id]').forEach(b => b.addEventListener('click', async () => {
        await changerStatut(b.dataset.id, 'ANNULE');
        renderRdvPatient();
    }));
}

async function renderDossier() {
    await loading();
    try {
        const d = await api('/api/dossiers/mon-dossier');
        content().innerHTML = `<div class="card"><div class="card-body">
            <h5 class="card-title">Dossier médical — ${esc(d.patientNom)}</h5>
            <p class="text-muted">N° de dossier : ${esc(d.numeroDossier)}</p>
            ${d.consultations.length ? d.consultations.map(c => `
                <div class="border rounded p-3 mb-2">
                    <div class="fw-bold">${esc(c.dateConsultation.replace('T',' '))} — ${esc(c.medecinNom)}</div>
                    <div><strong>Diagnostic :</strong> ${esc(c.diagnostic)}</div>
                    <div><strong>Notes :</strong> ${esc(c.notes)}</div>
                </div>`).join('') : '<div class="alert alert-info">Aucune consultation enregistrée.</div>'}
        </div></div>`;
    } catch (err) { msg(err.message, 'danger'); content().innerHTML = ''; }
}

async function renderMesOrdonnances() {
    await loading();
    const ords = await api('/api/ordonnances/mes');
    content().innerHTML = `<div class="card"><div class="card-body">
        <h5 class="card-title">Mes ordonnances</h5>
        ${ords.length ? ords.map(o => `
            <div class="border rounded p-3 mb-2">
                <div class="fw-bold">Ordonnance #${o.id} — ${esc(o.dateEmission)} — ${esc(o.medecinNom)}</div>
                <div class="text-muted small">Valide ${o.dureeValiditeJours} jours</div>
                <ul class="mb-0 mt-1">${o.lignes.map(l => `<li>${esc(l.medicament)} — ${esc(l.posologie)} ${l.dureeTraitement ? '(' + esc(l.dureeTraitement) + ')' : ''}</li>`).join('')}</ul>
            </div>`).join('') : '<div class="alert alert-info">Aucune ordonnance.</div>'}
    </div></div>`;
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('login-form').addEventListener('submit', async e => {
        e.preventDefault();
        const errBox = document.getElementById('login-error');
        errBox.classList.add('d-none');
        try {
            const data = await api('/api/auth/login', { method: 'POST', body: {
                email: document.getElementById('login-email').value,
                motDePasse: document.getElementById('login-password').value
            }});
            saveAuth(data);
            showApp();
        } catch (err) { errBox.textContent = err.message; errBox.classList.remove('d-none'); }
    });

    document.getElementById('show-register').addEventListener('click', () => {
        document.getElementById('register-card').classList.toggle('d-none');
    });

    document.getElementById('register-form').addEventListener('submit', async e => {
        e.preventDefault();
        const errBox = document.getElementById('register-error');
        errBox.classList.add('d-none');
        try {
            const data = await api('/api/auth/register', { method: 'POST', body: {
                nom: document.getElementById('reg-nom').value,
                prenom: document.getElementById('reg-prenom').value,
                email: document.getElementById('reg-email').value,
                motDePasse: document.getElementById('reg-password').value,
                telephone: document.getElementById('reg-tel').value,
                dateNaissance: document.getElementById('reg-naissance').value || null
            }});
            saveAuth(data);
            showApp();
        } catch (err) { errBox.textContent = err.message; errBox.classList.remove('d-none'); }
    });

    document.getElementById('logout-btn').addEventListener('click', () => {
        clearAuth();
        showLogin();
    });

    if (localStorage.getItem('token')) showApp(); else showLogin();
});
