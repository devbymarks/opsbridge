const $ = (selector) => document.querySelector(selector);
let incidents = [];
let selectedId = null;

const labels = {
  OPEN: "Aberto", IN_PROGRESS: "Em andamento", WAITING_FOR_USER: "Aguardando usuário",
  ESCALATED: "Escalonado", RESOLVED: "Resolvido", CLOSED: "Fechado",
  LOW: "Baixa", MEDIUM: "Média", HIGH: "Alta", CRITICAL: "Crítica",
  PENDING: "Pendente", PASSED: "Aprovado", FAILED: "Reprovado",
  INCONCLUSIVE: "Inconclusivo", NOT_APPLICABLE: "Não aplicável"
};
const categories = { APPLICATION: "Aplicação", NETWORK: "Rede / DNS", DATABASE: "Banco de dados", ACCESS: "Acesso / permissões", HARDWARE: "Hardware" };
const dateFmt = (value) => {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "—" : date.toLocaleString("pt-BR", {dateStyle:"short",timeStyle:"short"});
};
const esc = (value) => String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
function toast(message, error=false) {
  const el = $("#toast"); el.textContent = message; el.className = "toast" + (error ? " error" : "");
  el.hidden = false; setTimeout(() => el.hidden = true, 3600);
}
async function api(url, options={}) {
  const response = await fetch(url, {headers:{"Content-Type":"application/json", ...(options.headers||{})}, ...options});
  if (!response.ok) {
    let msg = `Erro HTTP ${response.status}`;
    try { const body = await response.json(); msg = body.message || JSON.stringify(body.fields || body); } catch {}
    throw new Error(msg);
  }
  const type = response.headers.get("content-type") || "";
  return type.includes("application/json") ? response.json() : response.text();
}
function statusPill(value) { return `<span class="pill ${esc(value)}">${esc(labels[value] || value)}</span>`; }
async function loadIncidents() {
  try {
    incidents = await api("/api/incidents");
    $("#total").textContent = incidents.length;
    $("#navIncidentCount").textContent = incidents.length;
    $("#open").textContent = incidents.filter(i => i.status === "OPEN").length;
    $("#progress").textContent = incidents.filter(i => i.status === "IN_PROGRESS").length;
    $("#escalated").textContent = incidents.filter(i => i.status === "ESCALATED").length;
    renderRows();
  } catch (e) {
    const count = $("#tableCount"); if (count) count.textContent = "API INDISPONÍVEL";
    toast("Não foi possível carregar a API: " + e.message, true);
  }
}
function renderRows() {
  const term = $("#search").value.trim().toLowerCase(), status = $("#statusFilter").value;
  const filtered = incidents.filter(i => (!status || i.status === status) &&
    [i.title,i.category,categories[i.category],i.reporter,String(i.id)].some(v => String(v || "").toLowerCase().includes(term)));
  const count = $("#tableCount"); if (count) count.textContent = `${filtered.length} ${filtered.length === 1 ? "REGISTRO" : "REGISTROS"}`;
  $("#incidentRows").innerHTML = filtered.length ? filtered.map(i => `
    <tr>
      <td><button class="incident-link" data-open="${i.id}">#${i.id} · ${esc(i.title)}<small>${esc(i.reporter)}</small></button></td>
      <td>${esc(categories[i.category] || i.category)}</td><td>${statusPill(i.priority)}</td><td>${statusPill(i.status)}</td>
      <td>${dateFmt(i.createdAt)}</td><td><button class="secondary" data-open="${i.id}">Abrir</button></td>
    </tr>`).join("") : `<tr><td colspan="6" class="empty">Nenhum incidente encontrado.</td></tr>`;
  document.querySelectorAll("[data-open]").forEach(btn => btn.addEventListener("click", () => openIncident(Number(btn.dataset.open))));
}
async function openIncident(id) {
  try {
    const i = await api(`/api/incidents/${id}`); selectedId = id;
    $("#detailPanel").hidden = false; $("#detailTitle").textContent = `#${i.id} · ${i.title}`;
    $("#detailDescription").textContent = `${categories[i.category] || i.category} · Prioridade ${labels[i.priority] || i.priority} · Solicitante: ${i.reporter} · Criado em ${dateFmt(i.createdAt)}`;
    $("#detailStatus").value = i.status; $("#assignee").value = i.assignedTo || "";
    renderChecks(i.checks || []); renderActivities(i.activities || []);
    $("#resolveForm").querySelector('[name="resolution"]').value = i.resolution || "";
    $("#detailPanel").scrollIntoView({behavior:"smooth",block:"start"});
  } catch(e) { toast(e.message,true); }
}
function renderChecks(checks) {
  $("#checks").innerHTML = checks.length ? checks.map(c => `
    <div class="check"><div><strong>${esc(c.title)} ${statusPill(c.status)}</strong><p>Esperado: ${esc(c.expectedResult)}</p></div>
      <select data-check-status="${c.id}" aria-label="Status da verificação">
        ${["PENDING","PASSED","FAILED","INCONCLUSIVE","NOT_APPLICABLE"].map(s=>`<option value="${s}" ${c.status===s?"selected":""}>${labels[s]}</option>`).join("")}
      </select>
      <textarea data-check-notes="${c.id}" rows="2" placeholder="Evidência / observações">${esc(c.notes || "")}</textarea>
      <button class="secondary" data-save-check="${c.id}">Salvar resultado</button>
    </div>`).join("") : `<p class="muted">Nenhuma verificação cadastrada.</p>`;
  document.querySelectorAll("[data-save-check]").forEach(btn => btn.addEventListener("click", async () => {
    const cid = btn.dataset.saveCheck;
    try {
      await api(`/api/incidents/${selectedId}/checks/${cid}`, {method:"PATCH",body:JSON.stringify({
        status:$(`[data-check-status="${cid}"]`).value,
        notes:$(`[data-check-notes="${cid}"]`).value
      })});
      toast("Resultado do diagnóstico salvo."); await openIncident(selectedId); await loadIncidents();
    } catch(e) { toast(e.message,true); }
  }));
}
function renderActivities(activities) {
  $("#activities").innerHTML = activities.length ? activities.slice().reverse().map(a=>`
    <div class="event"><strong>${esc(a.type.replaceAll("_"," "))} · ${dateFmt(a.createdAt)}</strong><p>${esc(a.details)}</p></div>`).join("") :
    `<p class="muted">Ainda não há atividades registradas.</p>`;
}
$("#incidentForm").addEventListener("submit", async e => {
  e.preventDefault(); const data = Object.fromEntries(new FormData(e.currentTarget));
  try {
    const created = await api("/api/incidents",{method:"POST",body:JSON.stringify(data)});
    e.currentTarget.reset(); $("#formMessage").textContent = `Incidente #${created.id} registrado.`;
    toast(`Incidente #${created.id} criado com sucesso.`); await loadIncidents(); await openIncident(created.id);
  } catch(err) { $("#formMessage").textContent = err.message; toast(err.message,true); }
});
$("#detailStatus").addEventListener("change", async e => {
  if (!selectedId) return;
  try { await api(`/api/incidents/${selectedId}/status`,{method:"PATCH",body:JSON.stringify({status:e.target.value})}); toast("Status atualizado."); await openIncident(selectedId); await loadIncidents(); }
  catch(err) { toast(err.message,true); }
});
$("#assignBtn").addEventListener("click", async () => {
  if (!selectedId) return;
  const assignedTo = $("#assignee").value.trim();
  if (!assignedTo) { toast("Informe o nome do responsável.", true); $("#assignee").focus(); return; }
  try { await api(`/api/incidents/${selectedId}/assignee`,{method:"PATCH",body:JSON.stringify({assignedTo})}); toast("Responsável atualizado."); await openIncident(selectedId); }
  catch(e) { toast(e.message,true); }
});
$("#addCheckForm").addEventListener("submit", async e => {
  e.preventDefault(); if (!selectedId) return;
  const data = Object.fromEntries(new FormData(e.currentTarget));
  try { await api(`/api/incidents/${selectedId}/checks`,{method:"POST",body:JSON.stringify(data)}); e.currentTarget.reset(); toast("Verificação adicionada."); await openIncident(selectedId); }
  catch(err) { toast(err.message,true); }
});
$("#resolveForm").addEventListener("submit", async e => {
  e.preventDefault(); if (!selectedId) return;
  const resolution = new FormData(e.currentTarget).get("resolution");
  try { await api(`/api/incidents/${selectedId}/resolve`,{method:"POST",body:JSON.stringify({resolution})}); toast("Resolução registrada."); await openIncident(selectedId); await loadIncidents(); }
  catch(err) { toast(err.message,true); }
});
$("#reportBtn").addEventListener("click", async () => {
  if (!selectedId) return;
  try {
    const report = await api(`/api/incidents/${selectedId}/report`);
    const blob = new Blob([report],{type:"text/plain;charset=utf-8"});
    const url = URL.createObjectURL(blob), a = document.createElement("a");
    a.href=url; a.download=`opsbridge-incidente-${selectedId}.txt`; a.click(); URL.revokeObjectURL(url);
  } catch(e) { toast(e.message,true); }
});
$("#refreshBtn").addEventListener("click",loadIncidents);
$("#search").addEventListener("input",renderRows);
$("#statusFilter").addEventListener("change",renderRows);
$("#newBtn").addEventListener("click",() => $("#new-incident").scrollIntoView({behavior:"smooth"}));
$("#closeDetail").addEventListener("click",() => {$("#detailPanel").hidden=true;selectedId=null;});
const todayLabel = $("#todayLabel");
if (todayLabel) todayLabel.textContent = new Date().toLocaleDateString("pt-BR", {day:"2-digit", month:"short", year:"numeric"}).toUpperCase();
document.querySelectorAll(".primary-nav a").forEach(link => link.addEventListener("click", () => {
  document.querySelectorAll(".primary-nav a").forEach(item => item.classList.remove("active"));
  link.classList.add("active");
}));
loadIncidents();
