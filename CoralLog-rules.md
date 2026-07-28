# Coral Log - AI Developer Instructions

You are an expert Android Kotlin developer. You are writing code for "Coral Log", an offline-first menstrual cycle tracking application. Always adhere strictly to the following architectural guidelines, constraints, and conventions when generating or refactoring code.

## 1. Core Technology Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Local Storage:** Room Database (SQLite) and Jetpack DataStore (for preferences)
*   **Asynchrony:** Kotlin Coroutines and Flows (StateFlow/SharedFlow)
*   **Dependency Injection:** KOIN

## 2. Strict Project Constraints
*   **100% Offline-First:** The application must never make network calls, connect to external APIs, or use cloud databases (like Firebase). All data must persist locally via Room.
*   **Privacy by Design:** Do not add telemetry, tracking, or analytics.
*   **Bilingual Support (i18n):** Never hardcode strings in the UI. Always use `strings.xml` resources to ensure seamless support for English (EN) and Spanish (ES).
*   **Theming:** UI must adapt automatically to system Light/Dark modes using Compose Material Design guidelines.

## 3. Project Structure: Feature-Based
Organize the codebase by feature, not by layer. All files related to a specific feature should live in the same package.
*   Example structure: `com.app.corallog.feature.calendar`
    *   `CalendarScreen.kt` (UI)
    *   `CalendarViewModel.kt` (Logic)
    *   `CalendarState.kt` (UI State)
    *   `CalendarRepository.kt` (Data coordination)

## 4. State Management
Always use **Sealed Interfaces** to model UI State. This ensures exhaustive handling of all possible states (preventing blank screens).
*   Example:
    ```kotlin
    sealed interface DashboardUiState {
        data object Loading : DashboardUiState
        data object Empty : DashboardUiState // e.g., Not enough data (HU-10)
        data class Success(val averageCycle: Int) : DashboardUiState
        data class Error(val message: String) : DashboardUiState
    }
    ```
*   ViewModels must expose state to the UI using `StateFlow`.

## 5. Naming Conventions
*   **Composables:** Use PascalCase. Full-screen composables MUST end with `Screen` (e.g., `CalendarScreen`). Reusable widget composables should describe their function (e.g., `SymptomCard`).
*   **Room Entities:** Database data classes MUST end with `Entity` (e.g., `CycleEntity`, `SymptomEntity`).
*   **Room DAOs:** Interfaces for database access MUST end with `Dao` (e.g., `CycleDao`).
*   **ViewModels:** MUST end with `ViewModel` (e.g., `CalendarViewModel`).
*   **Repositories:** MUST end with `Repository` (e.g., `CycleRepository`).

## 6. Dependency Injection (Koin)
*   Use Koin for all dependency injection.
*   Do not manually instantiate ViewModels, Repositories, or the Room Database in the UI layer.
*   Define Koin modules cleanly (e.g., `appModule`, `databaseModule`) and use standard Koin Compose functions like `koinViewModel()` in the UI.

## 7. Code Documentation (KDoc)
*   All public functions, classes, and complex logic blocks MUST be documented using standard KDoc (`/** ... */`).
*   KDoc must clearly explain the *why* behind the logic, document the parameters (`@param`), and document return types (`@return`).
*   Use Markdown formatting inside KDoc to ensure readability in Android Studio tooltips.

## 8. Security & Secrets
*   NEVER hardcode API keys, secrets, private tokens, or passwords into any `.kt`, `.xml`, or `build.gradle` file.
*   If a secret or key is ever required, store it in `local.properties` and read it via `BuildConfig` or the Secrets Gradle Plugin.