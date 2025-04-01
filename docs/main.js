// Load and render structured checklist JSON
async function loadStructuredChecklist() {
  const res = await fetch("App_Test_checklist.json");
  const data = await res.json();

  const container = document.getElementById("content");
  container.innerHTML = `<h2>✅ Interactive App Test Checklist</h2>`;

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
      row.appendChild(note);

      sectionDiv.appendChild(row);
    });

    container.appendChild(sectionDiv);
  });

  const saveBtn = document.createElement("button");
  saveBtn.textContent = "💾 Save Checklist Report";
  saveBtn.onclick = saveStructuredChecklist;
  container.appendChild(saveBtn);
}

function saveStructuredChecklist() {
  const sections = document.querySelectorAll(".checklist-section");
  let lines = [`# App Test Checklist\n`, `Date: ${new Date().toISOString().split('T')[0]}\n`];

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

// Swap to use structured checklist instead of raw markdown one
function loadChecklist() {
  loadStructuredChecklist();
}

//display markdown
async function loadMarkdown(file) {
  const res = await fetch(file);
  return await res.text();
}


// 📌 Handle all .md links as markdown-rendered pages
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
