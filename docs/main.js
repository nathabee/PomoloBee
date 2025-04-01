// Load and render structured checklist JSON with metadata
async function loadStructuredChecklist(file = "App_Test_checklist.json") {
  const res = await fetch(file);
  const json = await res.json();
  const meta = json.meta || {};
  const data = json.sections || json; // fallback if no meta wrapper

  const container = document.getElementById("content");
  container.innerHTML = `<h2>✅ Interactive App Test Checklist</h2>`;

  // ====== Metadata Form ======
  const metaBox = document.createElement("div");
  metaBox.classList.add("meta-box");
  metaBox.innerHTML = `
    <h3>📝 Test Session Info</h3>
    <label>Date: <input type="date" id="meta-date" value="${meta.date || new Date().toISOString().split('T')[0]}"></label>
    <label>Tester: <input type="text" id="meta-tester" value="${meta.tester || ''}"></label>
    <label>Device: <input type="text" id="meta-device" value="${meta.device || ''}"></label>
    <label>Android Version: <input type="text" id="meta-android" value="${meta.androidVersion || ''}"></label>
    <label>App Version: <input type="text" id="meta-app" value="${meta.appVersion || ''}"></label>
    <label>Build Variant: <input type="text" id="meta-build" value="${meta.buildVariant || ''}"></label>
  `;
  container.appendChild(metaBox);

  // ====== Button Row ======
  const buttonRow = document.createElement("div");
  buttonRow.style.display = "flex";
  buttonRow.style.gap = "1rem";
  buttonRow.style.marginBottom = "1.5rem";
  buttonRow.style.alignItems = "center";
  buttonRow.style.flexWrap = "wrap";

  const newChecklistBtn = document.createElement("button");
  newChecklistBtn.textContent = "🆕 New App Test Checklist";
  newChecklistBtn.onclick = () => loadStructuredChecklist("App_Test_checklist.json");
  buttonRow.appendChild(newChecklistBtn);

  const openLabel = document.createElement("label");
  openLabel.textContent = "📂 Open App JSON Checklist";
  openLabel.classList.add("button-like");

  const loadJsonInput = document.createElement("input");
  loadJsonInput.type = "file";
  loadJsonInput.accept = ".json";
  loadJsonInput.style.display = "none";
  loadJsonInput.onchange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        const loadedData = JSON.parse(event.target.result);
        renderChecklistFromData(loadedData);
      };
      reader.readAsText(file);
    }
  };

  openLabel.appendChild(loadJsonInput);
  buttonRow.appendChild(openLabel);
  container.appendChild(buttonRow);

  // ====== Checklist Rendering ======
  data.forEach((section, secIndex) => {
    const sectionDiv = document.createElement("div");
    sectionDiv.classList.add("checklist-section");

    const title = document.createElement("h3");
    title.textContent = section.section;
    sectionDiv.appendChild(title);

    section.items.forEach((item, itemIndex) => {
      const row = document.createElement("div");
      row.classList.add("checklist-item");

      const label = document.createElement("p");
      label.innerHTML = `<strong>${item.test}</strong><br><em>${item.expected}</em>`;
      row.appendChild(label);

      const options = ["Pass", "Partial", "Fail"];
      const name = `check-${secIndex}-${itemIndex}`;
      options.forEach(opt => {
        const input = document.createElement("input");
        input.type = "radio";
        input.name = name;
        input.value = opt;
        if (item.state === opt) input.checked = true;

        const radioLabel = document.createElement("label");
        radioLabel.style.marginRight = "1rem";
        radioLabel.appendChild(input);
        radioLabel.append(` ${opt}`);
        row.appendChild(radioLabel);
      });

      const note = document.createElement("input");
      note.type = "text";
      note.placeholder = "(Optional) Notes if Partial/Fail";
      note.classList.add("note-field");
      if (item.note) note.value = item.note;
      row.appendChild(note);

      sectionDiv.appendChild(row);
    });

    container.appendChild(sectionDiv);
  });

  // ====== Save Buttons ======
  const saveBtn = document.createElement("button");
  saveBtn.textContent = "💾 Save Checklist Report (Markdown)";
  saveBtn.onclick = saveStructuredChecklist;
  container.appendChild(saveBtn);

  const saveJsonBtn = document.createElement("button");
  saveJsonBtn.textContent = "🗄 Save Checklist as JSON";
  saveJsonBtn.onclick = saveChecklistAsJSON;
  container.appendChild(saveJsonBtn);
}

function getMetaFromFields() {
  return {
    date: document.getElementById("meta-date")?.value || "",
    tester: document.getElementById("meta-tester")?.value || "",
    device: document.getElementById("meta-device")?.value || "",
    androidVersion: document.getElementById("meta-android")?.value || "",
    appVersion: document.getElementById("meta-app")?.value || "",
    buildVariant: document.getElementById("meta-build")?.value || ""
  };
}

function saveChecklistAsJSON() {
  const meta = getMetaFromFields();
  const sections = document.querySelectorAll(".checklist-section");
  const structured = [];

  sections.forEach((section, secIndex) => {
    const heading = section.querySelector("h3").textContent;
    const items = [];
    section.querySelectorAll(".checklist-item").forEach((item, itemIndex) => {
      const labelText = item.querySelector("p").innerHTML.split("<br>")[0];
      const expected = item.querySelector("p em").innerText;
      const selected = item.querySelector("input[type='radio']:checked");
      const note = item.querySelector(".note-field").value;

      items.push({
        test: labelText.replace(/<[^>]+>/g, ""),
        expected,
        state: selected ? selected.value : "",
        note: note || ""
      });
    });
    structured.push({ section: heading, items });
  });

  const full = { meta, sections: structured };
  const blob = new Blob([JSON.stringify(full, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `App_Test_Checklist_${new Date().toISOString().split('T')[0]}.json`;
  a.click();
  URL.revokeObjectURL(url);
}

function saveStructuredChecklist() {
  const meta = getMetaFromFields();
  const sections = document.querySelectorAll(".checklist-section");
  let lines = [`# App Test Checklist\n`, `\n## ️Test Session`,
    `- **Date:** ${meta.date}`,
    `- **Tester:** ${meta.tester}`,
    `- **Device:** ${meta.device}`,
    `- **Android Version:** ${meta.androidVersion}`,
    `- **App Version:** ${meta.appVersion}`,
    `- **Build Variant:** ${meta.buildVariant}`,
    `\n---\n`];

  sections.forEach(section => {
    const heading = section.querySelector("h3").textContent;
    lines.push(`\n## ${heading}`);

    const items = section.querySelectorAll(".checklist-item");
    items.forEach(item => {
      const testLabel = item.querySelector("p").innerText.split("\n")[0];
      const selected = item.querySelector("input[type='radio']:checked");
      const note = item.querySelector(".note-field").value;
      const status = selected ? selected.value : "Not marked";

      lines.push(`- **${testLabel}** — ${status}${note ? `: ${note}` : ""}`);
    });
  });

  const content = lines.join("\n");
  const blob = new Blob([content], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `App_Test_Checklist_${new Date().toISOString().split('T')[0]}.md`;
  a.click();
  URL.revokeObjectURL(url);
}

function renderChecklistFromData(data) {
  const container = document.getElementById("content");
  container.innerHTML = "";
  const blob = new Blob([JSON.stringify(data)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  loadStructuredChecklist(url);
}

function loadChecklist() {
  loadStructuredChecklist();
}

function scrollToContent() {
  const contentDiv = document.getElementById("content");
  contentDiv.scrollIntoView({ behavior: "smooth" });
}

document.addEventListener("DOMContentLoaded", () => {
  const docLinks = document.querySelectorAll(".doc-list a");
  docLinks.forEach(link => {
    if (link.href.endsWith(".md")) {
      link.addEventListener("click", (e) => {
        e.preventDefault();
        const file = link.getAttribute("href");
        loadMarkdown(file).then(md => {
          document.getElementById("content").innerHTML =
            `<div class="markdown">${marked.parse(md)}</div>`;
          scrollToContent();
        });
      });
    }
  });
});

async function loadMarkdown(file) {
  const res = await fetch(file);
  return await res.text();
}