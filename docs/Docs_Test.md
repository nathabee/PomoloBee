 
# **GitHub Pages Documentation Portal**

Welcome to the **User Manual** of the **PomoloBee Android App**, powered by GitHub Pages.

---

## Overview

This documentation site is built using static files hosted on GitHub Pages. It allows you to:
- Read technical and user documentation
- Browse Markdown files in a clean UI
- Launch an **interactive test checklist**
- Export test results in Markdown or JSON

---

<details>
<summary>📑 Table of Contents</summary>

<!-- TOC -->
- [**GitHub Pages Documentation Portal**](#github-pages-documentation-portal)
  - [Overview](#overview)
  - [Installation Deployment](#installation-deployment)
    - [▶️ Local Preview](#local-preview)
    - [GitHub Pages Setup](#github-pages-setup)
    - [⤴️ Deployment](#deployment)
  - [️ Project Structure](#project-structure)
  - [How It Works](#how-it-works)
<!-- TOC END -->

</details>

---

## Installation Deployment

### ▶️ Local Preview

To preview the site locally without Django interfering:

```bash
python -m http.server 8001
```

Then open [http://localhost:8001](http://localhost:8001) in your browser.

---

### GitHub Pages Setup

- [ ] Configure repository settings for GitHub Pages
- [ ] Set source branch to `main` and root to `/docs`
- [ ] Add `CNAME` file if using a custom domain

---

### ⤴️ Deployment

Push changes to `main` to trigger GitHub Pages deployment automatically:

```bash
git push origin main
```

> 💡 No manual deployment needed — GitHub builds from the `/docs` folder on push.

---

## ️ Project Structure

All documentation files are inside the `/docs` folder:

```
📁 docs/
├── .nojekyll
├── index.html          ← Entry point for GitHub Pages
├── main.js             ← Script for Markdown loading & test checklist
├── style.css           ← (Optional) Styling
├── *.md                ← Markdown documentation
├── App_Test_checklist.json ← Structured app test data
└── images/             ← (Optional) Screenshots & illustrations
```

---

## How It Works

- The `index.html` file is the homepage.
- Clicking on any `.md` file in the menu loads its content below.
- Selecting **App Test Checklist** starts an interactive testing session.
- `App_Test_checklist.json` is parsed by `main.js` and rendered as editable test forms.
- You can modify:
  - Test **descriptions**
  - Test **results**: ✅ Pass / ⚠️ Partial / ❌ Fail
  - Add optional **notes**
- At the end, you can **export** your session:
  - Save as a `.md` file (Markdown summary)
  - Or as `.json` for later continuation

---
 