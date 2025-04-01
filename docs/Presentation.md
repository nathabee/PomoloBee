## **PomoloBee Orchard Monitoring Estimation App**

> 📱 An Android app that lets you **link fruit images to a location in a field**, and then **estimate yield** from those images—either with an ML model or manually.

---

### **Main Menu Features**

| Screen           | Purpose |
|------------------|---------|
| 📷 **CameraScreen** | Take or import a photo of the orchard |
| 📍 **LocationScreen** | Link the photo to a specific field + row |
| 🗺️ **SvgMapScreen** | Select a row visually from an SVG layout |
| 📡 **ProcessingScreen** | Manage pending images, trigger estimation (local or remote) |
| 📊 **ResultScreen** | View analysis results: fruit count, estimated yield, etc. |
| ⚙️ **SettingsScreen** | Configure app, sync orchard data |
| 🌳 **OrchardScreen** | Read-only view of field and row layout |
| ℹ️ **AboutScreen** | General app info |

---

### **What’s the App For?**

- Capture or import a **photo of a tree or row** in an orchard
- **Link this photo to a specific location**
- Launch a **yield estimation** (fruit count, weight, etc.)
- **Track results over time** and compare to past estimations
- Use a **local or remote ML model** *(but the core idea is the tracking, not the AI)*

---

### **What About Machine Learning?**

- It’s **optional**! ML lets you **automatically detect fruit** in the photo.
- But you can also **enter results manually** or simply use the image as a **visual trace linked to a row**.

---

### **Typical Use Case**

1. User takes a picture → selects field and row from list or SVG map.
2. Image is saved **locally** on device.
3. Later, the user can:
   - Send it to the **Django backend** for processing,
   - Or use a **local embedded ML model**.
4. Once analyzed, the results are shown and can be **compared historically**.

---

### **Bonus Offline? No Problem.**

- The app works **without internet**: images are saved locally.
- Sync with backend is **manual**, user-controlled.
- A **Jetpack DataStore** keeps all data safe, even after reboot.

--- 