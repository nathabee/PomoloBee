// Load and render Markdown using marked.js
async function loadMarkdown(file) {
  const res = await fetch(file);
  return await res.text();
}

// Render checklist interactively
function parseChecklist(md) {
  return md.replace(/^- \[( |x)] (.+)$/gm, (match, checked, item) => {
    const isChecked = checked === "x";
    return `<label><input type="checkbox" ${isChecked ? "checked" : ""}> ${item}</label>`;
  });
}

// Load Project Presentation
function loadPresentation() {
  loadMarkdown("presentation.md").then(text => {
    document.getElementById("content").innerHTML =
      `<div class='markdown'>${marked.parse(text)}</div>`;
  });
}

// Load Interactive Checklist
function loadChecklist() {
  loadMarkdown("App_Test_ChecklistTemplate.md").then(md => {
    const checklistHtml = parseChecklist(md);
    document.getElementById("content").innerHTML = `
      <h2>✅ Interactive App Test Checklist</h2>
      <div id='checklist' class='markdown'>${checklistHtml}</div>
      <button onclick="saveChecklist()">💾 Save Checklist Report</button>
    `;
  });
}

// Save checklist to file
function saveChecklist() {
  const checkboxes = document.querySelectorAll('#checklist input[type="checkbox"]');
  const lines = Array.from(checkboxes).map(cb => {
    const mark = cb.checked ? "x" : " ";
    const label = cb.parentElement.textContent.trim();
    return `- [${mark}] ${label}`;
  });
  const content = `# App Test Checklist\n\nDate: ${new Date().toISOString().split('T')[0]}\n\n` + lines.join("\n");

  const blob = new Blob([content], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `App_Test_Checklist_${new Date().toISOString().split('T')[0]}.md`;
  a.click();
  URL.revokeObjectURL(url);
}

// 📌 New: Handle all .md links as markdown-rendered pages
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
        });
      });
    }
  });
});
