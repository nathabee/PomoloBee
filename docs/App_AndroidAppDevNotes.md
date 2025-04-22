

**Android App Dev Notes**
Understanding coding in an Android App using **Jetpack Compose**, **ViewModel**, and **MVVM** architecture.


_Work in progress..._


---
# Table of Content
<details>
<summary>Table of Content</summary>
 
<!-- TOC -->
- [Table of Content](#table-of-content)
- [Good Practices](#good-practices)
  - [Coroutine Dispatchers](#coroutine-dispatchers)
    - [What’s the Problem?](#whats-the-problem)
    - [The Solution Use Coroutine Dispatchers](#the-solution-use-coroutine-dispatchers)
    - [Analogy Threads as a Restaurant](#analogy-threads-as-a-restaurant)
    - [Dispatcher Cheatsheet TL;DR](#dispatcher-cheatsheet-tldr)
  - [️ MVVM Architecture in Android](#mvvm-architecture-in-android)
    - [Overview](#overview)
    - [Why Use MVVM?](#why-use-mvvm)
    - [Structure Example](#structure-example)
    - [️ Common Patterns](#common-patterns)
    - [Anti-Patterns to Avoid](#anti-patterns-to-avoid)
    - [TL;DR](#tldr)
  - [MVVM](#mvvm)
  - [to do instead of multiplying views into screens](#to-do-instead-of-multiplying-views-into-screens)
    - [Here's a better cleaner approach Use a **shared `AppViewModelContainer`** or **dependency provider**](#heres-a-better-cleaner-approach-use-a-shared-appviewmodelcontainer-or-dependency-provider)
- [Good Analyses and Peformances](#good-analyses-and-peformances)
  - [Startup Logs Performance Notes](#startup-logs-performance-notes)
    - [What Logs Show](#what-logs-show)
    - [What to Watch For](#what-to-watch-for)
    - [How to Improve](#how-to-improve)
<!-- TOC END -->
 
</details>

---
 
 

 

---

# Good Practices

## Coroutine Dispatchers

### What’s the Problem?
If you run heavy operations (like saving images, reading large files, or querying the database) on the **main thread**, your app will freeze or lag.

### The Solution Use Coroutine Dispatchers

- 🧵 `launch {}` → Start a coroutine (a lightweight thread-like task)
- 🛠️ `withContext(Dispatchers.IO)` → Run on a background thread made for IO operations (disk, files, network)

This pattern keeps your app **smooth and responsive**.

```kotlin
val scope = rememberCoroutineScope()
scope.launch {
    withContext(Dispatchers.IO) {
        // Perform long operation here
    }
}
```

---

### Analogy Threads as a Restaurant

- 👨‍🍳 **Main Thread** = The waiter taking orders. Must stay fast and free.
- 🔧 **Dispatchers.IO** = The kitchen where food is made (slow, behind the scenes).
- 📕 `LaunchedEffect` = A recipe book the kitchen uses when the waiter *tells* it a dish was ordered.
- 🎒 `rememberCoroutineScope()` = The waiter's clipboard to pass jobs to the kitchen.

---

### Dispatcher Cheatsheet TL;DR

| Concept | What It Does | Where to Use It |
|--------|---------------|-----------------|
| `LaunchedEffect` | Runs side-effects on recomposition | Inside Composables |
| `rememberCoroutineScope()` | Gives a coroutine scope bound to the Composable lifecycle | Inside Composables (safe for `onClick`, etc.) |
| `launch { withContext(Dispatchers.IO) { ... } }` | Performs slow tasks in background | Inside button clicks, user actions, data saving |

---
 
## ️ MVVM Architecture in Android

MVVM stands for **Model - View - ViewModel**. It’s a design pattern that helps structure your app for **clean separation of concerns** and **testable, maintainable code**.

---

### Overview

| Layer | Responsibility |
|-------|----------------|
| **Model** | Business logic & data (e.g. Room DB, JSON files, network calls) |
| **View** | UI layer — your Composables (`@Composable` functions) |
| **ViewModel** | Middleman between View and Model. Holds UI state, handles logic, survives config changes |

---

### Why Use MVVM?

- Keeps UI clean and logic-free ✅
- Easy to test ViewModel in isolation 🧪
- State survives rotation (ViewModel outlives activity) 🔁
- Good fit for Jetpack Compose with `collectAsState()` and `state flows`

---

### Structure Example

```kotlin
// 🧠 ViewModel
class SettingsViewModel : ViewModel() {
    val selectedFieldId = MutableStateFlow<Int?>(null)

    fun updateSelectedField(id: Int) {
        selectedFieldId.value = id
    }
}

// 🎨 View (Composable)
@Composable
fun FieldSelector(settingsViewModel: SettingsViewModel) {
    val fieldId by settingsViewModel.selectedFieldId.collectAsState()

    Text("Selected field = $fieldId")
}
```

---

### ️ Common Patterns

- Use `StateFlow` or `LiveData` in ViewModel to expose state
- Use `collectAsState()` in Composable to observe state
- Don’t do business logic inside Composables — push it to ViewModel

---

### Anti-Patterns to Avoid

| Anti-Pattern | Why it’s bad |
|--------------|--------------|
| Business logic in `@Composable` | UI should only render, not compute |
| Reading files directly in UI layer | Blocks the UI, breaks separation |
| Saving state in `remember {}` only | Won’t survive configuration changes |

---

### TL;DR

- **ViewModel** = Your logic/states hub 🔁
- **Composable** = Just shows stuff 🎨
- Use **StateFlow** or **LiveData** to connect the two 🔗

---
## MVVM

✅ Best practice architecture
Let’s break it into 3 layers:

Repository Layer (like ConnectionRepository)

Deals with cloud + local filesystem.

Updates cache (OrchardCache).

ViewModel Layer (like ImageViewModel)

Observes cache.

Exposes StateFlow<List<ImageRecord>>.

Calls loadImageData(context, uri) to refresh from local.

UI Layer (like Screen)

Triggers ViewModel calls.

Shows state.

---
## to do instead of multiplying views into screens
Yeah, you're hitting on a **very real frustration** that comes with managing `ViewModel`s and `shared state` across screens in Compose — especially as your app grows and the screens rely on more and more data.

You're absolutely right:  
If **every screen needs access to** things like:
- `SettingsViewModel` (for URI, syncMode, etc.)
- `OrchardViewModel` (for location/field data)
- `ImageViewModel` (for image data & updates)

...then passing them **individually to every screen** gets:
- Verbose  
- Error-prone  
- Annoying as you add new dependencies  

---

### Here's a better cleaner approach Use a **shared `AppViewModelContainer`** or **dependency provider**

Instead of passing 3+ ViewModels into every screen, you wrap them together into one object or provide them via `CompositionLocal`.

#### Option 1 `AppViewModels` container

```kotlin
data class AppViewModels(
    val settings: SettingsViewModel,
    val orchard: OrchardViewModel,
    val image: ImageViewModel
)
```

Then in your `MainActivity`, you create it and pass it down:

```kotlin
val appViewModels = remember {
    AppViewModels(settingsVM, orchardVM, imageVM)
}
MyApp(appViewModels)
```

And each screen just needs:

```kotlin
fun SomeScreen(viewModels: AppViewModels) {
    val settings = viewModels.settings
    val orchard = viewModels.orchard
    val image = viewModels.image
}
```

---

#### Option 2 Use `CompositionLocalProvider`

You create a custom `CompositionLocal` that holds all your shared ViewModels:

```kotlin
val LocalAppViewModels = staticCompositionLocalOf<AppViewModels> {
    error("No AppViewModels provided")
}
```

Then wrap your app in it:

```kotlin
CompositionLocalProvider(LocalAppViewModels provides appViewModels) {
    AppNavigation()
}
```

And in every screen:

```kotlin
val viewModels = LocalAppViewModels.current
val orchard = viewModels.orchard
```



---

# Good Analyses and Peformances




## Startup Logs Performance Notes

### What Logs Show

| Log Example | Meaning |
|-------------|---------|
| `Skipped XX frames!` | App is doing too much on the main thread — causing UI lag |
| `Davey! duration=xxxms` | A single frame took over 700ms to draw = very laggy |
| `App running slow: Executing classloader ...` | Startup time is high, could be device or app's size |

---

### What to Watch For

- Make sure all heavy operations (like reading config JSONs) are wrapped in:
```kotlin
withContext(Dispatchers.IO) { ... }
```

- If you're doing multiple things (like `copyAssets`, `loadConfig`, etc), consider **chunking them**:
```kotlin
withContext(Dispatchers.IO) {
    // Step 1
    ...
    // Step 2
    ...
}
```

---

### How to Improve

- Profile startup using **Logcat** and **Android Studio Profiler**
- Make sure no `decodeStream`, `readJson`, or `saveFile` is ever on the UI thread
- Use logs like `Log.d("InitScreen", "🌱 Step X started")` to break up your process

---
